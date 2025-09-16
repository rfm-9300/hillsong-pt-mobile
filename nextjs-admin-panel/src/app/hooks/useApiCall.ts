'use client';

import { useState, useCallback, useRef, useEffect } from 'react';
import { enhancedApiService, ApiCallOptions } from '@/lib/enhancedApiService';
import { subscribeToLoading } from '@/lib/loadingStateService';
import { errorHandlingService } from '@/lib/errorHandlingService';
import { useErrorContext } from '@/app/context/ErrorContext';

interface UseEnhancedApiCallOptions extends ApiCallOptions {
  onSuccess?: (data: unknown) => void;
  autoExecute?: boolean;
  dependencies?: unknown[];
}

interface UseEnhancedApiCallState<T> {
  data: T | null;
  loading: boolean;
  error: Error | null;
  retryAttempt: number;
  progress?: number;
}

export function useEnhancedApiCall<T, TArgs extends unknown[] = []>(
  apiFunction: (...args: TArgs) => Promise<T>,
  options: UseEnhancedApiCallOptions = {}
) {
  const {
    onSuccess,
    autoExecute = false,
    dependencies = [],
    loadingKey,
    ...apiOptions
  } = options;

  const { handleError, showSuccess } = useErrorContext();
  const [state, setState] = useState<UseEnhancedApiCallState<T>>({
    data: null,
    loading: false,
    error: null,
    retryAttempt: 0,
  });

  const mountedRef = useRef(true);
  const lastArgsRef = useRef<TArgs | null>(null);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  // Subscribe to loading state changes if loadingKey is provided
  useEffect(() => {
    if (!loadingKey) return;

    const unsubscribe = subscribeToLoading(loadingKey, (loadingState) => {
      if (!mountedRef.current) return;
      
      setState(prev => ({
        ...prev,
        loading: loadingState.isLoading,
        progress: loadingState.progress,
      }));
    });

    return unsubscribe;
  }, [loadingKey]);

  const execute = useCallback(async (...args: TArgs): Promise<T | null> => {
    if (!mountedRef.current) return null;

    lastArgsRef.current = args;
    
    setState(prev => ({
      ...prev,
      loading: true,
      error: null,
    }));

    try {
      const result = await apiFunction(...args);
      
      if (!mountedRef.current) return null;

      setState(prev => ({
        ...prev,
        data: result,
        loading: false,
        error: null,
        retryAttempt: 0,
      }));

      onSuccess?.(result);
      
      // Show success message if configured
      if (apiOptions.showToUser !== false) {
        showSuccess('Operation completed successfully');
      }

      return result;
    } catch (error) {
      if (!mountedRef.current) return null;

      const errorObj = error instanceof Error ? error : new Error('Unknown error occurred');
      
      setState(prev => ({
        ...prev,
        loading: false,
        error: errorObj,
        retryAttempt: prev.retryAttempt + 1,
      }));

      // Handle error through context
      if (apiOptions.showToUser !== false) {
        handleError(errorObj, apiOptions.context);
      }

      throw errorObj;
    }
  }, [apiFunction, onSuccess, apiOptions, handleError, showSuccess]);

  const retry = useCallback(async (): Promise<T | null> => {
    if (lastArgsRef.current) {
      return execute(...lastArgsRef.current);
    }
    return null;
  }, [execute]);

  const reset = useCallback(() => {
    setState({
      data: null,
      loading: false,
      error: null,
      retryAttempt: 0,
    });
    lastArgsRef.current = null;
  }, []);

  // Auto-execute on dependency changes
  useEffect(() => {
    if (autoExecute && dependencies && dependencies.length > 0) {
      execute(...([] as unknown as TArgs));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoExecute, execute, ...(dependencies || [])]);

  return {
    ...state,
    execute,
    retry,
    reset,
    isRetrying: state.retryAttempt > 0,
    canRetry: state.error && errorHandlingService.shouldRetry(state.error),
  };
}

// Enhanced hook for multiple API calls
export function useEnhancedMultipleApiCalls<T extends Record<string, unknown>>(
  apiCalls: Record<keyof T, () => Promise<T[keyof T]>>,
  options: UseEnhancedApiCallOptions & {
    failFast?: boolean;
    batchLoadingKey?: string;
  } = {}
) {
  const {
    failFast = false,
    batchLoadingKey = 'batch_request',
    onSuccess,
    ...apiOptions
  } = options;

  const { handleError, showSuccess } = useErrorContext();
  const [state, setState] = useState<{
    data: Partial<T>;
    loading: Record<keyof T, boolean>;
    errors: Record<keyof T, Error | null>;
    globalLoading: boolean;
    globalError: Error | null;
    progress?: number;
  }>({
    data: {},
    loading: {} as Record<keyof T, boolean>,
    errors: {} as Record<keyof T, Error | null>,
    globalLoading: false,
    globalError: null,
  });

  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  // Subscribe to batch loading state
  useEffect(() => {
    const unsubscribe = subscribeToLoading(batchLoadingKey, (loadingState) => {
      if (!mountedRef.current) return;
      
      setState(prev => ({
        ...prev,
        globalLoading: loadingState.isLoading,
        progress: loadingState.progress,
      }));
    });

    return unsubscribe;
  }, [batchLoadingKey]);

  const executeAll = useCallback(async () => {
    if (!mountedRef.current) return;

    try {
      const result = await enhancedApiService.batch(apiCalls, {
        failFast,
        batchLoadingKey,
        ...apiOptions,
      });

      if (!mountedRef.current) return;

      const keys = Object.keys(apiCalls) as (keyof T)[];
      const newLoading = keys.reduce((acc, key) => ({ ...acc, [key]: false }), {} as Record<keyof T, boolean>);

      setState(prev => ({
        ...prev,
        data: { ...prev.data, ...result.data },
        loading: newLoading,
        errors: result.errors,
        globalLoading: false,
        globalError: Object.values(result.errors).some(error => error !== null) 
          ? new Error('Some operations failed') 
          : null,
      }));

      onSuccess?.(result.data);

      // Show success message if all operations succeeded
      if (Object.values(result.errors).every(error => error === null)) {
        showSuccess('All operations completed successfully');
      }

      return result;
    } catch (error) {
      if (!mountedRef.current) return;

      const errorObj = error instanceof Error ? error : new Error('Batch operation failed');
      
      setState(prev => ({
        ...prev,
        globalLoading: false,
        globalError: errorObj,
      }));

      handleError(errorObj, 'Batch API Operation');
      throw errorObj;
    }
  }, [apiCalls, failFast, batchLoadingKey, apiOptions, onSuccess, handleError, showSuccess]);

  const retry = useCallback((key?: keyof T) => {
    if (key && apiCalls[key]) {
      // Retry specific API call
      setState(prev => ({
        ...prev,
        loading: { ...prev.loading, [key]: true },
        errors: { ...prev.errors, [key]: null },
      }));

      apiCalls[key]()
        .then(result => {
          if (!mountedRef.current) return;
          setState(prev => ({
            ...prev,
            data: { ...prev.data, [key]: result },
            loading: { ...prev.loading, [key]: false },
          }));
          showSuccess(`${String(key)} operation completed successfully`);
        })
        .catch(error => {
          if (!mountedRef.current) return;
          const errorObj = error instanceof Error ? error : new Error('Unknown error occurred');
          setState(prev => ({
            ...prev,
            loading: { ...prev.loading, [key]: false },
            errors: { ...prev.errors, [key]: errorObj },
          }));
          handleError(errorObj, `Retry ${String(key)}`);
        });
    } else {
      // Retry all API calls
      executeAll();
    }
  }, [apiCalls, executeAll, handleError, showSuccess]);

  return {
    ...state,
    executeAll,
    retry,
  };
}

// Legacy compatibility - keep existing useApiCall for backward compatibility
export function useApiCall<T, TArgs extends unknown[] = []>(
  apiFunction: (...args: TArgs) => Promise<T>,
  options: UseEnhancedApiCallOptions = {}
) {
  return useEnhancedApiCall<T, TArgs>(apiFunction, options);
}

// Legacy compatibility - keep existing useMultipleApiCalls
export function useMultipleApiCalls<T extends Record<string, unknown>>(
  apiCalls: Record<keyof T, () => Promise<T[keyof T]>>,
  options: UseEnhancedApiCallOptions = {}
) {
  return useEnhancedMultipleApiCalls<T>(apiCalls, options);
}
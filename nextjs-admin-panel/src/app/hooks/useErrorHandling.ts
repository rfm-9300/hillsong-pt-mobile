'use client';

import { useState, useCallback, useRef, useEffect } from 'react';

export interface ErrorState {
  message: string;
  code?: string;
  details?: unknown;
  timestamp: number;
  retryable: boolean;
}

interface UseErrorHandlingOptions {
  onError?: (error: ErrorState) => void;
  logErrors?: boolean;
  showToast?: boolean;
  retryable?: boolean;
}

export function useErrorHandling(options: UseErrorHandlingOptions = {}) {
  const {
    onError,
    logErrors = true,
    showToast = false,
    retryable = true,
  } = options;

  const [error, setError] = useState<ErrorState | null>(null);
  const [errorHistory, setErrorHistory] = useState<ErrorState[]>([]);
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  const handleError = useCallback((error: unknown, context?: string) => {
    if (!mountedRef.current) return;

    let errorState: ErrorState;

    if (error instanceof Error) {
      errorState = {
        message: error.message,
        code: 'code' in error ? (error as { code: string }).code : undefined,
        details: 'details' in error ? (error as { details: unknown }).details : undefined,
        timestamp: Date.now(),
        retryable: retryable && !isNonRetryableError(error),
      };
    } else if (typeof error === 'string') {
      errorState = {
        message: error,
        timestamp: Date.now(),
        retryable,
      };
    } else {
      errorState = {
        message: 'An unknown error occurred',
        details: error,
        timestamp: Date.now(),
        retryable,
      };
    }

    // Add context if provided
    if (context) {
      errorState.message = `${context}: ${errorState.message}`;
    }

    // Log error if enabled
    if (logErrors) {
      console.error('Error handled:', errorState, error);
    }

    // Update state
    setError(errorState);
    setErrorHistory(prev => [errorState, ...prev.slice(0, 9)]); // Keep last 10 errors

    // Call custom error handler
    onError?.(errorState);

    // Show toast if enabled (would need toast system)
    if (showToast) {
      // This would integrate with a toast notification system
      console.warn('Toast notifications not implemented');
    }
  }, [onError, logErrors, showToast, retryable]);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const clearErrorHistory = useCallback(() => {
    setErrorHistory([]);
  }, []);

  const getErrorMessage = useCallback((error: unknown): string => {
    if (error instanceof Error) {
      return error.message;
    }
    if (typeof error === 'string') {
      return error;
    }
    return 'An unknown error occurred';
  }, []);

  const isRetryableError = useCallback((error: unknown): boolean => {
    if (error instanceof Error) {
      return !isNonRetryableError(error);
    }
    return true;
  }, []);

  return {
    error,
    errorHistory,
    handleError,
    clearError,
    clearErrorHistory,
    getErrorMessage,
    isRetryableError,
    hasError: error !== null,
  };
}

// Helper function to determine if an error is retryable
function isNonRetryableError(error: Error): boolean {
  const nonRetryableCodes = [
    'AUTHENTICATION_REQUIRED',
    'ACCESS_DENIED',
    'NOT_FOUND',
    'VALIDATION_ERROR',
    'BAD_REQUEST',
  ];

  const errorCode = 'code' in error ? (error as { code: string }).code : undefined;
  if (errorCode && nonRetryableCodes.includes(errorCode)) {
    return true;
  }

  // Check for HTTP status codes in error message
  const message = error.message.toLowerCase();
  if (message.includes('401') || message.includes('403') || 
      message.includes('404') || message.includes('400')) {
    return true;
  }

  return false;
}

// Global error handler hook for unhandled errors
export function useGlobalErrorHandler() {
  const { handleError } = useErrorHandling({
    logErrors: true,
    showToast: true,
  });

  useEffect(() => {
    const handleUnhandledError = (event: ErrorEvent) => {
      handleError(event.error, 'Unhandled Error');
    };

    const handleUnhandledRejection = (event: PromiseRejectionEvent) => {
      handleError(event.reason, 'Unhandled Promise Rejection');
    };

    window.addEventListener('error', handleUnhandledError);
    window.addEventListener('unhandledrejection', handleUnhandledRejection);

    return () => {
      window.removeEventListener('error', handleUnhandledError);
      window.removeEventListener('unhandledrejection', handleUnhandledRejection);
    };
  }, [handleError]);

  return { handleError };
}

// Hook for API error handling with specific patterns
export function useApiErrorHandling() {
  const { handleError: baseHandleError, ...rest } = useErrorHandling({
    logErrors: true,
    retryable: true,
  });

  const handleApiError = useCallback((error: unknown, endpoint?: string) => {
    let context = 'API Error';
    if (endpoint) {
      context = `API Error (${endpoint})`;
    }

    // Transform API errors to user-friendly messages
    if (error instanceof Error) {
      const message = error.message;
      
      if (message.includes('Authentication required')) {
        baseHandleError(new Error('Please log in to continue'), context);
        return;
      }
      
      if (message.includes('Access denied')) {
        baseHandleError(new Error('You do not have permission to perform this action'), context);
        return;
      }
      
      if (message.includes('Not found')) {
        baseHandleError(new Error('The requested resource was not found'), context);
        return;
      }
      
      if (message.includes('Network error')) {
        baseHandleError(new Error('Unable to connect to the server. Please check your internet connection'), context);
        return;
      }
      
      if (message.includes('timed out')) {
        baseHandleError(new Error('The request timed out. Please try again'), context);
        return;
      }
    }

    baseHandleError(error, context);
  }, [baseHandleError]);

  return {
    ...rest,
    handleError: handleApiError,
    handleApiError,
  };
}
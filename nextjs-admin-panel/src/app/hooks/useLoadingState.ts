'use client';

import { useState, useCallback, useRef, useEffect } from 'react';

interface LoadingState {
  [key: string]: boolean;
}

interface LoadingOptions {
  timeout?: number;
  onTimeout?: (key: string) => void;
  onStart?: (key: string) => void;
  onEnd?: (key: string) => void;
}

export function useLoadingState(options: LoadingOptions = {}) {
  const [loadingStates, setLoadingStates] = useState<LoadingState>({});
  const timeoutsRef = useRef<Record<string, NodeJS.Timeout>>({});
  const mountedRef = useRef(true);

  const { timeout, onTimeout, onStart, onEnd } = options;

  useEffect(() => {
    mountedRef.current = true;
    const currentTimeouts = timeoutsRef.current;
    return () => {
      mountedRef.current = false;
      // Clear all timeouts on unmount
      Object.values(currentTimeouts).forEach(clearTimeout);
    };
  }, []);

  const setLoading = useCallback((key: string, loading: boolean) => {
    if (!mountedRef.current) return;

    setLoadingStates(prev => ({
      ...prev,
      [key]: loading,
    }));

    if (loading) {
      onStart?.(key);
      
      // Set timeout if specified
      if (timeout) {
        timeoutsRef.current[key] = setTimeout(() => {
          if (mountedRef.current) {
            onTimeout?.(key);
            setLoadingStates(prev => ({
              ...prev,
              [key]: false,
            }));
          }
        }, timeout);
      }
    } else {
      onEnd?.(key);
      
      // Clear timeout
      if (timeoutsRef.current[key]) {
        clearTimeout(timeoutsRef.current[key]);
        delete timeoutsRef.current[key];
      }
    }
  }, [timeout, onTimeout, onStart, onEnd]);

  const isLoading = useCallback((key: string) => {
    return loadingStates[key] || false;
  }, [loadingStates]);

  const isAnyLoading = useCallback(() => {
    return Object.values(loadingStates).some(Boolean);
  }, [loadingStates]);

  const clearLoading = useCallback((key?: string) => {
    if (key) {
      setLoading(key, false);
    } else {
      // Clear all loading states
      Object.keys(loadingStates).forEach(k => setLoading(k, false));
    }
  }, [loadingStates, setLoading]);

  const withLoading = useCallback(async <T>(
    key: string,
    asyncFunction: () => Promise<T>
  ): Promise<T> => {
    setLoading(key, true);
    try {
      const result = await asyncFunction();
      return result;
    } finally {
      setLoading(key, false);
    }
  }, [setLoading]);

  return {
    loadingStates,
    setLoading,
    isLoading,
    isAnyLoading,
    clearLoading,
    withLoading,
  };
}

// Hook for managing multiple loading states with categories
export function useCategorizedLoadingState() {
  const [loadingStates, setLoadingStates] = useState<Record<string, LoadingState>>({});
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  const setLoading = useCallback((category: string, key: string, loading: boolean) => {
    if (!mountedRef.current) return;

    setLoadingStates(prev => ({
      ...prev,
      [category]: {
        ...prev[category],
        [key]: loading,
      },
    }));
  }, []);

  const isLoading = useCallback((category: string, key?: string) => {
    if (!loadingStates[category]) return false;
    
    if (key) {
      return loadingStates[category][key] || false;
    }
    
    // Check if any item in category is loading
    return Object.values(loadingStates[category]).some(Boolean);
  }, [loadingStates]);

  const clearCategory = useCallback((category: string) => {
    setLoadingStates(prev => ({
      ...prev,
      [category]: {},
    }));
  }, []);

  const withCategorizedLoading = useCallback(async <T>(
    category: string,
    key: string,
    asyncFunction: () => Promise<T>
  ): Promise<T> => {
    setLoading(category, key, true);
    try {
      const result = await asyncFunction();
      return result;
    } finally {
      setLoading(category, key, false);
    }
  }, [setLoading]);

  return {
    loadingStates,
    setLoading,
    isLoading,
    clearCategory,
    withCategorizedLoading,
  };
}

// Hook for managing loading states with progress tracking
export function useProgressiveLoadingState() {
  const [loadingStates, setLoadingStates] = useState<Record<string, { loading: boolean; progress: number }>>({});
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  const setLoading = useCallback((key: string, loading: boolean, progress = 0) => {
    if (!mountedRef.current) return;

    setLoadingStates(prev => ({
      ...prev,
      [key]: { loading, progress: Math.max(0, Math.min(100, progress)) },
    }));
  }, []);

  const setProgress = useCallback((key: string, progress: number) => {
    if (!mountedRef.current) return;

    setLoadingStates(prev => ({
      ...prev,
      [key]: {
        loading: prev[key]?.loading || false,
        progress: Math.max(0, Math.min(100, progress)),
      },
    }));
  }, []);

  const isLoading = useCallback((key: string) => {
    return loadingStates[key]?.loading || false;
  }, [loadingStates]);

  const getProgress = useCallback((key: string) => {
    return loadingStates[key]?.progress || 0;
  }, [loadingStates]);

  const withProgressiveLoading = useCallback(async <T>(
    key: string,
    asyncFunction: (updateProgress: (progress: number) => void) => Promise<T>
  ): Promise<T> => {
    setLoading(key, true, 0);
    
    const updateProgress = (progress: number) => {
      setProgress(key, progress);
    };

    try {
      const result = await asyncFunction(updateProgress);
      setProgress(key, 100);
      return result;
    } finally {
      // Keep loading state briefly to show 100% completion
      setTimeout(() => {
        if (mountedRef.current) {
          setLoading(key, false, 0);
        }
      }, 500);
    }
  }, [setLoading, setProgress]);

  return {
    loadingStates,
    setLoading,
    setProgress,
    isLoading,
    getProgress,
    withProgressiveLoading,
  };
}
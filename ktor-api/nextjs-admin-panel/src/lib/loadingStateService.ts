/**
 * Centralized loading state management service
 * Provides consistent loading state handling across the application
 */

export interface LoadingState {
  isLoading: boolean;
  progress?: number; // 0-100
  message?: string;
  startTime?: number;
  timeout?: number;
}

export interface LoadingOptions {
  message?: string;
  timeout?: number;
  onTimeout?: () => void;
  showProgress?: boolean;
  category?: string;
}

class LoadingStateService {
  private static instance: LoadingStateService;
  private loadingStates: Map<string, LoadingState> = new Map();
  private timeouts: Map<string, NodeJS.Timeout> = new Map();
  private listeners: Map<string, Set<(state: LoadingState) => void>> = new Map();

  private constructor() {}

  static getInstance(): LoadingStateService {
    if (!LoadingStateService.instance) {
      LoadingStateService.instance = new LoadingStateService();
    }
    return LoadingStateService.instance;
  }

  /**
   * Start loading for a specific key
   */
  startLoading(key: string, options: LoadingOptions = {}): void {
    const { message, timeout, onTimeout, showProgress = false } = options;
    
    const state: LoadingState = {
      isLoading: true,
      message,
      startTime: Date.now(),
      timeout,
      progress: showProgress ? 0 : undefined,
    };

    this.loadingStates.set(key, state);

    // Set timeout if specified
    if (timeout) {
      const timeoutId = setTimeout(() => {
        this.handleTimeout(key, onTimeout);
      }, timeout);
      this.timeouts.set(key, timeoutId);
    }

    this.notifyListeners(key, state);
  }

  /**
   * Stop loading for a specific key
   */
  stopLoading(key: string): void {
    const state = this.loadingStates.get(key);
    if (state) {
      const updatedState: LoadingState = {
        ...state,
        isLoading: false,
        progress: state.progress !== undefined ? 100 : undefined,
      };
      
      this.loadingStates.set(key, updatedState);
      this.clearTimeout(key);
      this.notifyListeners(key, updatedState);

      // Clean up after a short delay to show completion
      setTimeout(() => {
        this.loadingStates.delete(key);
        this.notifyListeners(key, { isLoading: false });
      }, 500);
    }
  }

  /**
   * Update progress for a loading operation
   */
  updateProgress(key: string, progress: number, message?: string): void {
    const state = this.loadingStates.get(key);
    if (state && state.isLoading) {
      const updatedState: LoadingState = {
        ...state,
        progress: Math.max(0, Math.min(100, progress)),
        message: message || state.message,
      };
      
      this.loadingStates.set(key, updatedState);
      this.notifyListeners(key, updatedState);
    }
  }

  /**
   * Check if a specific key is loading
   */
  isLoading(key: string): boolean {
    const state = this.loadingStates.get(key);
    return state?.isLoading || false;
  }

  /**
   * Check if any operation is loading
   */
  isAnyLoading(): boolean {
    return Array.from(this.loadingStates.values()).some(state => state.isLoading);
  }

  /**
   * Check if any operation in a category is loading
   */
  isCategoryLoading(category: string): boolean {
    return Array.from(this.loadingStates.keys())
      .filter(key => key.startsWith(`${category}:`))
      .some(key => this.isLoading(key));
  }

  /**
   * Get loading state for a specific key
   */
  getLoadingState(key: string): LoadingState {
    return this.loadingStates.get(key) || { isLoading: false };
  }

  /**
   * Get all loading states
   */
  getAllLoadingStates(): Record<string, LoadingState> {
    const states: Record<string, LoadingState> = {};
    this.loadingStates.forEach((state, key) => {
      states[key] = state;
    });
    return states;
  }

  /**
   * Get loading states for a specific category
   */
  getCategoryLoadingStates(category: string): Record<string, LoadingState> {
    const states: Record<string, LoadingState> = {};
    this.loadingStates.forEach((state, key) => {
      if (key.startsWith(`${category}:`)) {
        states[key] = state;
      }
    });
    return states;
  }

  /**
   * Clear all loading states
   */
  clearAll(): void {
    this.loadingStates.forEach((_, key) => {
      this.clearTimeout(key);
    });
    this.loadingStates.clear();
    this.notifyAllListeners();
  }

  /**
   * Clear loading states for a specific category
   */
  clearCategory(category: string): void {
    const keysToDelete = Array.from(this.loadingStates.keys())
      .filter(key => key.startsWith(`${category}:`));
    
    keysToDelete.forEach(key => {
      this.clearTimeout(key);
      this.loadingStates.delete(key);
      this.notifyListeners(key, { isLoading: false });
    });
  }

  /**
   * Wrap an async operation with loading state
   */
  async withLoading<T>(
    key: string,
    operation: (updateProgress?: (progress: number, message?: string) => void) => Promise<T>,
    options: LoadingOptions = {}
  ): Promise<T> {
    this.startLoading(key, options);

    const updateProgress = (progress: number, message?: string) => {
      this.updateProgress(key, progress, message);
    };

    try {
      const result = await operation(updateProgress);
      this.stopLoading(key);
      return result;
    } catch (error) {
      this.stopLoading(key);
      throw error;
    }
  }

  /**
   * Subscribe to loading state changes for a specific key
   */
  subscribe(key: string, listener: (state: LoadingState) => void): () => void {
    if (!this.listeners.has(key)) {
      this.listeners.set(key, new Set());
    }
    
    this.listeners.get(key)!.add(listener);

    // Return unsubscribe function
    return () => {
      const keyListeners = this.listeners.get(key);
      if (keyListeners) {
        keyListeners.delete(listener);
        if (keyListeners.size === 0) {
          this.listeners.delete(key);
        }
      }
    };
  }

  /**
   * Subscribe to all loading state changes
   */
  subscribeToAll(listener: (states: Record<string, LoadingState>) => void): () => void {
    const globalKey = '__global__';
    const wrappedListener = () => {
      listener(this.getAllLoadingStates());
    };

    return this.subscribe(globalKey, wrappedListener);
  }

  /**
   * Handle timeout for a loading operation
   */
  private handleTimeout(key: string, onTimeout?: () => void): void {
    const state = this.loadingStates.get(key);
    if (state && state.isLoading) {
      console.warn(`Loading operation "${key}" timed out after ${state.timeout}ms`);
      onTimeout?.();
      
      // Optionally stop loading on timeout
      // this.stopLoading(key);
    }
  }

  /**
   * Clear timeout for a specific key
   */
  private clearTimeout(key: string): void {
    const timeoutId = this.timeouts.get(key);
    if (timeoutId) {
      clearTimeout(timeoutId);
      this.timeouts.delete(key);
    }
  }

  /**
   * Notify listeners for a specific key
   */
  private notifyListeners(key: string, state: LoadingState): void {
    const keyListeners = this.listeners.get(key);
    if (keyListeners) {
      keyListeners.forEach(listener => {
        try {
          listener(state);
        } catch (error) {
          console.error('Error in loading state listener:', error);
        }
      });
    }

    // Notify global listeners
    this.notifyGlobalListeners();
  }

  /**
   * Notify global listeners
   */
  private notifyGlobalListeners(): void {
    const globalListeners = this.listeners.get('__global__');
    if (globalListeners) {
      globalListeners.forEach(listener => {
        try {
          // Global listeners are wrapped functions that don't take parameters
          (listener as () => void)();
        } catch (error) {
          console.error('Error in global loading state listener:', error);
        }
      });
    }
  }

  /**
   * Notify all listeners
   */
  private notifyAllListeners(): void {
    this.listeners.forEach((listeners, key) => {
      const state = this.getLoadingState(key);
      listeners.forEach(listener => {
        try {
          if (key === '__global__') {
            // Global listeners are wrapped functions that don't take parameters
            (listener as () => void)();
          } else {
            listener(state);
          }
        } catch (error) {
          console.error('Error in loading state listener:', error);
        }
      });
    });
  }
}

// Export singleton instance
export const loadingStateService = LoadingStateService.getInstance();

// Convenience functions
export const startLoading = (key: string, options?: LoadingOptions) =>
  loadingStateService.startLoading(key, options);

export const stopLoading = (key: string) =>
  loadingStateService.stopLoading(key);

export const updateProgress = (key: string, progress: number, message?: string) =>
  loadingStateService.updateProgress(key, progress, message);

export const isLoading = (key: string) =>
  loadingStateService.isLoading(key);

export const isAnyLoading = () =>
  loadingStateService.isAnyLoading();

export const isCategoryLoading = (category: string) =>
  loadingStateService.isCategoryLoading(category);

export const getLoadingState = (key: string) =>
  loadingStateService.getLoadingState(key);

export const withLoading = <T>(
  key: string,
  operation: (updateProgress?: (progress: number, message?: string) => void) => Promise<T>,
  options?: LoadingOptions
) => loadingStateService.withLoading(key, operation, options);

export const subscribeToLoading = (key: string, listener: (state: LoadingState) => void) =>
  loadingStateService.subscribe(key, listener);

export const subscribeToAllLoading = (listener: (states: Record<string, LoadingState>) => void) =>
  loadingStateService.subscribeToAll(listener);
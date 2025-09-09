import { api } from './api';

// Enhanced API client with better error handling and retry logic
export class EnhancedApiClient {
  private static instance: EnhancedApiClient;
  
  private constructor() {}
  
  static getInstance(): EnhancedApiClient {
    if (!EnhancedApiClient.instance) {
      EnhancedApiClient.instance = new EnhancedApiClient();
    }
    return EnhancedApiClient.instance;
  }

  // Generic request method with enhanced error handling
  async request<T>(
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    endpoint: string,
    data?: unknown,
    options: {
      retries?: number;
      timeout?: number;
      onRetry?: (attempt: number, error: Error) => void;
      onError?: (error: Error) => void;
    } = {}
  ): Promise<T> {
    const { retries = 3, onRetry, onError } = options;
    
    let lastError: Error;
    
    for (let attempt = 0; attempt <= retries; attempt++) {
      try {
        let result: T | null;
        
        switch (method) {
          case 'GET':
            result = await api.get<T>(endpoint);
            break;
          case 'POST':
            result = await api.post<T>(endpoint, data);
            break;
          case 'PUT':
            result = await api.put<T>(endpoint, data);
            break;
          case 'DELETE':
            result = await api.delete<T>(endpoint, data);
            break;
          default:
            throw new Error(`Unsupported HTTP method: ${method}`);
        }
        
        return result as T;
      } catch (error) {
        lastError = error instanceof Error ? error : new Error('Unknown error');
        
        // Don't retry on certain errors
        if (this.isNonRetryableError(lastError) || attempt === retries) {
          onError?.(lastError);
          throw lastError;
        }
        
        // Call retry callback
        onRetry?.(attempt + 1, lastError);
        
        // Wait before retrying (exponential backoff)
        const delay = Math.min(1000 * Math.pow(2, attempt), 10000);
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
    
    throw lastError!;
  }

  private isNonRetryableError(error: Error): boolean {
    const message = error.message.toLowerCase();
    
    // Don't retry on authentication, authorization, or validation errors
    if (message.includes('401') || message.includes('403') || 
        message.includes('400') || message.includes('404') ||
        message.includes('authentication') || message.includes('access denied') ||
        message.includes('validation') || message.includes('bad request')) {
      return true;
    }
    
    return false;
  }

  // Convenience methods
  async get<T>(endpoint: string, options?: Parameters<typeof this.request>[3]): Promise<T> {
    return this.request<T>('GET', endpoint, undefined, options);
  }

  async post<T>(endpoint: string, data?: unknown, options?: Parameters<typeof this.request>[3]): Promise<T> {
    return this.request<T>('POST', endpoint, data, options);
  }

  async put<T>(endpoint: string, data?: unknown, options?: Parameters<typeof this.request>[3]): Promise<T> {
    return this.request<T>('PUT', endpoint, data, options);
  }

  async delete<T>(endpoint: string, data?: unknown, options?: Parameters<typeof this.request>[3]): Promise<T> {
    return this.request<T>('DELETE', endpoint, data, options);
  }

  // Batch requests with error handling
  async batch<T extends Record<string, unknown>>(
    requests: Record<keyof T, () => Promise<T[keyof T]>>,
    options: {
      failFast?: boolean;
      onPartialFailure?: (errors: Record<keyof T, Error | null>) => void;
    } = {}
  ): Promise<{ data: Partial<T>; errors: Record<keyof T, Error | null> }> {
    const { failFast = false, onPartialFailure } = options;
    const keys = Object.keys(requests) as (keyof T)[];
    const data: Partial<T> = {};
    const errors: Record<keyof T, Error | null> = {} as Record<keyof T, Error | null>;

    if (failFast) {
      // Execute requests sequentially and fail on first error
      for (const key of keys) {
        try {
          data[key] = await requests[key]();
          errors[key] = null;
        } catch (error) {
          errors[key] = error instanceof Error ? error : new Error('Unknown error');
          throw error;
        }
      }
    } else {
      // Execute all requests and collect results/errors
      const results = await Promise.allSettled(
        keys.map(async (key) => ({ key, result: await requests[key]() }))
      );

      results.forEach((result, index) => {
        const key = keys[index];
        if (result.status === 'fulfilled') {
          data[key] = result.value.result;
          errors[key] = null;
        } else {
          errors[key] = result.reason instanceof Error ? result.reason : new Error('Unknown error');
        }
      });

      // Check if there are any errors
      const hasErrors = Object.values(errors).some(error => error !== null);
      if (hasErrors) {
        onPartialFailure?.(errors);
      }
    }

    return { data, errors };
  }
}

// Export singleton instance
export const apiClient = EnhancedApiClient.getInstance();

// Export convenience functions
export const apiGet = <T>(endpoint: string, options?: Parameters<EnhancedApiClient['request']>[3]) => 
  apiClient.get<T>(endpoint, options);

export const apiPost = <T>(endpoint: string, data?: unknown, options?: Parameters<EnhancedApiClient['request']>[3]) => 
  apiClient.post<T>(endpoint, data, options);

export const apiPut = <T>(endpoint: string, data?: unknown, options?: Parameters<EnhancedApiClient['request']>[3]) => 
  apiClient.put<T>(endpoint, data, options);

export const apiDelete = <T>(endpoint: string, data?: unknown, options?: Parameters<EnhancedApiClient['request']>[3]) => 
  apiClient.delete<T>(endpoint, data, options);

export const apiBatch = <T extends Record<string, unknown>>(
  requests: Record<keyof T, () => Promise<T[keyof T]>>,
  options?: Parameters<EnhancedApiClient['batch']>[1]
) => apiClient.batch<T>(requests, options);
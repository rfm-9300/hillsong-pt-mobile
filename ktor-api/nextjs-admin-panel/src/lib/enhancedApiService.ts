/**
 * Enhanced API service that integrates error handling and loading state management
 */

import { apiClient } from './apiClient';
import { errorHandlingService, ErrorHandlingOptions } from './errorHandlingService';
import { loadingStateService, LoadingOptions } from './loadingStateService';

export interface ApiCallOptions extends ErrorHandlingOptions, LoadingOptions {
  retries?: number;
  retryDelay?: number;
  loadingKey?: string;
  skipLoading?: boolean;
  skipErrorHandling?: boolean;
}

class EnhancedApiService {
  private static instance: EnhancedApiService;

  private constructor() {}

  static getInstance(): EnhancedApiService {
    if (!EnhancedApiService.instance) {
      EnhancedApiService.instance = new EnhancedApiService();
    }
    return EnhancedApiService.instance;
  }

  /**
   * Enhanced GET request with error handling and loading states
   */
  async get<T>(endpoint: string, options: ApiCallOptions = {}): Promise<T> {
    return this.executeRequest<T>('GET', endpoint, undefined, options);
  }

  /**
   * Enhanced POST request with error handling and loading states
   */
  async post<T>(endpoint: string, data?: unknown, options: ApiCallOptions = {}): Promise<T> {
    return this.executeRequest<T>('POST', endpoint, data, options);
  }

  /**
   * Enhanced PUT request with error handling and loading states
   */
  async put<T>(endpoint: string, data?: unknown, options: ApiCallOptions = {}): Promise<T> {
    return this.executeRequest<T>('PUT', endpoint, data, options);
  }

  /**
   * Enhanced DELETE request with error handling and loading states
   */
  async delete<T>(endpoint: string, data?: unknown, options: ApiCallOptions = {}): Promise<T> {
    return this.executeRequest<T>('DELETE', endpoint, data, options);
  }

  /**
   * Execute API request with comprehensive error handling and loading management
   */
  private async executeRequest<T>(
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    endpoint: string,
    data?: unknown,
    options: ApiCallOptions = {}
  ): Promise<T> {
    const {
      retries = 3,
      retryDelay = 1000,
      loadingKey = `${method.toLowerCase()}_${endpoint.replace(/\//g, '_')}`,
      skipLoading = false,
      skipErrorHandling = false,
      context = `${method} ${endpoint}`,
      message = `Loading...`,
      timeout = 30000,
      showProgress = false,
      ...restOptions
    } = options;

    // Start loading state
    if (!skipLoading) {
      loadingStateService.startLoading(loadingKey, {
        message,
        timeout,
        showProgress,
        onTimeout: () => {
          console.warn(`API request ${context} timed out`);
        },
      });
    }

    try {
      // Execute the API call with retry logic
      const result = await this.executeWithRetry<T>(
        method,
        endpoint,
        data,
        retries,
        retryDelay,
        loadingKey,
        showProgress
      );

      // Stop loading state
      if (!skipLoading) {
        loadingStateService.stopLoading(loadingKey);
      }

      return result;
    } catch (error) {
      // Stop loading state
      if (!skipLoading) {
        loadingStateService.stopLoading(loadingKey);
      }

      // Handle error
      if (!skipErrorHandling) {
        const errorDetails = errorHandlingService.processError(error, {
          context,
          ...restOptions,
        });

        // Re-throw with processed error details
        const enhancedError = new Error(errorDetails.message) as Error & { details?: unknown };
        enhancedError.details = errorDetails;
        throw enhancedError;
      }

      throw error;
    }
  }

  /**
   * Execute API call with retry logic
   */
  private async executeWithRetry<T>(
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    endpoint: string,
    data: unknown,
    maxRetries: number,
    retryDelay: number,
    loadingKey: string,
    showProgress: boolean
  ): Promise<T> {
    let lastError: Error;

    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        // Update progress if showing
        if (showProgress && attempt > 0) {
          const progress = (attempt / (maxRetries + 1)) * 50; // First 50% for retries
          loadingStateService.updateProgress(
            loadingKey,
            progress,
            `Retrying... (${attempt}/${maxRetries})`
          );
        }

        let result: T;
        switch (method) {
          case 'GET':
            result = await apiClient.get<T>(endpoint);
            break;
          case 'POST':
            result = await apiClient.post<T>(endpoint, data);
            break;
          case 'PUT':
            result = await apiClient.put<T>(endpoint, data);
            break;
          case 'DELETE':
            result = await apiClient.delete<T>(endpoint, data);
            break;
        }

        // Update progress to completion if showing
        if (showProgress) {
          loadingStateService.updateProgress(loadingKey, 100, 'Complete');
        }

        return result;
      } catch (error) {
        lastError = error instanceof Error ? error : new Error('Unknown error');

        // Check if error is retryable
        if (!errorHandlingService.shouldRetry(lastError) || attempt === maxRetries) {
          throw lastError;
        }

        // Wait before retrying with exponential backoff
        const delay = retryDelay * Math.pow(2, attempt);
        console.log(`Retrying ${method} ${endpoint} (attempt ${attempt + 1}/${maxRetries}) in ${delay}ms`);
        
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }

    throw lastError!;
  }

  /**
   * Batch API calls with comprehensive error handling
   */
  async batch<T extends Record<string, unknown>>(
    requests: Record<keyof T, () => Promise<T[keyof T]>>,
    options: ApiCallOptions & {
      failFast?: boolean;
      batchLoadingKey?: string;
    } = {}
  ): Promise<{ data: Partial<T>; errors: Record<keyof T, Error | null> }> {
    const {
      failFast = false,
      batchLoadingKey = 'batch_request',
      skipLoading = false,
      skipErrorHandling = false,
      context = 'Batch API Request',
      message = 'Loading multiple resources...',
      showProgress = true,
    } = options;

    const keys = Object.keys(requests) as (keyof T)[];
    const data: Partial<T> = {};
    const errors: Record<keyof T, Error | null> = {} as Record<keyof T, Error | null>;

    // Start batch loading
    if (!skipLoading) {
      loadingStateService.startLoading(batchLoadingKey, {
        message,
        showProgress,
      });
    }

    try {
      if (failFast) {
        // Execute sequentially and fail on first error
        for (let i = 0; i < keys.length; i++) {
          const key = keys[i];
          
          if (showProgress && !skipLoading) {
            const progress = (i / keys.length) * 100;
            loadingStateService.updateProgress(
              batchLoadingKey,
              progress,
              `Loading ${String(key)}...`
            );
          }

          try {
            data[key] = await requests[key]();
            errors[key] = null;
          } catch (error) {
            const processedError = skipErrorHandling 
              ? (error instanceof Error ? error : new Error('Unknown error'))
              : new Error(errorHandlingService.getUserFriendlyMessage(error, `${context} - ${String(key)}`));
            
            errors[key] = processedError;
            throw processedError;
          }
        }
      } else {
        // Execute all requests in parallel
        const results = await Promise.allSettled(
          keys.map(async (key, index) => {
            if (showProgress && !skipLoading) {
              // Update progress as requests complete
              setTimeout(() => {
                const progress = ((index + 1) / keys.length) * 100;
                loadingStateService.updateProgress(
                  batchLoadingKey,
                  progress,
                  `Loaded ${index + 1}/${keys.length} resources`
                );
              }, 100);
            }

            return { key, result: await requests[key]() };
          })
        );

        results.forEach((result, index) => {
          const key = keys[index];
          if (result.status === 'fulfilled') {
            data[key] = result.value.result;
            errors[key] = null;
          } else {
            const processedError = skipErrorHandling
              ? (result.reason instanceof Error ? result.reason : new Error('Unknown error'))
              : new Error(errorHandlingService.getUserFriendlyMessage(result.reason, `${context} - ${String(key)}`));
            
            errors[key] = processedError;
          }
        });
      }

      return { data, errors };
    } finally {
      // Stop batch loading
      if (!skipLoading) {
        loadingStateService.stopLoading(batchLoadingKey);
      }
    }
  }

  /**
   * Upload file with progress tracking
   */
  async uploadFile(
    endpoint: string,
    file: File,
    options: ApiCallOptions & {
      onProgress?: (progress: number) => void;
    } = {}
  ): Promise<unknown> {
    const {
      loadingKey = `upload_${file.name}`,
      message = `Uploading ${file.name}...`,
      onProgress,
      context = `Upload ${file.name}`,
      ...restOptions
    } = options;

    // Start loading with progress
    loadingStateService.startLoading(loadingKey, {
      message,
      showProgress: true,
    });

    try {
      // Create FormData
      const formData = new FormData();
      formData.append('file', file);

      // Create XMLHttpRequest for progress tracking
      return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();

        xhr.upload.addEventListener('progress', (event) => {
          if (event.lengthComputable) {
            const progress = (event.loaded / event.total) * 100;
            loadingStateService.updateProgress(loadingKey, progress, `Uploading... ${Math.round(progress)}%`);
            onProgress?.(progress);
          }
        });

        xhr.addEventListener('load', () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            try {
              const response = JSON.parse(xhr.responseText);
              loadingStateService.stopLoading(loadingKey);
              resolve(response);
            } catch (error) {
              const parseError = new Error('Failed to parse upload response');
              if (!restOptions.skipErrorHandling) {
                errorHandlingService.processError(parseError, { context });
              }
              loadingStateService.stopLoading(loadingKey);
              reject(parseError);
            }
          } else {
            const uploadError = new Error(`Upload failed with status ${xhr.status}`);
            if (!restOptions.skipErrorHandling) {
              errorHandlingService.processError(uploadError, { context });
            }
            loadingStateService.stopLoading(loadingKey);
            reject(uploadError);
          }
        });

        xhr.addEventListener('error', () => {
          const networkError = new Error('Upload failed due to network error');
          if (!restOptions.skipErrorHandling) {
            errorHandlingService.processError(networkError, { context });
          }
          loadingStateService.stopLoading(loadingKey);
          reject(networkError);
        });

        xhr.open('POST', endpoint);
        xhr.send(formData);
      });
    } catch (error) {
      loadingStateService.stopLoading(loadingKey);
      if (!restOptions.skipErrorHandling) {
        errorHandlingService.processError(error, { context });
      }
      throw error;
    }
  }
}

// Export singleton instance
export const enhancedApiService = EnhancedApiService.getInstance();

// Convenience functions
export const apiGet = <T>(endpoint: string, options?: ApiCallOptions) =>
  enhancedApiService.get<T>(endpoint, options);

export const apiPost = <T>(endpoint: string, data?: unknown, options?: ApiCallOptions) =>
  enhancedApiService.post<T>(endpoint, data, options);

export const apiPut = <T>(endpoint: string, data?: unknown, options?: ApiCallOptions) =>
  enhancedApiService.put<T>(endpoint, data, options);

export const apiDelete = <T>(endpoint: string, data?: unknown, options?: ApiCallOptions) =>
  enhancedApiService.delete<T>(endpoint, data, options);

export const apiBatch = <T extends Record<string, unknown>>(
  requests: Record<keyof T, () => Promise<T[keyof T]>>,
  options?: Parameters<EnhancedApiService['batch']>[1]
) => enhancedApiService.batch<T>(requests, options);

export const apiUpload = (
  endpoint: string,
  file: File,
  options?: Parameters<EnhancedApiService['uploadFile']>[2]
) => enhancedApiService.uploadFile(endpoint, file, options);
/**
 * Centralized error handling service for the admin panel
 * Provides consistent error processing, logging, and user feedback
 */

export interface ErrorDetails {
  message: string;
  code?: string;
  statusCode?: number;
  details?: unknown;
  timestamp: number;
  context?: string;
  retryable: boolean;
  userFriendly: boolean;
}

export interface ErrorHandlingOptions {
  context?: string;
  showToUser?: boolean;
  logError?: boolean;
  retryable?: boolean;
  onError?: (error: ErrorDetails) => void;
}

class ErrorHandlingService {
  private static instance: ErrorHandlingService;
  private errorHistory: ErrorDetails[] = [];
  private maxHistorySize = 50;

  private constructor() {}

  static getInstance(): ErrorHandlingService {
    if (!ErrorHandlingService.instance) {
      ErrorHandlingService.instance = new ErrorHandlingService();
    }
    return ErrorHandlingService.instance;
  }

  /**
   * Process and transform errors into standardized format
   */
  processError(error: unknown, options: ErrorHandlingOptions = {}): ErrorDetails {
    const {
      context,
      logError = true,
      retryable = true,
    } = options;

    let errorDetails: ErrorDetails;

    if (error instanceof Error) {
      errorDetails = this.processJavaScriptError(error, context);
    } else if (typeof error === 'string') {
      errorDetails = {
        message: error,
        timestamp: Date.now(),
        context,
        retryable,
        userFriendly: true,
      };
    } else if (this.isApiError(error)) {
      errorDetails = this.processApiError(error, context);
    } else {
      errorDetails = {
        message: 'An unexpected error occurred',
        details: error,
        timestamp: Date.now(),
        context,
        retryable,
        userFriendly: true,
      };
    }

    // Add to error history
    this.addToHistory(errorDetails);

    // Log error if enabled
    if (logError) {
      this.logError(errorDetails);
    }

    // Call custom error handler
    options.onError?.(errorDetails);

    return errorDetails;
  }

  /**
   * Process JavaScript/TypeScript errors
   */
  private processJavaScriptError(error: Error, context?: string): ErrorDetails {
    const errorDetails: ErrorDetails = {
      message: error.message,
      code: 'code' in error ? (error as { code: string }).code : undefined,
      timestamp: Date.now(),
      context,
      retryable: this.isRetryableError(error),
      userFriendly: false,
    };

    // Transform common errors to user-friendly messages
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      errorDetails.message = 'Unable to connect to the server. Please check your internet connection.';
      errorDetails.userFriendly = true;
    } else if (error.name === 'AbortError') {
      errorDetails.message = 'The request was cancelled.';
      errorDetails.userFriendly = true;
      errorDetails.retryable = false;
    } else if (error.message.includes('timeout')) {
      errorDetails.message = 'The request timed out. Please try again.';
      errorDetails.userFriendly = true;
    } else if (error.message.includes('Network')) {
      errorDetails.message = 'Network error. Please check your connection and try again.';
      errorDetails.userFriendly = true;
    }

    return errorDetails;
  }

  /**
   * Process API errors with specific handling
   */
  private processApiError(error: unknown, context?: string): ErrorDetails {
    const errorObj = error as Record<string, unknown>;
    const statusCode = errorObj.status || errorObj.statusCode;
    const errorCode = errorObj.code;
    
    let message = (errorObj.message as string) || 'An API error occurred';
    let userFriendly = false;
    let retryable = true;

    // Handle specific HTTP status codes
    switch (statusCode) {
      case 400:
        message = 'Invalid request. Please check your input and try again.';
        userFriendly = true;
        retryable = false;
        break;
      case 401:
        message = 'Authentication required. Please log in to continue.';
        userFriendly = true;
        retryable = false;
        break;
      case 403:
        message = 'You do not have permission to perform this action.';
        userFriendly = true;
        retryable = false;
        break;
      case 404:
        message = 'The requested resource was not found.';
        userFriendly = true;
        retryable = false;
        break;
      case 409:
        message = 'This action conflicts with existing data. Please refresh and try again.';
        userFriendly = true;
        retryable = false;
        break;
      case 422:
        message = 'The data provided is invalid. Please check your input.';
        userFriendly = true;
        retryable = false;
        break;
      case 429:
        message = 'Too many requests. Please wait a moment and try again.';
        userFriendly = true;
        retryable = true;
        break;
      case 500:
        message = 'Server error. Please try again later.';
        userFriendly = true;
        retryable = true;
        break;
      case 502:
      case 503:
      case 504:
        message = 'Service temporarily unavailable. Please try again later.';
        userFriendly = true;
        retryable = true;
        break;
    }

    // Handle specific error codes
    if (errorCode) {
      switch (errorCode) {
        case 'VALIDATION_ERROR':
          message = 'Please check your input and try again.';
          userFriendly = true;
          retryable = false;
          break;
        case 'AUTHENTICATION_REQUIRED':
          message = 'Please log in to continue.';
          userFriendly = true;
          retryable = false;
          break;
        case 'ACCESS_DENIED':
          message = 'You do not have permission to perform this action.';
          userFriendly = true;
          retryable = false;
          break;
        case 'RESOURCE_NOT_FOUND':
          message = 'The requested item was not found.';
          userFriendly = true;
          retryable = false;
          break;
        case 'DUPLICATE_RESOURCE':
          message = 'This item already exists.';
          userFriendly = true;
          retryable = false;
          break;
      }
    }

    return {
      message,
      code: errorCode as string,
      statusCode: statusCode as number,
      details: (error as { details?: unknown }).details,
      timestamp: Date.now(),
      context,
      retryable,
      userFriendly,
    };
  }

  /**
   * Check if an error is an API error
   */
  private isApiError(error: unknown): boolean {
    if (!error || typeof error !== 'object') return false;
    const errorObj = error as Record<string, unknown>;
    return (
      typeof errorObj.status === 'number' ||
      typeof errorObj.statusCode === 'number' ||
      !!errorObj.code ||
      !!errorObj.response
    );
  }

  /**
   * Determine if an error is retryable
   */
  private isRetryableError(error: Error): boolean {
    const nonRetryablePatterns = [
      /401/,
      /403/,
      /404/,
      /400/,
      /422/,
      /authentication/i,
      /access denied/i,
      /validation/i,
      /bad request/i,
      /not found/i,
    ];

    const message = error.message.toLowerCase();
    return !nonRetryablePatterns.some(pattern => pattern.test(message));
  }

  /**
   * Add error to history
   */
  private addToHistory(error: ErrorDetails): void {
    this.errorHistory.unshift(error);
    if (this.errorHistory.length > this.maxHistorySize) {
      this.errorHistory = this.errorHistory.slice(0, this.maxHistorySize);
    }
  }

  /**
   * Log error with appropriate level
   */
  private logError(error: ErrorDetails): void {
    const logData = {
      ...error,
      userAgent: typeof window !== 'undefined' ? window.navigator.userAgent : 'server',
      url: typeof window !== 'undefined' ? window.location.href : 'server',
    };

    if (error.statusCode && error.statusCode >= 500) {
      console.error('Server Error:', logData);
    } else if (error.statusCode && error.statusCode >= 400) {
      console.warn('Client Error:', logData);
    } else {
      console.error('Application Error:', logData);
    }

    // In production, send to error reporting service
    if (process.env.NODE_ENV === 'production') {
      this.sendToErrorReporting(logData);
    }
  }

  /**
   * Send error to external error reporting service
   */
  private sendToErrorReporting(error: unknown): void {
    // Implement integration with error reporting service (e.g., Sentry, LogRocket)
    // This is a placeholder for the actual implementation
    console.log('Would send to error reporting service:', error);
  }

  /**
   * Get error history
   */
  getErrorHistory(): ErrorDetails[] {
    return [...this.errorHistory];
  }

  /**
   * Clear error history
   */
  clearErrorHistory(): void {
    this.errorHistory = [];
  }

  /**
   * Get user-friendly error message
   */
  getUserFriendlyMessage(error: unknown, context?: string): string {
    const errorDetails = this.processError(error, { context, logError: false });
    return errorDetails.userFriendly ? errorDetails.message : 'An unexpected error occurred';
  }

  /**
   * Check if error should be retried
   */
  shouldRetry(error: unknown): boolean {
    const errorDetails = this.processError(error, { logError: false });
    return errorDetails.retryable;
  }
}

// Export singleton instance
export const errorHandlingService = ErrorHandlingService.getInstance();

// Convenience functions
export const processError = (error: unknown, options?: ErrorHandlingOptions) =>
  errorHandlingService.processError(error, options);

export const getUserFriendlyMessage = (error: unknown, context?: string) =>
  errorHandlingService.getUserFriendlyMessage(error, context);

export const shouldRetryError = (error: unknown) =>
  errorHandlingService.shouldRetry(error);

export const getErrorHistory = () =>
  errorHandlingService.getErrorHistory();

export const clearErrorHistory = () =>
  errorHandlingService.clearErrorHistory();
'use client';

import React from 'react';
import { ErrorBoundaryState } from '@/lib/types';
import Button from './Button';
import Card from './Card';
import Alert from './Alert';

interface ErrorBoundaryProps {
  children: React.ReactNode;
  fallback?: React.ComponentType<{ error: Error; retry: () => void; errorId: string }>;
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
  showErrorDetails?: boolean;
  level?: 'page' | 'component' | 'global';
}

interface EnhancedErrorBoundaryState extends ErrorBoundaryState {
  errorId: string;
  errorInfo?: React.ErrorInfo;
  retryCount: number;
}

class ErrorBoundary extends React.Component<ErrorBoundaryProps, EnhancedErrorBoundaryState> {
  private maxRetries = 3;

  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { 
      hasError: false, 
      errorId: '',
      retryCount: 0,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<EnhancedErrorBoundaryState> {
    const errorId = `error_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    return { 
      hasError: true, 
      error,
      errorId,
    };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    this.setState({ errorInfo });

    // Call custom error handler
    this.props.onError?.(error, errorInfo);

    // Log error details for debugging
    const errorDetails = {
      message: error.message,
      stack: error.stack,
      componentStack: errorInfo.componentStack,
      errorBoundary: this.constructor.name,
      level: this.props.level || 'component',
      timestamp: new Date().toISOString(),
      errorId: this.state.errorId,
    };

    console.error('Error details:', errorDetails);

    // In production, you might want to send this to an error reporting service
    if (process.env.NODE_ENV === 'production') {
      // Example: sendErrorToService(errorDetails);
    }
  }

  retry = () => {
    if (this.state.retryCount < this.maxRetries) {
      this.setState(prevState => ({ 
        hasError: false, 
        error: undefined,
        errorInfo: undefined,
        retryCount: prevState.retryCount + 1,
      }));
    }
  };

  reset = () => {
    this.setState({ 
      hasError: false, 
      error: undefined,
      errorInfo: undefined,
      retryCount: 0,
      errorId: '',
    });
  };

  render() {
    if (this.state.hasError) {
      const { error, errorId, retryCount, errorInfo } = this.state;
      const { fallback: FallbackComponent, showErrorDetails = false, level = 'component' } = this.props;
      const canRetry = retryCount < this.maxRetries;

      if (FallbackComponent) {
        return <FallbackComponent error={error!} retry={this.retry} errorId={errorId} />;
      }

      // Different UI based on error level
      if (level === 'global') {
        return (
          <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <Card className="max-w-lg mx-auto">
              <div className="text-center">
                <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-red-100 mb-6">
                  <svg
                    className="h-8 w-8 text-red-600"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"
                    />
                  </svg>
                </div>
                <h1 className="text-2xl font-bold text-gray-900 mb-2">
                  Application Error
                </h1>
                <p className="text-gray-600 mb-6">
                  The application encountered an unexpected error and needs to be restarted.
                </p>
                <div className="space-y-3">
                  <Button onClick={() => window.location.reload()} variant="primary" className="w-full">
                    Reload Application
                  </Button>
                  {showErrorDetails && (
                    <details className="text-left">
                      <summary className="cursor-pointer text-sm text-gray-500 hover:text-gray-700">
                        Show error details
                      </summary>
                      <div className="mt-2 p-3 bg-gray-100 rounded text-xs font-mono text-gray-800">
                        <p><strong>Error ID:</strong> {errorId}</p>
                        <p><strong>Message:</strong> {error?.message}</p>
                        {error?.stack && (
                          <div className="mt-2">
                            <strong>Stack trace:</strong>
                            <pre className="whitespace-pre-wrap">{error.stack}</pre>
                          </div>
                        )}
                      </div>
                    </details>
                  )}
                </div>
              </div>
            </Card>
          </div>
        );
      }

      if (level === 'page') {
        return (
          <div className="max-w-2xl mx-auto mt-8">
            <Alert
              type="error"
              message="This page encountered an error and couldn't load properly."
            />
            <Card className="mt-4">
              <div className="text-center">
                <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
                  <svg
                    className="h-6 w-6 text-red-600"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"
                    />
                  </svg>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  Page Error
                </h3>
                <p className="text-sm text-gray-500 mb-4">
                  {error?.message || 'An unexpected error occurred while loading this page'}
                </p>
                <div className="flex justify-center space-x-3">
                  {canRetry && (
                    <Button onClick={this.retry} variant="primary">
                      Try Again {retryCount > 0 && `(${this.maxRetries - retryCount} left)`}
                    </Button>
                  )}
                  <Button onClick={() => window.history.back()} variant="secondary">
                    Go Back
                  </Button>
                </div>
              </div>
            </Card>
          </div>
        );
      }

      // Component level error (default)
      return (
        <Card className="max-w-md mx-auto mt-4 border-red-200">
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-10 w-10 rounded-full bg-red-100 mb-3">
              <svg
                className="h-5 w-5 text-red-600"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"
                />
              </svg>
            </div>
            <h3 className="text-base font-medium text-gray-900 mb-2">
              Component Error
            </h3>
            <p className="text-sm text-gray-500 mb-4">
              {error?.message || 'This component encountered an error'}
            </p>
            {canRetry && (
              <Button onClick={this.retry} variant="primary" size="sm">
                Retry {retryCount > 0 && `(${this.maxRetries - retryCount} left)`}
              </Button>
            )}
            {!canRetry && (
              <p className="text-xs text-gray-400">
                Maximum retry attempts reached. Please refresh the page.
              </p>
            )}
          </div>
        </Card>
      );
    }

    return this.props.children;
  }
}

// Hook-based error boundary for functional components
export const useErrorHandler = () => {
  const [error, setError] = React.useState<Error | null>(null);

  const resetError = React.useCallback(() => {
    setError(null);
  }, []);

  const handleError = React.useCallback((error: Error) => {
    console.error('Error caught by useErrorHandler:', error);
    setError(error);
  }, []);

  React.useEffect(() => {
    if (error) {
      throw error;
    }
  }, [error]);

  return { handleError, resetError };
};

// Wrapper component for easier error boundary usage
export const withErrorBoundary = <P extends object>(
  Component: React.ComponentType<P>,
  errorBoundaryProps?: Omit<ErrorBoundaryProps, 'children'>
) => {
  const WrappedComponent = (props: P) => (
    <ErrorBoundary {...errorBoundaryProps}>
      <Component {...props} />
    </ErrorBoundary>
  );

  WrappedComponent.displayName = `withErrorBoundary(${Component.displayName || Component.name})`;
  return WrappedComponent;
};

export default ErrorBoundary;
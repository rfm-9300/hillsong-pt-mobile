'use client';

import React, { createContext, useContext, useCallback, useState } from 'react';
import { useGlobalErrorHandler } from '@/app/hooks/useErrorHandling';
import Alert from '@/app/components/ui/Alert';

interface ErrorContextType {
  showError: (message: string, options?: { type?: 'error' | 'warning'; duration?: number }) => void;
  showSuccess: (message: string, options?: { duration?: number }) => void;
  clearNotifications: () => void;
  handleError: (error: unknown, context?: string) => void;
}

interface Notification {
  id: string;
  type: 'error' | 'success' | 'warning';
  message: string;
  timestamp: number;
  duration?: number;
}

const ErrorContext = createContext<ErrorContextType | undefined>(undefined);

export const useErrorContext = () => {
  const context = useContext(ErrorContext);
  if (!context) {
    throw new Error('useErrorContext must be used within an ErrorProvider');
  }
  return context;
};

interface ErrorProviderProps {
  children: React.ReactNode;
  maxNotifications?: number;
  defaultDuration?: number;
}

export const ErrorProvider: React.FC<ErrorProviderProps> = ({
  children,
  maxNotifications = 5,
  defaultDuration = 5000,
}) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const { handleError: globalHandleError } = useGlobalErrorHandler();

  const removeNotification = useCallback((id: string) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  }, []);

  const addNotification = useCallback((notification: Omit<Notification, 'id' | 'timestamp'>) => {
    const id = `notification_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const newNotification: Notification = {
      ...notification,
      id,
      timestamp: Date.now(),
    };

    setNotifications(prev => {
      const updated = [newNotification, ...prev].slice(0, maxNotifications);
      return updated;
    });

    // Auto-remove notification after duration
    const duration = notification.duration ?? defaultDuration;
    if (duration > 0) {
      setTimeout(() => {
        removeNotification(id);
      }, duration);
    }
  }, [maxNotifications, defaultDuration, removeNotification]);

  const showError = useCallback((message: string, options?: { type?: 'error' | 'warning'; duration?: number }) => {
    addNotification({
      type: options?.type || 'error',
      message,
      duration: options?.duration,
    });
  }, [addNotification]);

  const showSuccess = useCallback((message: string, options?: { duration?: number }) => {
    addNotification({
      type: 'success',
      message,
      duration: options?.duration,
    });
  }, [addNotification]);

  const clearNotifications = useCallback(() => {
    setNotifications([]);
  }, []);

  const handleError = useCallback((error: unknown, context?: string) => {
    // Use global error handler for logging and processing
    globalHandleError(error, context);
    
    // Show user-friendly notification
    let message = 'An unexpected error occurred';
    if (error instanceof Error) {
      message = error.message;
    } else if (typeof error === 'string') {
      message = error;
    }

    if (context) {
      message = `${context}: ${message}`;
    }

    showError(message);
  }, [globalHandleError, showError]);

  const contextValue: ErrorContextType = {
    showError,
    showSuccess,
    clearNotifications,
    handleError,
  };

  return (
    <ErrorContext.Provider value={contextValue}>
      {children}
      
      {/* Notification container */}
      <div className="fixed top-4 right-4 z-50 space-y-2 max-w-sm">
        {notifications.map((notification) => (
          <Alert
            key={notification.id}
            type={notification.type}
            message={notification.message}
            onClose={() => removeNotification(notification.id)}
            className="shadow-lg"
          />
        ))}
      </div>
    </ErrorContext.Provider>
  );
};

// HOC for wrapping components with error handling
export const withErrorHandling = <P extends object>(
  Component: React.ComponentType<P>
) => {
  const WrappedComponent = (props: P) => {
    const { handleError } = useErrorContext();

    // Add error handling props to the component
    const enhancedProps = {
      ...props,
      onError: handleError,
    } as P & { onError: (error: unknown, context?: string) => void };

    return <Component {...enhancedProps} />;
  };

  WrappedComponent.displayName = `withErrorHandling(${Component.displayName || Component.name})`;
  return WrappedComponent;
};

export default ErrorProvider;
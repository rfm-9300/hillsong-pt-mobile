'use client';

import React from 'react';
import { LoadingOverlayProps } from '@/lib/types';
import { cn } from '@/lib/utils';

interface EnhancedLoadingOverlayProps extends LoadingOverlayProps {
  type?: 'overlay' | 'inline' | 'page';
  size?: 'sm' | 'md' | 'lg';
  progress?: number; // 0-100 for progress bar
  showProgress?: boolean;
  cancelable?: boolean;
  onCancel?: () => void;
  timeout?: number; // Auto-hide after timeout (ms)
  onTimeout?: () => void;
}

const LoadingOverlay: React.FC<EnhancedLoadingOverlayProps> = ({
  show,
  message = 'Loading...',
  type = 'overlay',
  size = 'md',
  progress,
  showProgress = false,
  cancelable = false,
  onCancel,
  timeout,
  onTimeout,
}) => {
  const [timeoutReached, setTimeoutReached] = React.useState(false);

  React.useEffect(() => {
    if (show && timeout) {
      const timer = setTimeout(() => {
        setTimeoutReached(true);
        onTimeout?.();
      }, timeout);

      return () => clearTimeout(timer);
    }
  }, [show, timeout, onTimeout]);

  React.useEffect(() => {
    if (!show) {
      setTimeoutReached(false);
    }
  }, [show]);

  if (!show) return null;

  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-6 w-6',
    lg: 'h-8 w-8',
  };

  const LoadingSpinner = () => (
    <svg
      className={cn(
        'animate-spin text-blue-600',
        sizeClasses[size]
      )}
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle
        className="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="4"
      />
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
      />
    </svg>
  );

  const ProgressBar = () => {
    if (!showProgress || progress === undefined) return null;
    
    return (
      <div className="w-full bg-gray-200 rounded-full h-2 mt-3">
        <div
          className="bg-blue-600 h-2 rounded-full transition-all duration-300"
          style={{ width: `${Math.min(100, Math.max(0, progress))}%` }}
        />
        <p className="text-xs text-gray-500 mt-1 text-center">
          {Math.round(progress)}%
        </p>
      </div>
    );
  };

  const TimeoutWarning = () => {
    if (!timeoutReached) return null;
    
    return (
      <div className="mt-3 p-2 bg-yellow-50 border border-yellow-200 rounded text-xs text-yellow-800">
        This is taking longer than expected. You can continue waiting or cancel the operation.
      </div>
    );
  };

  const CancelButton = () => {
    if (!cancelable || !onCancel) return null;
    
    return (
      <button
        onClick={onCancel}
        className="mt-3 text-sm text-gray-500 hover:text-gray-700 underline"
      >
        Cancel
      </button>
    );
  };

  if (type === 'inline') {
    return (
      <div className="flex items-center justify-center p-4">
        <div className="flex items-center space-x-3">
          <LoadingSpinner />
          <span className="text-gray-600">{message}</span>
        </div>
      </div>
    );
  }

  if (type === 'page') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-4 text-gray-600 font-medium">{message}</p>
          <ProgressBar />
          <TimeoutWarning />
          <CancelButton />
        </div>
      </div>
    );
  }

  // Default overlay type
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-lg p-6 shadow-xl max-w-sm w-full mx-4">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-4 text-gray-900 font-medium">{message}</p>
          <ProgressBar />
          <TimeoutWarning />
          <CancelButton />
        </div>
      </div>
    </div>
  );
};

// Inline loading spinner component
export const LoadingSpinner: React.FC<{ 
  size?: 'sm' | 'md' | 'lg'; 
  className?: string;
  color?: string;
}> = ({
  size = 'md',
  className,
  color = 'text-blue-600',
}) => {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-6 w-6',
    lg: 'h-8 w-8',
  };

  return (
    <svg
      className={cn(
        'animate-spin',
        color,
        sizeClasses[size],
        className
      )}
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle
        className="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="4"
      />
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
      />
    </svg>
  );
};

// Loading skeleton component
export const LoadingSkeleton: React.FC<{ 
  className?: string;
  lines?: number;
  avatar?: boolean;
}> = ({ 
  className,
  lines = 1,
  avatar = false,
}) => {
  return (
    <div className={cn('animate-pulse', className)}>
      {avatar && (
        <div className="flex items-center space-x-3 mb-3">
          <div className="h-10 w-10 bg-gray-200 rounded-full" />
          <div className="flex-1">
            <div className="h-4 bg-gray-200 rounded w-1/4 mb-2" />
            <div className="h-3 bg-gray-200 rounded w-1/3" />
          </div>
        </div>
      )}
      {Array.from({ length: lines }).map((_, index) => (
        <div
          key={index}
          className={cn(
            'h-4 bg-gray-200 rounded mb-2',
            index === lines - 1 && 'mb-0',
            index === lines - 1 && lines > 1 && 'w-3/4'
          )}
        />
      ))}
    </div>
  );
};

// Loading dots component
export const LoadingDots: React.FC<{ className?: string }> = ({ className }) => {
  return (
    <div className={cn('flex space-x-1', className)}>
      {[0, 1, 2].map((index) => (
        <div
          key={index}
          className="w-2 h-2 bg-blue-600 rounded-full animate-bounce"
          style={{ animationDelay: `${index * 0.1}s` }}
        />
      ))}
    </div>
  );
};

export default LoadingOverlay;
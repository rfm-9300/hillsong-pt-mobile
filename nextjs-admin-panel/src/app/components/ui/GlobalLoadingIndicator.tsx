'use client';

import React, { useState, useEffect } from 'react';
import { subscribeToAllLoading, LoadingState } from '@/lib/loadingStateService';
import { LoadingSpinner } from './LoadingOverlay';
import { cn } from '@/lib/utils';

interface GlobalLoadingIndicatorProps {
  position?: 'top' | 'bottom' | 'top-right' | 'top-left';
  showProgress?: boolean;
  showMessage?: boolean;
  className?: string;
}

const GlobalLoadingIndicator: React.FC<GlobalLoadingIndicatorProps> = ({
  position = 'top',
  showProgress = false,
  showMessage = false,
  className,
}) => {
  const [loadingStates, setLoadingStates] = useState<Record<string, LoadingState>>({});
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const unsubscribe = subscribeToAllLoading((states) => {
      setLoadingStates(states);
      
      // Check if any loading state is active
      const hasActiveLoading = Object.values(states).some(state => state.isLoading);
      setIsVisible(hasActiveLoading);
    });

    return unsubscribe;
  }, []);

  // Calculate overall progress if showing progress
  const overallProgress = React.useMemo(() => {
    if (!showProgress) return undefined;

    const loadingOperations = Object.values(loadingStates).filter(state => state.isLoading);
    if (loadingOperations.length === 0) return 100;

    const totalProgress = loadingOperations.reduce((sum, state) => {
      return sum + (state.progress || 0);
    }, 0);

    return Math.round(totalProgress / loadingOperations.length);
  }, [loadingStates, showProgress]);

  // Get current loading message
  const currentMessage = React.useMemo(() => {
    if (!showMessage) return undefined;

    const loadingOperations = Object.values(loadingStates).filter(state => state.isLoading && state.message);
    if (loadingOperations.length === 0) return 'Loading...';

    // Return the most recent message
    const mostRecent = loadingOperations.reduce((latest, current) => {
      return (current.startTime || 0) > (latest.startTime || 0) ? current : latest;
    });

    return mostRecent.message || 'Loading...';
  }, [loadingStates, showMessage]);

  if (!isVisible) return null;

  const positionClasses = {
    top: 'top-0 left-0 right-0',
    bottom: 'bottom-0 left-0 right-0',
    'top-right': 'top-4 right-4',
    'top-left': 'top-4 left-4',
  };

  const containerClasses = {
    top: 'w-full bg-blue-600 text-white shadow-lg',
    bottom: 'w-full bg-blue-600 text-white shadow-lg',
    'top-right': 'bg-white rounded-lg shadow-lg border max-w-sm',
    'top-left': 'bg-white rounded-lg shadow-lg border max-w-sm',
  };

  const contentClasses = {
    top: 'px-4 py-2',
    bottom: 'px-4 py-2',
    'top-right': 'p-4',
    'top-left': 'p-4',
  };

  return (
    <div
      className={cn(
        'fixed z-50 transition-all duration-300',
        positionClasses[position],
        className
      )}
    >
      <div className={cn(containerClasses[position])}>
        <div className={cn('flex items-center space-x-3', contentClasses[position])}>
          <LoadingSpinner 
            size="sm" 
            color={position.includes('top-') ? 'text-blue-600' : 'text-white'} 
          />
          
          <div className="flex-1 min-w-0">
            {showMessage && (
              <p className={cn(
                'text-sm font-medium truncate',
                position.includes('top-') ? 'text-gray-900' : 'text-white'
              )}>
                {currentMessage}
              </p>
            )}
            
            {showProgress && overallProgress !== undefined && (
              <div className="mt-1">
                <div className={cn(
                  'w-full rounded-full h-1.5',
                  position.includes('top-') ? 'bg-gray-200' : 'bg-blue-500'
                )}>
                  <div
                    className={cn(
                      'h-1.5 rounded-full transition-all duration-300',
                      position.includes('top-') ? 'bg-blue-600' : 'bg-white'
                    )}
                    style={{ width: `${overallProgress}%` }}
                  />
                </div>
                {showMessage && (
                  <p className={cn(
                    'text-xs mt-1',
                    position.includes('top-') ? 'text-gray-500' : 'text-blue-100'
                  )}>
                    {overallProgress}% complete
                  </p>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// Compact version for minimal UI impact
export const CompactGlobalLoadingIndicator: React.FC<{
  className?: string;
}> = ({ className }) => {
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const unsubscribe = subscribeToAllLoading((states) => {
      const hasActiveLoading = Object.values(states).some(state => state.isLoading);
      setIsLoading(hasActiveLoading);
    });

    return unsubscribe;
  }, []);

  if (!isLoading) return null;

  return (
    <div className={cn('fixed top-0 left-0 right-0 z-50', className)}>
      <div className="h-1 bg-blue-600 animate-pulse" />
    </div>
  );
};

// Loading indicator with operation count
export const DetailedGlobalLoadingIndicator: React.FC<{
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left';
  className?: string;
}> = ({ 
  position = 'top-right',
  className 
}) => {
  const [loadingStates, setLoadingStates] = useState<Record<string, LoadingState>>({});

  useEffect(() => {
    const unsubscribe = subscribeToAllLoading((states) => {
      setLoadingStates(states);
    });

    return unsubscribe;
  }, []);

  const activeOperations = Object.values(loadingStates).filter(state => state.isLoading);
  
  if (activeOperations.length === 0) return null;

  const positionClasses = {
    'top-right': 'top-4 right-4',
    'top-left': 'top-4 left-4',
    'bottom-right': 'bottom-4 right-4',
    'bottom-left': 'bottom-4 left-4',
  };

  return (
    <div
      className={cn(
        'fixed z-50 bg-white rounded-lg shadow-lg border p-3 max-w-xs',
        positionClasses[position],
        className
      )}
    >
      <div className="flex items-center space-x-2">
        <LoadingSpinner size="sm" />
        <div>
          <p className="text-sm font-medium text-gray-900">
            {activeOperations.length} operation{activeOperations.length !== 1 ? 's' : ''} in progress
          </p>
          {activeOperations.length === 1 && activeOperations[0].message && (
            <p className="text-xs text-gray-500 truncate">
              {activeOperations[0].message}
            </p>
          )}
        </div>
      </div>
    </div>
  );
};

export default GlobalLoadingIndicator;
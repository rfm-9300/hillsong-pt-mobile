'use client';

import React, { useState } from 'react';
import Button from './Button';
import { cn } from '@/lib/utils';

interface RetryButtonProps {
  onRetry: () => Promise<void> | void;
  maxRetries?: number;
  retryDelay?: number;
  disabled?: boolean;
  className?: string;
  children?: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  showRetryCount?: boolean;
  autoRetry?: boolean;
  exponentialBackoff?: boolean;
}

const RetryButton: React.FC<RetryButtonProps> = ({
  onRetry,
  maxRetries = 3,
  retryDelay = 1000,
  disabled = false,
  className,
  children = 'Retry',
  variant = 'primary',
  size = 'md',
  showRetryCount = true,
  autoRetry = false,
  exponentialBackoff = true,
}) => {
  const [retryCount, setRetryCount] = useState(0);
  const [isRetrying, setIsRetrying] = useState(false);
  const [countdown, setCountdown] = useState(0);

  const canRetry = retryCount < maxRetries && !disabled;

  const handleRetry = async () => {
    if (!canRetry || isRetrying) return;

    setIsRetrying(true);
    
    try {
      await onRetry();
      // Reset retry count on success
      setRetryCount(0);
    } catch (error) {
      console.error('Retry failed:', error);
      setRetryCount(prev => prev + 1);
      
      // Auto retry with delay if enabled and retries remaining
      if (autoRetry && retryCount + 1 < maxRetries) {
        const delay = exponentialBackoff 
          ? retryDelay * Math.pow(2, retryCount)
          : retryDelay;
        
        setCountdown(Math.ceil(delay / 1000));
        
        const countdownInterval = setInterval(() => {
          setCountdown(prev => {
            if (prev <= 1) {
              clearInterval(countdownInterval);
              handleRetry();
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      }
    } finally {
      setIsRetrying(false);
    }
  };

  const getButtonText = () => {
    if (countdown > 0) {
      return `Retrying in ${countdown}s...`;
    }
    
    if (isRetrying) {
      return 'Retrying...';
    }
    
    if (!canRetry && retryCount >= maxRetries) {
      return 'Max retries reached';
    }
    
    let text = children;
    if (showRetryCount && retryCount > 0) {
      text = `${children} (${maxRetries - retryCount} left)`;
    }
    
    return text;
  };

  return (
    <div className={cn('flex flex-col items-center space-y-2', className)}>
      <Button
        onClick={handleRetry}
        disabled={!canRetry || isRetrying || countdown > 0}
        loading={isRetrying}
        variant={variant}
        size={size}
      >
        {getButtonText()}
      </Button>
      
      {retryCount > 0 && retryCount < maxRetries && (
        <p className="text-xs text-gray-500">
          Attempt {retryCount} of {maxRetries}
        </p>
      )}
      
      {retryCount >= maxRetries && (
        <p className="text-xs text-red-500">
          Maximum retry attempts reached
        </p>
      )}
    </div>
  );
};

export default RetryButton;
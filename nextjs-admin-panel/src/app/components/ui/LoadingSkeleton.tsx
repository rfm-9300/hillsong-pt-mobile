'use client';

import React from 'react';
import { cn } from '@/lib/utils';

interface LoadingSkeletonProps {
  className?: string;
  variant?: 'text' | 'circular' | 'rectangular';
  width?: string | number;
  height?: string | number;
  lines?: number;
  style?: React.CSSProperties;
}

const LoadingSkeleton: React.FC<LoadingSkeletonProps> = ({
  className,
  variant = 'rectangular',
  width,
  height,
  lines = 1,
  style,
}) => {
  const baseClasses = 'skeleton-shimmer bg-gray-200';
  
  const variantClasses = {
    text: 'rounded',
    circular: 'rounded-full',
    rectangular: 'rounded',
  };

  const computedStyle: React.CSSProperties = { ...style };
  if (width) computedStyle.width = typeof width === 'number' ? `${width}px` : width;
  if (height) computedStyle.height = typeof height === 'number' ? `${height}px` : height;

  if (variant === 'text' && lines > 1) {
    return (
      <div className={className}>
        {Array.from({ length: lines }).map((_, index) => (
          <div
            key={index}
            className={cn(
              baseClasses,
              variantClasses[variant],
              index === lines - 1 ? 'w-3/4' : 'w-full',
              'h-4 mb-2 last:mb-0'
            )}
            style={computedStyle}
          />
        ))}
      </div>
    );
  }

  return (
    <div
      className={cn(
        baseClasses,
        variantClasses[variant],
        className
      )}
      style={computedStyle}
    />
  );
};

// Specific skeleton components for dashboard
export const StatCardSkeleton: React.FC<{ className?: string }> = ({ className }) => (
  <div className={cn('bg-white p-6 rounded-lg shadow-md', className)}>
    <LoadingSkeleton variant="text" className="mb-4" height={24} />
    <LoadingSkeleton variant="text" height={48} width="60%" />
  </div>
);

export const DashboardSkeleton: React.FC = () => (
  <div className="p-4">
    <LoadingSkeleton variant="text" className="mb-6" height={36} width="300px" />
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
      <StatCardSkeleton />
      <StatCardSkeleton />
      <StatCardSkeleton />
    </div>
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div className="bg-white p-6 rounded-lg shadow-md">
        <LoadingSkeleton variant="text" className="mb-4" height={24} width="200px" />
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="flex items-center space-x-3">
              <LoadingSkeleton variant="circular" width={40} height={40} />
              <div className="flex-1">
                <LoadingSkeleton variant="text" height={16} className="mb-1" />
                <LoadingSkeleton variant="text" height={14} width="60%" />
              </div>
            </div>
          ))}
        </div>
      </div>
      <div className="bg-white p-6 rounded-lg shadow-md">
        <LoadingSkeleton variant="text" className="mb-4" height={24} width="150px" />
        <div className="grid grid-cols-2 gap-4">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="text-center">
              <LoadingSkeleton variant="text" height={32} className="mb-2" />
              <LoadingSkeleton variant="text" height={16} />
            </div>
          ))}
        </div>
      </div>
    </div>
  </div>
);

export default LoadingSkeleton;
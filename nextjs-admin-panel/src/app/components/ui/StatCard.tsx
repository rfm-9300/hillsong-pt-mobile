'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Card from './Card';
import LoadingSkeleton from './LoadingSkeleton';
import { cn } from '@/lib/utils';

interface StatCardProps {
  title: string;
  value: number;
  loading?: boolean;
  error?: string;
  icon?: React.ReactNode;
  color?: 'blue' | 'green' | 'purple' | 'orange' | 'red';
  trend?: {
    value: number;
    isPositive: boolean;
  };
  onClick?: () => void;
  href?: string;
  className?: string;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  loading = false,
  error,
  icon,
  color = 'blue',
  trend,
  onClick,
  href,
  className,
}) => {
  const [displayValue, setDisplayValue] = useState(0);
  const router = useRouter();

  // Animated counter effect
  useEffect(() => {
    if (loading || error) return;

    const duration = 1000; // 1 second
    const steps = 60;
    const increment = value / steps;
    let current = 0;
    let step = 0;

    const timer = setInterval(() => {
      step++;
      current = Math.min(increment * step, value);
      setDisplayValue(Math.floor(current));

      if (step >= steps) {
        clearInterval(timer);
        setDisplayValue(value);
      }
    }, duration / steps);

    return () => clearInterval(timer);
  }, [value, loading, error]);

  const colorClasses = {
    blue: {
      text: 'text-blue-600',
      bg: 'bg-blue-50',
      icon: 'text-blue-500',
      border: 'border-blue-200',
    },
    green: {
      text: 'text-green-600',
      bg: 'bg-green-50',
      icon: 'text-green-500',
      border: 'border-green-200',
    },
    purple: {
      text: 'text-purple-600',
      bg: 'bg-purple-50',
      icon: 'text-purple-500',
      border: 'border-purple-200',
    },
    orange: {
      text: 'text-orange-600',
      bg: 'bg-orange-50',
      icon: 'text-orange-500',
      border: 'border-orange-200',
    },
    red: {
      text: 'text-red-600',
      bg: 'bg-red-50',
      icon: 'text-red-500',
      border: 'border-red-200',
    },
  };

  const colors = colorClasses[color];

  const handleClick = () => {
    if (href) {
      router.push(href);
    } else if (onClick) {
      onClick();
    }
  };

  const isClickable = !!(href || onClick);

  if (loading) {
    return (
      <Card className={cn('p-6', className)}>
        <div className="flex items-center">
          <div className="flex-1">
            <LoadingSkeleton variant="text" height={20} className="mb-2" />
            <LoadingSkeleton variant="text" height={32} width="60%" />
          </div>
          <LoadingSkeleton variant="circular" width={48} height={48} />
        </div>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className={cn('p-6 border-red-200', className)}>
        <div className="flex items-center">
          <div className="flex-1">
            <h3 className="text-sm font-medium text-gray-900 mb-1">{title}</h3>
            <p className="text-sm text-red-600">Error loading data</p>
          </div>
          <div className="flex-shrink-0">
            <div className="w-12 h-12 bg-red-50 rounded-lg flex items-center justify-center">
              <svg
                className="w-6 h-6 text-red-500"
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
          </div>
        </div>
      </Card>
    );
  }

  return (
    <Card
      className={cn(
        'p-6 transition-all duration-200',
        isClickable && 'hover:shadow-lg hover:scale-105 cursor-pointer',
        colors.border,
        className
      )}
      onClick={handleClick}
    >
      <div className="flex items-center">
        <div className="flex-1">
          <h3 className="text-sm font-medium text-gray-900 mb-1">{title}</h3>
          <div className="flex items-baseline">
            <p className={cn('text-3xl font-bold', colors.text)}>
              {displayValue.toLocaleString()}
            </p>
            {trend && (
              <span
                className={cn(
                  'ml-2 text-sm font-medium',
                  trend.isPositive ? 'text-green-600' : 'text-red-600'
                )}
              >
                {trend.isPositive ? '+' : ''}
                {trend.value}%
              </span>
            )}
          </div>
        </div>
        {icon && (
          <div className="flex-shrink-0">
            <div className={cn('w-12 h-12 rounded-lg flex items-center justify-center', colors.bg)}>
              <div className={cn('w-6 h-6', colors.icon)}>
                {icon}
              </div>
            </div>
          </div>
        )}
      </div>
    </Card>
  );
};

export default StatCard;
'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import Card from './Card';
import { cn } from '@/lib/utils';
import { AlertIcon } from '../icons/Icons';

interface StatCardProps {
  title: string;
  value: number | string;
  loading?: boolean;
  error?: string;
  icon?: React.ReactNode;
  color?: 'amber' | 'green' | 'blue' | 'red' | 'purple' | 'orange';
  trend?: string | { value: number; isPositive: boolean };
  onClick?: () => void;
  href?: string;
  className?: string;
}

const colorMap = {
  amber: 'bg-[var(--color-accent-sub)] text-[var(--color-accent)]',
  green: 'bg-[var(--color-success-bg)] text-[var(--color-success)]',
  blue: 'bg-[var(--color-info-bg)] text-[var(--color-info)]',
  red: 'bg-[var(--color-danger-bg)] text-[var(--color-danger)]',
  purple: 'bg-[var(--color-accent-sub)] text-[var(--color-accent)]',
  orange: 'bg-[var(--color-warning-bg)] text-[var(--color-warning)]',
};

const StatCard: React.FC<StatCardProps> = ({ title, value, loading, error, icon, color = 'amber', trend, onClick, href, className }) => {
  const router = useRouter();
  const clickable = Boolean(onClick || href);
  const trendLabel = typeof trend === 'string' ? trend : trend ? `${trend.isPositive ? '+' : ''}${trend.value}%` : undefined;

  const handleClick = () => {
    if (href) router.push(href);
    else onClick?.();
  };

  if (loading) {
    return (
      <Card className={cn('p-4 sm:p-[18px_20px]', className)}>
        <div className="h-3 w-24 rounded skeleton-shimmer" />
        <div className="mt-3 h-8 w-16 rounded skeleton-shimmer" />
      </Card>
    );
  }

  return (
    <Card
      className={cn(
        'p-4 shadow-[0_1px_3px_rgba(0,0,0,0.04)] transition-all duration-150 sm:p-[18px_20px]',
        clickable && 'cursor-pointer hover:border-[var(--color-border-med)] hover:shadow-[0_4px_16px_rgba(0,0,0,0.08)]',
        error && 'border-[rgba(220,38,38,0.2)]',
        className
      )}
      onClick={clickable ? handleClick : undefined}
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="mb-2.5 text-[12px] font-medium uppercase tracking-[0.3px] text-[var(--color-text-sub)]">{title}</p>
          {error ? (
            <p className="text-[13px] text-[var(--color-danger)]">Error loading</p>
          ) : (
            <p className="font-display text-[26px] leading-none text-[var(--color-text)] sm:text-[34px]">{typeof value === 'number' ? value.toLocaleString() : value}</p>
          )}
          {trendLabel && !error && <p className="mt-1.5 text-[11px] text-[var(--color-success)]">↑ {trendLabel}</p>}
        </div>
        <div className={cn('flex h-[38px] w-[38px] shrink-0 items-center justify-center rounded-[9px]', colorMap[color])}>
          {error ? <AlertIcon /> : icon}
        </div>
      </div>
    </Card>
  );
};

export default StatCard;

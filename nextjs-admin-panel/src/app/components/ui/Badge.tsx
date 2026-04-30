import React from 'react';
import { cn } from '@/lib/utils';

export type BadgeColor = 'neutral' | 'amber' | 'green' | 'red' | 'blue' | 'yellow';

const palettes: Record<BadgeColor, string> = {
  neutral: 'bg-[var(--color-surface-alt)] text-[var(--color-text-sub)] border-[var(--color-border)]',
  amber: 'bg-[var(--color-accent-sub)] text-[var(--color-accent)] border-[rgba(201,149,42,0.25)]',
  green: 'bg-[var(--color-success-bg)] text-[var(--color-success)] border-[rgba(22,163,74,0.2)]',
  red: 'bg-[var(--color-danger-bg)] text-[var(--color-danger)] border-[rgba(220,38,38,0.2)]',
  blue: 'bg-[var(--color-info-bg)] text-[var(--color-info)] border-[rgba(37,99,235,0.2)]',
  yellow: 'bg-[var(--color-warning-bg)] text-[var(--color-warning)] border-[rgba(217,119,6,0.2)]',
};

export default function Badge({
  color = 'neutral',
  size = 'sm',
  children,
  className,
}: {
  color?: BadgeColor;
  size?: 'xs' | 'sm';
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 whitespace-nowrap rounded border font-semibold tracking-[0.3px]',
        size === 'xs' ? 'px-1.5 py-px text-[10px]' : 'px-2 py-0.5 text-[11px]',
        palettes[color],
        className
      )}
    >
      {children}
    </span>
  );
}

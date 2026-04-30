'use client';

import React from 'react';
import { ButtonProps } from '@/lib/types';
import { cn } from '@/lib/utils';

const variants = {
  primary: 'bg-[var(--color-accent)] text-white border-transparent hover:bg-[var(--color-accent-hover)]',
  secondary: 'bg-[var(--color-surface)] text-[var(--color-text)] border-[var(--color-border-med)] hover:bg-[var(--color-surface-alt)]',
  ghost: 'bg-transparent text-[var(--color-text-sub)] border-transparent hover:bg-[var(--color-surface-alt)]',
  danger: 'bg-[var(--color-danger-bg)] text-[var(--color-danger)] border-[rgba(220,38,38,0.2)] hover:bg-[#FECACA]',
  outline: 'bg-transparent text-[var(--color-accent)] border-[var(--color-accent)] hover:bg-[var(--color-accent-sub)]',
};

const sizes = {
  xs: 'px-[10px] py-[6px] text-[11px] max-sm:min-h-[40px]',
  sm: 'px-3 py-[7px] text-[12px] max-sm:min-h-[40px]',
  md: 'px-[14px] py-2 text-[13px]',
  lg: 'px-5 py-[10px] text-[14px]',
};

const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  icon,
  loading = false,
  disabled = false,
  onClick,
  children,
  className,
  type = 'button',
}) => (
  <button
    type={type}
    className={cn(
      'inline-flex cursor-pointer items-center justify-center gap-1.5 rounded-[7px] border font-semibold transition-all duration-150 ease-out',
      'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--color-accent)]',
      'disabled:cursor-not-allowed disabled:opacity-50',
      variants[variant],
      sizes[size],
      className
    )}
    onClick={onClick}
    disabled={disabled || loading}
  >
    {loading ? (
      <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent" />
    ) : icon ? (
      <span className="shrink-0" aria-hidden="true">{icon}</span>
    ) : null}
    {children}
  </button>
);

export default Button;

'use client';

import React from 'react';
import { cn } from '@/lib/utils';

export default function Select({
  label,
  error,
  className,
  children,
  ...props
}: React.SelectHTMLAttributes<HTMLSelectElement> & { label?: string; error?: string }) {
  const generatedId = React.useId();
  const id = props.id || generatedId;
  return (
    <div className={cn('space-y-1.5', className)}>
      {label && <label htmlFor={id} className="block text-[12px] font-semibold uppercase tracking-[0.2px] text-[var(--color-text-sub)]">{label}</label>}
      <select
        id={id}
        className={cn(
          'min-h-[40px] w-full appearance-none rounded-[7px] border bg-[var(--color-surface)] px-3 py-2 pr-9 text-[16px] text-[var(--color-text)] outline-none transition-all duration-150 sm:text-[13px]',
          'border-[var(--color-border-med)] focus:border-[var(--color-accent)] focus:shadow-[0_0_0_3px_var(--color-accent-sub)]',
          error && 'border-[var(--color-danger)] shadow-[0_0_0_3px_var(--color-danger-bg)]'
        )}
        style={{
          backgroundImage: "url(\"data:image/svg+xml,%3Csvg width='14' height='14' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M6 9l6 6 6-6' stroke='%239CA3AF' stroke-width='1.7' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E\")",
          backgroundPosition: 'right 10px center',
          backgroundRepeat: 'no-repeat',
        }}
        {...props}
      >
        {children}
      </select>
      {error && <p className="text-[11px] text-[var(--color-danger)]">{error}</p>}
    </div>
  );
}

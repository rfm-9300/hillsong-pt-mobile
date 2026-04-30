'use client';

import React, { forwardRef } from 'react';
import { cn } from '@/lib/utils';

interface InputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'onChange' | 'value' | 'prefix'> {
  label?: string;
  value?: string;
  onChange?: (value: string) => void;
  error?: string;
  prefix?: React.ReactNode;
  showValidation?: boolean;
  successMessage?: string;
  isValidating?: boolean;
}

const Input = forwardRef<HTMLInputElement, InputProps>(({
  label,
  value,
  onChange,
  error,
  required = false,
  disabled = false,
  className,
  prefix,
  showValidation = true,
  successMessage,
  isValidating,
  id,
  ...props
}, ref) => {
  const generatedId = React.useId();
  const inputId = id || generatedId;
  const hasError = Boolean(error && showValidation);

  return (
    <div className={cn('space-y-1.5', className)}>
      {label && (
        <label htmlFor={inputId} className="block text-[12px] font-semibold uppercase tracking-[0.2px] text-[var(--color-text-sub)]">
          {label}{required && <span className="ml-1 text-[var(--color-danger)]">*</span>}
        </label>
      )}
      <div
        className={cn(
          'relative flex items-center rounded-[7px] border bg-[var(--color-surface)] transition-all duration-150',
          hasError
            ? 'border-[var(--color-danger)] shadow-[0_0_0_3px_var(--color-danger-bg)]'
            : 'border-[var(--color-border-med)] focus-within:border-[var(--color-accent)] focus-within:shadow-[0_0_0_3px_var(--color-accent-sub)]',
          disabled && 'bg-[var(--color-surface-alt)] opacity-70'
        )}
      >
        {prefix && <span className="ml-3 text-[var(--color-text-muted)]">{prefix}</span>}
        <input
          ref={ref}
          id={inputId}
          value={value || ''}
          onChange={(event) => onChange?.(event.target.value)}
          disabled={disabled}
          required={required}
          className={cn(
            'min-h-[40px] w-full rounded-[7px] bg-transparent px-3 py-2 text-[16px] text-[var(--color-text)] outline-none placeholder:text-[var(--color-text-muted)] sm:text-[13px]',
            prefix && 'pl-2'
          )}
          aria-invalid={hasError}
          aria-describedby={hasError ? `${inputId}-error` : undefined}
          {...props}
        />
        {isValidating && <span className="mr-3 h-3.5 w-3.5 animate-spin rounded-full border-2 border-[var(--color-accent)] border-t-transparent" />}
      </div>
      {hasError && <p id={`${inputId}-error`} className="text-[11px] text-[var(--color-danger)]">{error}</p>}
      {successMessage && !hasError && <p className="text-[11px] text-[var(--color-success)]">{successMessage}</p>}
    </div>
  );
});

Input.displayName = 'Input';

export default Input;

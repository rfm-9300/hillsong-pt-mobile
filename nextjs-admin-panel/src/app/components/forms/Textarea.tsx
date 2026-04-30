'use client';

import React, { forwardRef } from 'react';
import { cn } from '@/lib/utils';

interface TextareaProps extends Omit<React.TextareaHTMLAttributes<HTMLTextAreaElement>, 'onChange' | 'value'> {
  label?: string;
  value?: string;
  onChange?: (value: string) => void;
  error?: string;
  showValidation?: boolean;
  successMessage?: string;
  isValidating?: boolean;
  showCharCount?: boolean;
}

const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(({
  label,
  value,
  onChange,
  error,
  required = false,
  disabled = false,
  className,
  showValidation = true,
  successMessage,
  isValidating,
  showCharCount = false,
  id,
  rows = 4,
  maxLength,
  ...props
}, ref) => {
  const generatedId = React.useId();
  const textareaId = id || generatedId;
  const hasError = Boolean(error && showValidation);

  return (
    <div className={cn('space-y-1.5', className)}>
      {label && (
        <div className="flex items-center justify-between gap-3">
          <label htmlFor={textareaId} className="block text-[12px] font-semibold uppercase tracking-[0.2px] text-[var(--color-text-sub)]">
            {label}{required && <span className="ml-1 text-[var(--color-danger)]">*</span>}
          </label>
          {showCharCount && maxLength && <span className="text-[11px] text-[var(--color-text-muted)]">{(value || '').length}/{maxLength}</span>}
        </div>
      )}
      <div
        className={cn(
          'rounded-[7px] border bg-[var(--color-surface)] transition-all duration-150',
          hasError
            ? 'border-[var(--color-danger)] shadow-[0_0_0_3px_var(--color-danger-bg)]'
            : 'border-[var(--color-border-med)] focus-within:border-[var(--color-accent)] focus-within:shadow-[0_0_0_3px_var(--color-accent-sub)]',
          disabled && 'bg-[var(--color-surface-alt)] opacity-70'
        )}
      >
        <textarea
          ref={ref}
          id={textareaId}
          value={value || ''}
          onChange={(event) => onChange?.(event.target.value)}
          disabled={disabled}
          required={required}
          rows={rows}
          maxLength={maxLength}
          className="min-h-[96px] w-full resize-y rounded-[7px] bg-transparent px-3 py-2 text-[16px] text-[var(--color-text)] outline-none placeholder:text-[var(--color-text-muted)] sm:text-[13px]"
          aria-invalid={hasError}
          aria-describedby={hasError ? `${textareaId}-error` : undefined}
          {...props}
        />
      </div>
      {isValidating && <p className="text-[11px] text-[var(--color-text-muted)]">Validating...</p>}
      {hasError && <p id={`${textareaId}-error`} className="text-[11px] text-[var(--color-danger)]">{error}</p>}
      {successMessage && !hasError && <p className="text-[11px] text-[var(--color-success)]">{successMessage}</p>}
    </div>
  );
});

Textarea.displayName = 'Textarea';

export default Textarea;

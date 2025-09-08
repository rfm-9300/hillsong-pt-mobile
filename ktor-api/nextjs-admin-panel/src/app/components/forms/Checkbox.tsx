'use client';

import React from 'react';
import { CheckboxProps } from '@/lib/types';
import { cn } from '@/lib/utils';

const Checkbox: React.FC<CheckboxProps> = ({
  label,
  checked,
  onChange,
  error,
  disabled = false,
  className,
}) => {
  const checkboxId = React.useId();

  return (
    <div className={cn('space-y-1', className)}>
      <div className="flex items-center">
        <input
          id={checkboxId}
          type="checkbox"
          checked={checked}
          onChange={(e) => onChange(e.target.checked)}
          disabled={disabled}
          className={cn(
            'h-4 w-4 text-blue-600 border-gray-300 rounded',
            'focus:ring-2 focus:ring-blue-500',
            'disabled:opacity-50 disabled:cursor-not-allowed',
            error && 'border-red-300 focus:ring-red-500'
          )}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={error ? `${checkboxId}-error` : undefined}
        />
        {label && (
          <label
            htmlFor={checkboxId}
            className={cn(
              'ml-2 block text-sm text-gray-700',
              disabled && 'opacity-50 cursor-not-allowed'
            )}
          >
            {label}
          </label>
        )}
      </div>
      {error && (
        <p
          id={`${checkboxId}-error`}
          className="text-sm text-red-600"
          role="alert"
        >
          {error}
        </p>
      )}
    </div>
  );
};

export default Checkbox;
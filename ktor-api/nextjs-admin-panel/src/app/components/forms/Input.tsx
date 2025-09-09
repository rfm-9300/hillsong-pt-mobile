'use client';

import React, { forwardRef } from 'react';
import { InputProps } from '@/lib/types';
import { cn } from '@/lib/utils';

interface EnhancedInputProps extends Omit<InputProps, 'onChange'> {
  onChange?: (value: string) => void;
  onBlur?: () => void;
  name?: string;
  isValidating?: boolean;
  showValidation?: boolean;
  successMessage?: string;
}

const Input = forwardRef<HTMLInputElement, EnhancedInputProps>(({
  label,
  type = 'text',
  placeholder,
  value,
  onChange,
  onBlur,
  error,
  required = false,
  disabled = false,
  className,
  name,
  isValidating = false,
  showValidation = true,
  successMessage,
}, ref) => {
  const inputId = React.useId();
  const hasError = error && showValidation;
  const hasSuccess = successMessage && !error && value && showValidation;

  return (
    <div className={cn('space-y-1', className)}>
      {label && (
        <label
          htmlFor={inputId}
          className="block text-sm font-medium text-gray-700"
        >
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      <div className="relative">
        <input
          ref={ref}
          id={inputId}
          name={name}
          type={type}
          placeholder={placeholder}
          value={value || ''}
          onChange={(e) => onChange?.(e.target.value)}
          onBlur={onBlur}
          disabled={disabled}
          required={required}
          className={cn(
            'block w-full px-3 py-2 sm:py-3 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 touch-target',
            'focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500',
            'disabled:bg-gray-50 disabled:text-gray-500 disabled:cursor-not-allowed',
            'transition-all duration-200',
            hasError && 'border-red-300 focus:ring-red-500 focus:border-red-500',
            hasSuccess && 'border-green-300 focus:ring-green-500 focus:border-green-500',
            isValidating && 'pr-10'
          )}
          aria-invalid={hasError ? 'true' : 'false'}
          aria-describedby={
            hasError ? `${inputId}-error` : 
            hasSuccess ? `${inputId}-success` : 
            undefined
          }
        />
        
        {/* Validation indicator */}
        {showValidation && (
          <div className="absolute inset-y-0 right-0 flex items-center pr-3">
            {isValidating && (
              <div className="animate-spin h-4 w-4 border-2 border-blue-500 border-t-transparent rounded-full" />
            )}
            {hasError && !isValidating && (
              <svg className="h-4 w-4 text-red-500" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                  clipRule="evenodd"
                />
              </svg>
            )}
            {hasSuccess && !isValidating && (
              <svg className="h-4 w-4 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                  clipRule="evenodd"
                />
              </svg>
            )}
          </div>
        )}
      </div>
      
      {/* Error message */}
      {hasError && (
        <p
          id={`${inputId}-error`}
          className="text-sm text-red-600 flex items-center"
          role="alert"
        >
          <svg className="h-4 w-4 mr-1 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path
              fillRule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
              clipRule="evenodd"
            />
          </svg>
          {error}
        </p>
      )}
      
      {/* Success message */}
      {hasSuccess && (
        <p
          id={`${inputId}-success`}
          className="text-sm text-green-600 flex items-center"
        >
          <svg className="h-4 w-4 mr-1 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
              clipRule="evenodd"
            />
          </svg>
          {successMessage}
        </p>
      )}
    </div>
  );
});

Input.displayName = 'Input';

export default Input;
'use client';

import { useForm, UseFormProps, FieldValues, Path, UseFormReturn, Resolver } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useState, useCallback } from 'react';

// Generic form validation hook with Zod schema support
export function useFormValidation<T extends FieldValues>(
  schema: z.ZodSchema<T>,
  options?: UseFormProps<T>
): UseFormReturn<T> & {
  isSubmitting: boolean;
  submitError: string | null;
  submitSuccess: boolean;
  handleSubmit: (onSubmit: (data: T) => Promise<void> | void) => (e?: React.BaseSyntheticEvent) => Promise<void>;
  clearSubmitError: () => void;
  clearSubmitSuccess: () => void;
} {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  const form = useForm<T>({
    mode: 'onChange', // Real-time validation
    ...options,
  });

  const clearSubmitError = useCallback(() => {
    setSubmitError(null);
  }, []);

  const clearSubmitSuccess = useCallback(() => {
    setSubmitSuccess(false);
  }, []);

  const handleSubmit = useCallback((onSubmit: (data: T) => Promise<void> | void) => {
    return form.handleSubmit(async (data: T) => {
      setIsSubmitting(true);
      setSubmitError(null);
      setSubmitSuccess(false);

      try {
        await onSubmit(data);
        setSubmitSuccess(true);
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : 'An unexpected error occurred';
        setSubmitError(errorMessage);
      } finally {
        setIsSubmitting(false);
      }
    });
  }, [form]);

  return {
    ...form,
    isSubmitting,
    submitError,
    submitSuccess,
    handleSubmit: handleSubmit as typeof form.handleSubmit,
    clearSubmitError,
    clearSubmitSuccess,
  };
}

// Field-level validation hook for real-time feedback
export function useFieldValidation<T extends FieldValues>(
  form: UseFormReturn<T>,
  fieldName: Path<T>
) {
  const fieldState = form.getFieldState(fieldName, form.formState);
  const fieldValue = form.watch(fieldName);

  return {
    value: fieldValue,
    error: fieldState.error?.message,
    isDirty: fieldState.isDirty,
    isTouched: fieldState.isTouched,
    isValid: !fieldState.error,
    isValidating: fieldState.isValidating,
  };
}

// Common validation schemas
export const validationSchemas = {
  post: z.object({
    title: z.string()
      .min(1, 'Title is required')
      .max(200, 'Title must be less than 200 characters'),
    description: z.string()
      .min(1, 'Description is required')
      .max(500, 'Description must be less than 500 characters'),
    content: z.string()
      .min(1, 'Content is required')
      .max(10000, 'Content must be less than 10,000 characters'),
    imageUrl: z.string().url('Invalid image URL').optional().or(z.literal('')),
  }),

  event: z.object({
    title: z.string()
      .min(1, 'Title is required')
      .max(200, 'Title must be less than 200 characters'),
    description: z.string()
      .min(1, 'Description is required')
      .max(1000, 'Description must be less than 1,000 characters'),
    date: z.string()
      .min(1, 'Date is required')
      .refine((date) => {
        const selectedDate = new Date(date);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return selectedDate >= today;
      }, 'Event date must be today or in the future'),
    location: z.string()
      .min(1, 'Location is required')
      .max(200, 'Location must be less than 200 characters'),
    imageUrl: z.string().url('Invalid image URL').optional().or(z.literal('')),
  }),

  user: z.object({
    email: z.string()
      .min(1, 'Email is required')
      .email('Invalid email address'),
    firstName: z.string()
      .min(1, 'First name is required')
      .max(50, 'First name must be less than 50 characters'),
    lastName: z.string()
      .min(1, 'Last name is required')
      .max(50, 'Last name must be less than 50 characters'),
    isAdmin: z.boolean(),
  }),

  login: z.object({
    email: z.string()
      .min(1, 'Email is required')
      .email('Invalid email address'),
    password: z.string()
      .min(1, 'Password is required')
      .min(6, 'Password must be at least 6 characters'),
  }),

  attendance: z.object({
    notes: z.string()
      .max(500, 'Notes must be less than 500 characters')
      .optional(),
    status: z.enum(['CHECKED_IN', 'CHECKED_OUT', 'NO_SHOW', 'EMERGENCY'], {
      message: 'Status is required',
    }),
  }),
};

// Form field validation utilities
export const fieldValidators = {
  required: (message = 'This field is required') => z.string().min(1, message),
  email: (message = 'Invalid email address') => z.string().email(message),
  url: (message = 'Invalid URL') => z.string().url(message),
  minLength: (min: number, message?: string) => 
    z.string().min(min, message || `Must be at least ${min} characters`),
  maxLength: (max: number, message?: string) => 
    z.string().max(max, message || `Must be less than ${max} characters`),
  futureDate: (message = 'Date must be in the future') => 
    z.string().refine((date) => new Date(date) > new Date(), message),
  pastDate: (message = 'Date must be in the past') => 
    z.string().refine((date) => new Date(date) < new Date(), message),
};
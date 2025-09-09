'use client';

import React from 'react';
import { Controller, Control, FieldPath, FieldValues } from 'react-hook-form';
import Input from './Input';
import Textarea from './Textarea';
import Checkbox from './Checkbox';
import ImageUpload from './ImageUpload';

interface BaseFieldProps<T extends FieldValues> {
  control: Control<T>;
  name: FieldPath<T>;
  label?: string;
  placeholder?: string;
  required?: boolean;
  disabled?: boolean;
  className?: string;
}

interface InputFieldProps<T extends FieldValues> extends BaseFieldProps<T> {
  type: 'input';
  inputType?: string;
  successMessage?: string;
}

interface TextareaFieldProps<T extends FieldValues> extends BaseFieldProps<T> {
  type: 'textarea';
  rows?: number;
  maxLength?: number;
  showCharCount?: boolean;
  successMessage?: string;
}

interface CheckboxFieldProps<T extends FieldValues> extends BaseFieldProps<T> {
  type: 'checkbox';
}

interface ImageUploadFieldProps<T extends FieldValues> extends BaseFieldProps<T> {
  type: 'image';
  accept?: string;
}

type FormFieldProps<T extends FieldValues> = 
  | InputFieldProps<T>
  | TextareaFieldProps<T>
  | CheckboxFieldProps<T>
  | ImageUploadFieldProps<T>;

function FormField<T extends FieldValues>(props: FormFieldProps<T>) {
  const { control, name, type, label, required, disabled, className } = props;

  return (
    <Controller
      control={control}
      name={name}
      render={({ field, fieldState, formState }) => {
        const { onChange, onBlur, value, ref } = field;
        const { error, isDirty, isTouched } = fieldState;
        const { isValidating } = formState;

        const commonProps = {
          label,
          required,
          disabled,
          className,
          error: error?.message,
          onBlur,
          name,
          isValidating: isValidating,
          showValidation: isDirty || isTouched,
        };

        switch (type) {
          case 'input':
            return (
              <Input
                {...commonProps}
                ref={ref}
                type={(props as InputFieldProps<T>).inputType || 'text'}
                placeholder={(props as InputFieldProps<T>).placeholder}
                value={value || ''}
                onChange={onChange}
                successMessage={(props as InputFieldProps<T>).successMessage}
              />
            );

          case 'textarea':
            const textareaProps = props as TextareaFieldProps<T>;
            return (
              <Textarea
                {...commonProps}
                ref={ref}
                placeholder={textareaProps.placeholder}
                value={value || ''}
                onChange={onChange}
                rows={textareaProps.rows}
                maxLength={textareaProps.maxLength}
                showCharCount={textareaProps.showCharCount}
                successMessage={textareaProps.successMessage}
              />
            );

          case 'checkbox':
            return (
              <Checkbox
                {...commonProps}
                checked={value || false}
                onChange={onChange}
              />
            );

          case 'image':
            const imageProps = props as ImageUploadFieldProps<T>;
            return (
              <ImageUpload
                {...commonProps}
                value={value}
                onChange={onChange}
                accept={imageProps.accept}
              />
            );

          default:
            return <div>Unsupported field type: {type}</div>;
        }
      }}
    />
  );
}

export default FormField;
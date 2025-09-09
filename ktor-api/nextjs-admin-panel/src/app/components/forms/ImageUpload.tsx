'use client';

import React, { useRef, useState } from 'react';
import Image from 'next/image';
import { ImageUploadProps } from '@/lib/types';
import { cn } from '@/lib/utils';
import { formatFileSize } from '@/lib/utils';
import { MAX_FILE_SIZE, ALLOWED_IMAGE_TYPES } from '@/lib/constants';

const ImageUpload: React.FC<ImageUploadProps> = ({
  label,
  value,
  onChange,
  error,
  accept = 'image/*',
  className,
  disabled = false,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [dragOver, setDragOver] = useState(false);
  const [preview, setPreview] = useState<string | null>(value || null);

  const handleFileSelect = (file: File | null) => {
    if (!file) {
      onChange(null);
      setPreview(null);
      return;
    }

    // Validate file type
    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
      return; // Could set an error here
    }

    // Validate file size
    if (file.size > MAX_FILE_SIZE) {
      return; // Could set an error here
    }

    onChange(file);

    // Create preview
    const reader = new FileReader();
    reader.onload = (e) => {
      setPreview(e.target?.result as string);
    };
    reader.readAsDataURL(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);

    const files = Array.from(e.dataTransfer.files);
    if (files.length > 0) {
      handleFileSelect(files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
  };

  const handleClick = () => {
    if (!disabled) {
      fileInputRef.current?.click();
    }
  };

  const handleRemove = () => {
    handleFileSelect(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const inputId = React.useId();

  return (
    <div className={cn('space-y-1', className)}>
      {label && (
        <label
          htmlFor={inputId}
          className="block text-sm font-medium text-gray-700"
        >
          {label}
        </label>
      )}
      
      <div
        className={cn(
          'relative border-2 border-dashed rounded-lg p-6 transition-colors',
          disabled
            ? 'border-gray-200 bg-gray-50 cursor-not-allowed'
            : dragOver
            ? 'border-blue-400 bg-blue-50'
            : 'border-gray-300 hover:border-gray-400',
          error && 'border-red-300'
        )}
        onDrop={disabled ? undefined : handleDrop}
        onDragOver={disabled ? undefined : handleDragOver}
        onDragLeave={disabled ? undefined : handleDragLeave}
      >
        <input
          ref={fileInputRef}
          id={inputId}
          type="file"
          accept={accept}
          onChange={(e) => {
            const file = e.target.files?.[0] || null;
            handleFileSelect(file);
          }}
          className="hidden"
          disabled={disabled}
          aria-describedby={error ? `${inputId}-error` : undefined}
        />

        {preview ? (
          <div className="text-center">
            <div className="relative mx-auto h-32 w-32 mb-4">
              <Image
                src={preview}
                alt="Preview"
                fill
                className="object-cover rounded-lg"
                sizes="128px"
              />
            </div>
            <div className="flex justify-center space-x-2">
              <button
                type="button"
                onClick={handleClick}
                disabled={disabled}
                className={cn(
                  "text-sm transition-colors",
                  disabled
                    ? "text-gray-400 cursor-not-allowed"
                    : "text-blue-600 hover:text-blue-500"
                )}
              >
                Change Image
              </button>
              <button
                type="button"
                onClick={handleRemove}
                disabled={disabled}
                className={cn(
                  "text-sm transition-colors",
                  disabled
                    ? "text-gray-400 cursor-not-allowed"
                    : "text-red-600 hover:text-red-500"
                )}
              >
                Remove
              </button>
            </div>
          </div>
        ) : (
          <div className="text-center">
            <svg
              className="mx-auto h-12 w-12 text-gray-400"
              stroke="currentColor"
              fill="none"
              viewBox="0 0 48 48"
            >
              <path
                d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
                strokeWidth={2}
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
            <div className="mt-4">
              <button
                type="button"
                onClick={handleClick}
                disabled={disabled}
                className={cn(
                  "font-medium transition-colors",
                  disabled
                    ? "text-gray-400 cursor-not-allowed"
                    : "text-blue-600 hover:text-blue-500"
                )}
              >
                Upload an image
              </button>
              <p className={cn(
                "text-sm mt-1",
                disabled ? "text-gray-400" : "text-gray-500"
              )}>
                {disabled ? "Upload disabled" : "or drag and drop"}
              </p>
            </div>
            <p className="text-xs text-gray-500 mt-2">
              PNG, JPG, GIF up to {formatFileSize(MAX_FILE_SIZE)}
            </p>
          </div>
        )}
      </div>

      {error && (
        <p
          id={`${inputId}-error`}
          className="text-sm text-red-600"
          role="alert"
        >
          {error}
        </p>
      )}
    </div>
  );
};

export default ImageUpload;
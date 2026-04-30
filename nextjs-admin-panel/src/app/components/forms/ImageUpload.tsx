'use client';

import React, { useRef, useState } from 'react';
import Image from 'next/image';
import { ImageUploadProps } from '@/lib/types';
import { cn, formatFileSize } from '@/lib/utils';
import { MAX_FILE_SIZE, ALLOWED_IMAGE_TYPES } from '@/lib/constants';
import { ImageIcon } from '../icons/Icons';

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
  const [preview, setPreview] = useState<string | null>(
    value ? (value.startsWith('http') || value.startsWith('/') ? value : `/${value}`) : null
  );

  const handleFileSelect = (file: File | null) => {
    if (!file) {
      onChange(null);
      setPreview(null);
      return;
    }
    if (!ALLOWED_IMAGE_TYPES.includes(file.type) || file.size > MAX_FILE_SIZE) return;

    onChange(file);
    const reader = new FileReader();
    reader.onload = (event) => setPreview(event.target?.result as string);
    reader.readAsDataURL(file);
  };

  const handleClick = () => {
    if (!disabled) fileInputRef.current?.click();
  };

  const inputId = React.useId();

  return (
    <div className={cn('space-y-1.5', className)}>
      {label && <label htmlFor={inputId} className="block text-[12px] font-semibold uppercase tracking-[0.2px] text-[var(--color-text-sub)]">{label}</label>}
      <div
        className={cn(
          'cursor-pointer rounded-[8px] border border-dashed border-[var(--color-border-med)] bg-[var(--color-surface-alt)] px-4 py-5 text-center transition-colors duration-150',
          dragOver && 'border-[var(--color-accent)] bg-[var(--color-accent-sub)]',
          disabled && 'cursor-not-allowed opacity-60',
          error && 'border-[var(--color-danger)]'
        )}
        onClick={handleClick}
        onDrop={(event) => {
          event.preventDefault();
          setDragOver(false);
          if (!disabled) handleFileSelect(Array.from(event.dataTransfer.files)[0] || null);
        }}
        onDragOver={(event) => {
          event.preventDefault();
          if (!disabled) setDragOver(true);
        }}
        onDragLeave={(event) => {
          event.preventDefault();
          setDragOver(false);
        }}
      >
        <input
          ref={fileInputRef}
          id={inputId}
          type="file"
          accept={accept}
          onChange={(event) => handleFileSelect(event.target.files?.[0] || null)}
          className="hidden"
          disabled={disabled}
          aria-describedby={error ? `${inputId}-error` : undefined}
        />

        {preview ? (
          <div className="flex items-center justify-center gap-3 text-left">
            <div className="relative h-9 w-12 overflow-hidden rounded-[4px] bg-[var(--color-surface)]">
              <Image src={preview} alt="Selected image" fill className="object-cover" sizes="48px" />
            </div>
            <div>
              <div className="text-[13px] font-semibold text-[var(--color-text)]">Image selected</div>
              <button type="button" className="text-[12px] font-semibold text-[var(--color-accent)]" onClick={handleClick} disabled={disabled}>Change</button>
            </div>
          </div>
        ) : (
          <div>
            <div className="mx-auto mb-2 flex justify-center text-[var(--color-text-muted)]"><ImageIcon /></div>
            <div className="text-[13px] text-[var(--color-text-sub)]">
              Drop an image or <span className="font-semibold text-[var(--color-accent)]">browse</span>
            </div>
            <div className="mt-1 text-[11px] text-[var(--color-text-muted)]">PNG, JPG up to {formatFileSize(MAX_FILE_SIZE)}</div>
          </div>
        )}
      </div>
      {error && <p id={`${inputId}-error`} className="text-[11px] text-[var(--color-danger)]" role="alert">{error}</p>}
    </div>
  );
};

export default ImageUpload;

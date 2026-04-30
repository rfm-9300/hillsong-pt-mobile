'use client';

import React from 'react';
import { AlertProps } from '@/lib/types';
import { cn } from '@/lib/utils';
import { AlertIcon, CheckIcon, XIcon } from '../icons/Icons';

const styles = {
  success: 'bg-[var(--color-success-bg)] text-[var(--color-success)] border-[rgba(22,163,74,0.25)]',
  error: 'bg-[var(--color-danger-bg)] text-[var(--color-danger)] border-[rgba(220,38,38,0.25)]',
  warning: 'bg-[var(--color-warning-bg)] text-[var(--color-warning)] border-[rgba(217,119,6,0.25)]',
  info: 'bg-[var(--color-info-bg)] text-[var(--color-info)] border-[rgba(37,99,235,0.25)]',
};

const Alert: React.FC<AlertProps> = ({ type, message, onClose, className }) => (
  <div className={cn('flex items-start gap-2.5 rounded-[8px] border px-[14px] py-2.5', styles[type], className)} role="alert">
    <span className="mt-0.5 shrink-0">{type === 'success' ? <CheckIcon /> : <AlertIcon />}</span>
    <p className="flex-1 text-[13px] leading-[1.4]">{message}</p>
    {onClose && (
      <button type="button" onClick={onClose} className="shrink-0 cursor-pointer rounded p-0.5 opacity-70 hover:opacity-100" aria-label="Dismiss alert">
        <XIcon />
      </button>
    )}
  </div>
);

export default Alert;

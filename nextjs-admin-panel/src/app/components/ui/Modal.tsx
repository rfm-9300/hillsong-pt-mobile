'use client';

import React, { useEffect } from 'react';
import { ModalProps } from '@/lib/types';
import { cn } from '@/lib/utils';
import { XIcon } from '../icons/Icons';

const sizes = {
  sm: 'max-w-[440px]',
  md: 'max-w-[560px]',
  lg: 'max-w-[720px]',
};

const Modal: React.FC<ModalProps> = ({ show, title, size = 'md', onClose, children }) => {
  useEffect(() => {
    if (!show) return;
    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleEscape);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [show, onClose]);

  if (!show) return null;

  return (
    <div className="fixed inset-0 z-[200] flex items-end justify-center bg-black/45 backdrop-blur-[4px] sm:items-center sm:p-6" onClick={onClose}>
      <div
        className={cn(
          'max-h-[92dvh] w-full overflow-auto rounded-t-[16px] bg-[var(--color-surface)] pb-safe shadow-[0_24px_60px_rgba(0,0,0,0.2)] sm:max-h-[90vh] sm:rounded-[12px]',
          sizes[size]
        )}
        style={{ animation: 'sheet-in 180ms ease' }}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="sticky top-0 z-10 flex items-center justify-between border-b border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-4">
          <h3 className="text-[15px] font-bold text-[var(--color-text)]">{title}</h3>
          <button type="button" onClick={onClose} className="flex min-h-touch min-w-touch items-center justify-center rounded-[7px] text-[var(--color-text-sub)] hover:bg-[var(--color-surface-alt)] sm:min-h-0 sm:min-w-0 sm:p-1.5" aria-label="Close modal">
            <XIcon />
          </button>
        </div>
        <div className="p-4 sm:p-5">{children}</div>
      </div>
    </div>
  );
};

export default Modal;

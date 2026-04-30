'use client';

import React from 'react';
import { EmptyStateProps } from '@/lib/types';
import Button from './Button';
import { ImageIcon, PlusIcon } from '../icons/Icons';

const EmptyState: React.FC<EmptyStateProps & { actionLabel?: string }> = ({
  title,
  description,
  actionText,
  actionLabel,
  onAction,
  icon,
}) => (
  <div className="px-10 py-[60px] text-center">
    <div className="mx-auto mb-4 flex h-[52px] w-[52px] items-center justify-center rounded-[12px] bg-[var(--color-surface-alt)] text-[var(--color-text-muted)]">
      {icon || <ImageIcon />}
    </div>
    <h3 className="font-display text-[20px] font-normal text-[var(--color-text)]">{title}</h3>
    <p className="mx-auto mt-1 mb-5 max-w-sm text-[13px] leading-[1.5] text-[var(--color-text-sub)]">{description}</p>
    {(actionText || actionLabel) && onAction && (
      <Button variant="primary" size="md" icon={<PlusIcon />} onClick={onAction}>
        {actionLabel || actionText}
      </Button>
    )}
  </div>
);

export default EmptyState;

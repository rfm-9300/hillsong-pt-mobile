'use client';

import React from 'react';
import { CardProps } from '@/lib/types';
import { cn } from '@/lib/utils';

const Card: React.FC<CardProps> = ({
  children,
  hover = false,
  padding = '',
  className,
  onClick,
}) => (
  <div
    className={cn(
      'rounded-[10px] border border-[var(--color-border)] bg-[var(--color-surface)]',
      hover && 'transition-all duration-150 hover:border-[var(--color-border-med)] hover:shadow-[0_4px_16px_rgba(0,0,0,0.08)]',
      onClick && 'cursor-pointer',
      padding,
      className
    )}
    onClick={onClick}
  >
    {children}
  </div>
);

export default Card;

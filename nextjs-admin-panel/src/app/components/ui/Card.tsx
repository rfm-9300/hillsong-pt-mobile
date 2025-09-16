'use client';

import React from 'react';
import { CardProps } from '@/lib/types';
import { cn } from '@/lib/utils';

const Card: React.FC<CardProps> = ({
  children,
  hover = false,
  padding = 'p-6',
  className,
  onClick,
}) => {
  const baseClasses = 'bg-white rounded-lg border border-gray-200 shadow-sm';
  const hoverClasses = hover ? 'hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer hover-lift' : '';
  const clickableClasses = onClick ? 'cursor-pointer' : '';

  return (
    <div
      className={cn(
        baseClasses,
        hoverClasses,
        clickableClasses,
        padding,
        className
      )}
      onClick={onClick}
    >
      {children}
    </div>
  );
};

export default Card;
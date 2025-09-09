'use client';

import React from 'react';
import { cn } from '@/lib/utils';

interface ResponsiveGridProps {
  children: React.ReactNode;
  className?: string;
  cols?: {
    default?: number;
    sm?: number;
    md?: number;
    lg?: number;
    xl?: number;
  };
  gap?: string;
  minItemWidth?: string;
}

const ResponsiveGrid: React.FC<ResponsiveGridProps> = ({
  children,
  className,
  cols = { default: 1, sm: 2, lg: 3 },
  gap = 'gap-6',
  minItemWidth,
}) => {
  // Build grid classes based on cols prop
  const gridClasses = [];
  
  if (cols.default) gridClasses.push(`grid-cols-${cols.default}`);
  if (cols.sm) gridClasses.push(`sm:grid-cols-${cols.sm}`);
  if (cols.md) gridClasses.push(`md:grid-cols-${cols.md}`);
  if (cols.lg) gridClasses.push(`lg:grid-cols-${cols.lg}`);
  if (cols.xl) gridClasses.push(`xl:grid-cols-${cols.xl}`);

  // If minItemWidth is provided, use auto-fit grid
  const gridStyle = minItemWidth 
    ? { gridTemplateColumns: `repeat(auto-fit, minmax(${minItemWidth}, 1fr))` }
    : undefined;

  return (
    <div
      className={cn(
        'grid',
        !minItemWidth && gridClasses.join(' '),
        gap,
        className
      )}
      style={gridStyle}
    >
      {children}
    </div>
  );
};

export default ResponsiveGrid;

// Predefined grid configurations for common use cases
export const PostsGrid: React.FC<{ children: React.ReactNode; className?: string }> = ({ 
  children, 
  className 
}) => (
  <ResponsiveGrid
    cols={{ default: 1, sm: 2, lg: 3 }}
    gap="gap-6"
    className={className}
  >
    {children}
  </ResponsiveGrid>
);

export const EventsGrid: React.FC<{ children: React.ReactNode; className?: string }> = ({ 
  children, 
  className 
}) => (
  <ResponsiveGrid
    cols={{ default: 1, md: 2, lg: 3 }}
    gap="gap-6"
    className={className}
  >
    {children}
  </ResponsiveGrid>
);

export const UsersGrid: React.FC<{ children: React.ReactNode; className?: string }> = ({ 
  children, 
  className 
}) => (
  <ResponsiveGrid
    cols={{ default: 1, md: 2, lg: 3, xl: 4 }}
    gap="gap-6"
    className={className}
  >
    {children}
  </ResponsiveGrid>
);

export const DashboardGrid: React.FC<{ children: React.ReactNode; className?: string }> = ({ 
  children, 
  className 
}) => (
  <ResponsiveGrid
    cols={{ default: 1, sm: 2, lg: 3, xl: 4 }}
    gap="gap-4 md:gap-6"
    className={className}
  >
    {children}
  </ResponsiveGrid>
);
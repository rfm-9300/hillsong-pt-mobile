'use client';

import React from 'react';
import { cn } from '@/lib/utils';

interface AnimatedListProps {
  children: React.ReactNode;
  className?: string;
  animation?: 'fade-in' | 'slide-in-from-bottom' | 'slide-in-from-left' | 'slide-in-from-right' | 'scale-in' | 'bounce-in';
  staggerDelay?: number;
  duration?: number;
  as?: keyof React.JSX.IntrinsicElements;
}

const AnimatedList: React.FC<AnimatedListProps> = ({
  children,
  className,
  animation = 'slide-in-from-bottom',
  staggerDelay = 100,
  duration = 400,
  as: Component = 'div',
}) => {
  const childrenArray = React.Children.toArray(children);

  return (
    <Component className={className}>
      {childrenArray.map((child, index) => (
        <div
          key={index}
          className={cn('animate-in', animation)}
          style={{
            animationDelay: `${index * staggerDelay}ms`,
            animationDuration: `${duration}ms`,
          }}
        >
          {child}
        </div>
      ))}
    </Component>
  );
};

export default AnimatedList;

// Predefined animated list components
export const AnimatedGrid: React.FC<{
  children: React.ReactNode;
  className?: string;
  cols?: string;
  gap?: string;
}> = ({ children, className, cols = 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3', gap = 'gap-6' }) => (
  <AnimatedList
    className={cn('grid', cols, gap, className)}
    animation="slide-in-from-bottom"
    staggerDelay={75}
    duration={400}
  >
    {children}
  </AnimatedList>
);

export const AnimatedStack: React.FC<{
  children: React.ReactNode;
  className?: string;
  spacing?: string;
}> = ({ children, className, spacing = 'space-y-4' }) => (
  <AnimatedList
    className={cn('flex flex-col', spacing, className)}
    animation="slide-in-from-left"
    staggerDelay={100}
    duration={300}
  >
    {children}
  </AnimatedList>
);

export const AnimatedRow: React.FC<{
  children: React.ReactNode;
  className?: string;
  spacing?: string;
}> = ({ children, className, spacing = 'space-x-4' }) => (
  <AnimatedList
    className={cn('flex flex-row', spacing, className)}
    animation="scale-in"
    staggerDelay={150}
    duration={350}
  >
    {children}
  </AnimatedList>
);
'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import Breadcrumb, { BreadcrumbItem } from './Breadcrumb';
import Button from './Button';
import { cn } from '@/lib/utils';

interface NavigationHeaderProps {
  title?: string;
  subtitle?: string;
  breadcrumbs?: BreadcrumbItem[];
  showBackButton?: boolean;
  backButtonText?: string;
  backButtonHref?: string;
  children?: React.ReactNode;
  className?: string;
}

const NavigationHeader: React.FC<NavigationHeaderProps> = ({
  title,
  subtitle,
  breadcrumbs,
  showBackButton = false,
  backButtonText = 'Back',
  backButtonHref,
  children,
  className,
}) => {
  const router = useRouter();

  const handleBackClick = () => {
    if (backButtonHref) {
      router.push(backButtonHref);
    } else {
      router.back();
    }
  };

  return (
    <div className={cn('space-y-4', className)}>
      {/* Breadcrumbs */}
      <Breadcrumb items={breadcrumbs} />
      
      {/* Back Button */}
      {showBackButton && (
        <div>
          <Button
            variant="ghost"
            size="sm"
            onClick={handleBackClick}
            className="text-gray-600 hover:text-blue-600 transition-colors p-0 h-auto font-normal"
          >
            <svg 
              xmlns="http://www.w3.org/2000/svg" 
              className="h-4 w-4 mr-2" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path 
                strokeLinecap="round" 
                strokeLinejoin="round" 
                strokeWidth="2" 
                d="M10 19l-7-7m0 0l7-7m-7 7h18" 
              />
            </svg>
            {backButtonText}
          </Button>
        </div>
      )}

      {/* Title and Actions */}
      {(title || children) && (
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            {title && (
              <h1 className="text-2xl font-bold text-gray-900">
                {title}
              </h1>
            )}
            {subtitle && (
              <p className="mt-1 text-sm text-gray-600">
                {subtitle}
              </p>
            )}
          </div>
          {children && (
            <div className="flex items-center gap-3">
              {children}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default NavigationHeader;
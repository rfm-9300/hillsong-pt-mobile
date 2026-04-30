'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import Breadcrumb, { BreadcrumbItem } from './Breadcrumb';
import Button from './Button';
import { cn } from '@/lib/utils';
import { ChevronRIcon } from '../icons/Icons';

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

  return (
    <div className={cn('mb-6', className)}>
      {breadcrumbs && <Breadcrumb items={breadcrumbs} className="mb-2 hidden md:flex" />}
      {showBackButton && (
        <Button
          variant="ghost"
          size="sm"
          icon={<ChevronRIcon className="rotate-180" />}
          onClick={() => backButtonHref ? router.push(backButtonHref) : router.back()}
          className="mb-3 px-0"
        >
          {backButtonText}
        </Button>
      )}
      {(title || children) && (
        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between sm:gap-4">
          <div className="min-w-0">
            {title && <h1 className="font-display text-[22px] font-normal leading-[1.15] text-[var(--color-text)] sm:text-[26px]">{title}</h1>}
            {subtitle && <p className="mt-1 text-[13px] leading-[1.4] text-[var(--color-text-sub)]">{subtitle}</p>}
          </div>
          {children && <div className="flex flex-wrap items-center gap-2">{children}</div>}
        </div>
      )}
    </div>
  );
};

export default NavigationHeader;

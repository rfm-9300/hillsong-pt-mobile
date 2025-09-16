'use client';

import React from 'react';
import { PageHeaderProps } from '@/lib/types';

const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  subtitle,
  children,
}) => {
  return (
    <div className="mb-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            {title}
          </h1>
          {subtitle && (
            <p className="mt-2 text-gray-600">
              {subtitle}
            </p>
          )}
        </div>
        {children && (
          <div className="flex items-center space-x-3">
            {children}
          </div>
        )}
      </div>
    </div>
  );
};

export default PageHeader;
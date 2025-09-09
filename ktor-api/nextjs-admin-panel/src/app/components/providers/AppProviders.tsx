'use client';

import React from 'react';
import { ErrorProvider } from '@/app/context/ErrorContext';
import { ErrorBoundary } from '@/app/components/ui';

interface AppProvidersProps {
  children: React.ReactNode;
}

const AppProviders: React.FC<AppProvidersProps> = ({ children }) => {
  return (
    <ErrorBoundary level="global" showErrorDetails={process.env.NODE_ENV === 'development'}>
      <ErrorProvider>
        {children}
      </ErrorProvider>
    </ErrorBoundary>
  );
};

export default AppProviders;
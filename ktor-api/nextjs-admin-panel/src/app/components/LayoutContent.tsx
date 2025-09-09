'use client';

import { useState } from 'react';
import { AuthProvider, useAuth } from '../context/AuthContext';
import { ErrorProvider } from '../context/ErrorContext';
import ErrorBoundary from './ui/ErrorBoundary';
import { CompactGlobalLoadingIndicator } from './ui/GlobalLoadingIndicator';
import Sidebar from './Sidebar';
import MobileHeader from './MobileHeader';
import MobileSidebar from './MobileSidebar';

function LayoutContentInner({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div className="flex h-screen bg-gray-50">
      <CompactGlobalLoadingIndicator />
      {isAuthenticated ? (
        <>
          <Sidebar />
          <MobileHeader onMenuClick={toggleSidebar} />
          <MobileSidebar isOpen={sidebarOpen} onClose={toggleSidebar} />

          <main className="flex-1 md:ml-64">
            <div className="flex flex-col w-full p-4 sm:p-6 md:p-8 min-h-screen page-transition">
              {children}
            </div>
          </main>
        </>
      ) : (
        <div className="flex items-center justify-center min-h-screen w-full bg-gradient-to-br from-indigo-50 to-white">
          {children}
        </div>
      )}
    </div>
  );
}

export default function LayoutContent({ children }: { children: React.ReactNode }) {
  return (
    <ErrorBoundary level="global" showErrorDetails={process.env.NODE_ENV === 'development'}>
      <ErrorProvider>
        <AuthProvider>
          <LayoutContentInner>{children}</LayoutContentInner>
        </AuthProvider>
      </ErrorProvider>
    </ErrorBoundary>
  );
}
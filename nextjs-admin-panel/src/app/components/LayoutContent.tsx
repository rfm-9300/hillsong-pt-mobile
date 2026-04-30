'use client';

import { useEffect, useState } from 'react';
import { usePathname } from 'next/navigation';
import { AuthProvider, useAuth } from '../context/AuthContext';
import { ErrorProvider } from '../context/ErrorContext';
import ErrorBoundary from './ui/ErrorBoundary';
import { CompactGlobalLoadingIndicator } from './ui/GlobalLoadingIndicator';
import MobileHeader from './MobileHeader';
import MobileSidebar from './MobileSidebar';
import Sidebar from './Sidebar';
import TopBar from './TopBar';

function LayoutContentInner({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const pathname = usePathname();

  useEffect(() => {
    setMobileNavOpen(false);
  }, [pathname]);

  if (!isAuthenticated) {
    return (
      <>
        <CompactGlobalLoadingIndicator />
        {children}
      </>
    );
  }

  return (
    <div className="flex h-[100dvh] overflow-hidden bg-[var(--color-content-bg)]">
      <CompactGlobalLoadingIndicator />
      <Sidebar />
      <MobileSidebar isOpen={mobileNavOpen} onClose={() => setMobileNavOpen(false)} />
      <div className="flex flex-1 flex-col overflow-hidden">
        <MobileHeader onMenuClick={() => setMobileNavOpen(true)} />
        <TopBar />
        <main className="flex-1 overflow-auto bg-[var(--color-content-bg)] px-4 py-4 pb-12 pl-safe pr-safe sm:px-6 md:px-7 md:py-6">
          {children}
        </main>
      </div>
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

'use client';

import React from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { RefreshIcon } from './icons/Icons';
import { getBreadcrumbs } from './TopBar';

interface MobileHeaderProps {
  onMenuClick: () => void;
}

const MobileHeader: React.FC<MobileHeaderProps> = ({ onMenuClick }) => {
  const pathname = usePathname();
  const router = useRouter();
  const title = getBreadcrumbs(pathname).at(-1)?.label ?? 'Hillsong PT';

  return (
    <header className="sticky top-0 z-30 flex items-center justify-between border-b border-[var(--color-sidebar-border)] bg-[var(--color-sidebar-bg)] px-3 py-2 pt-safe text-white md:hidden">
      <div className="flex min-w-0 items-center gap-2">
        <button
          onClick={onMenuClick}
          className="flex min-h-touch min-w-touch cursor-pointer items-center justify-center rounded-[7px] text-[var(--color-sidebar-text)] transition-colors hover:bg-white/[0.04] hover:text-white"
          aria-label="Open navigation menu"
        >
          <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.7" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <div className="min-w-0">
          <p className="truncate font-display text-[17px] leading-tight text-[#F9F8F6]">{title}</p>
          <p className="text-[10px] uppercase tracking-[1.2px] text-[var(--color-sidebar-text)]">Hillsong PT</p>
        </div>
      </div>
      <button
        type="button"
        onClick={() => router.refresh()}
        className="flex min-h-touch min-w-touch cursor-pointer items-center justify-center rounded-[7px] text-[var(--color-sidebar-text)] transition-colors hover:bg-white/[0.04] hover:text-white"
        aria-label="Refresh"
      >
        <RefreshIcon />
      </button>
    </header>
  );
};

export default MobileHeader;

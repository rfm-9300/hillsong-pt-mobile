'use client';

import React from 'react';
import { useRouter } from 'next/navigation';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  actions?: React.ReactNode;
  breadcrumbs?: string[];
  children?: React.ReactNode;
}

const PageHeader: React.FC<PageHeaderProps> = ({ title, subtitle, actions, breadcrumbs, children }) => {
  const router = useRouter();
  const actionSlot = actions || children;

  return (
    <div className="mb-6">
      {breadcrumbs && breadcrumbs.length > 0 && (
        <div className="mb-2 hidden items-center gap-1.5 text-[12px] font-medium md:flex">
          {breadcrumbs.map((crumb, index) => {
            const last = index === breadcrumbs.length - 1;
            return (
              <React.Fragment key={`${crumb}-${index}`}>
                {index > 0 && <span className="text-[var(--color-text-muted)]">/</span>}
                <button
                  type="button"
                  className={last ? 'cursor-default text-[var(--color-text-sub)]' : 'cursor-pointer text-[var(--color-accent)]'}
                  onClick={() => {
                    if (!last && crumb.toLowerCase() === 'admin') router.push('/admin/dashboard');
                  }}
                >
                  {crumb}
                </button>
              </React.Fragment>
            );
          })}
        </div>
      )}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between sm:gap-4">
        <div className="min-w-0">
          <h1 className="font-display text-[22px] font-normal leading-[1.15] text-[var(--color-text)] sm:text-[26px]">{title}</h1>
          {subtitle && <p className="mt-1 text-[13px] leading-[1.4] text-[var(--color-text-sub)]">{subtitle}</p>}
        </div>
        {actionSlot && <div className="flex flex-wrap items-center gap-2">{actionSlot}</div>}
      </div>
    </div>
  );
};

export default PageHeader;

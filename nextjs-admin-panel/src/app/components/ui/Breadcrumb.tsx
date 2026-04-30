'use client';

import React from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { cn } from '@/lib/utils';

export interface BreadcrumbItem {
  label: string;
  href?: string;
  route?: string;
  current?: boolean;
}

interface BreadcrumbProps {
  items?: BreadcrumbItem[];
  className?: string;
  onNavigate?: (route: string) => void;
}

const Breadcrumb: React.FC<BreadcrumbProps> = ({ items, className, onNavigate }) => {
  const pathname = usePathname();
  const router = useRouter();
  const breadcrumbItems = items || generateBreadcrumbsFromPath(pathname);

  if (breadcrumbItems.length <= 1) return null;

  return (
    <nav className={cn('flex items-center gap-1.5 text-[12px] font-medium', className)} aria-label="Breadcrumb">
      {breadcrumbItems.map((item, index) => {
        const href = item.href || item.route;
        const last = item.current || index === breadcrumbItems.length - 1 || !href;
        return (
          <React.Fragment key={`${item.label}-${index}`}>
            {index > 0 && <span className="text-[var(--color-text-muted)]">/</span>}
            <button
              type="button"
              disabled={last}
              className={last ? 'cursor-default text-[var(--color-text-sub)]' : 'cursor-pointer text-[var(--color-accent)] hover:text-[var(--color-accent-hover)]'}
              onClick={() => {
                if (!last && href) (onNavigate || router.push)(href);
              }}
            >
              {item.label}
            </button>
          </React.Fragment>
        );
      })}
    </nav>
  );
};

function generateBreadcrumbsFromPath(pathname: string): BreadcrumbItem[] {
  const parts = pathname.split('/').filter(Boolean);
  const items: BreadcrumbItem[] = [{ label: 'Dashboard', href: '/admin/dashboard' }];
  let currentPath = '';
  parts.forEach((part, index) => {
    currentPath += `/${part}`;
    if (part === 'admin' || part === 'dashboard') return;
    const last = index === parts.length - 1;
    items.push({ label: formatSegment(part, parts, index), href: last ? undefined : currentPath, current: last });
  });
  return items;
}

function formatSegment(segment: string, parts: string[], index: number) {
  if (segment === 'create') return 'New';
  if (/^\d+$/.test(segment) || /^[a-f0-9-]{24,36}$/i.test(segment)) return `Edit ${singular(parts[index - 1])}`;
  const labels: Record<string, string> = {
    posts: 'Posts',
    events: 'Events',
    groups: 'Groups',
    encounters: 'Encounters',
    videos: 'Videos',
    calendar: 'Calendar',
    users: 'Users',
    attendance: 'Attendance',
    event: 'Events',
    service: 'Services',
    'kids-service': 'Kids Services',
    reports: 'Reports',
  };
  return labels[segment] || segment.charAt(0).toUpperCase() + segment.slice(1);
}

function singular(value = '') {
  const map: Record<string, string> = { posts: 'Post', events: 'Event', groups: 'Group', encounters: 'Encounter', videos: 'Video' };
  return map[value] || 'Item';
}

export default Breadcrumb;

'use client';

import React from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { RefreshIcon } from './icons/Icons';

export default function TopBar() {
  const pathname = usePathname();
  const router = useRouter();
  const crumbs = getBreadcrumbs(pathname);

  return (
    <header className="sticky top-0 z-10 hidden h-[52px] shrink-0 items-center justify-between border-b border-[var(--color-border)] bg-[var(--color-surface)] px-6 md:flex">
      <nav className="flex items-center gap-1.5 text-[12px]">
        {crumbs.map((crumb, index) => {
          const last = index === crumbs.length - 1;
          return (
            <React.Fragment key={`${crumb.label}-${index}`}>
              {index > 0 && <span className="text-[var(--color-text-muted)]">/</span>}
              <button
                type="button"
                disabled={last}
                onClick={() => !last && router.push(crumb.route)}
                className={last ? 'cursor-default text-[var(--color-text-sub)]' : 'cursor-pointer font-medium text-[var(--color-accent)]'}
              >
                {crumb.label}
              </button>
            </React.Fragment>
          );
        })}
      </nav>
      <button
        type="button"
        onClick={() => router.refresh()}
        className="flex min-h-touch min-w-touch cursor-pointer items-center justify-center rounded-[7px] bg-[var(--color-surface-alt)] text-[var(--color-text-sub)] transition-colors hover:text-[var(--color-text)] md:h-7 md:w-7 md:min-h-0 md:min-w-0"
        aria-label="Refresh"
      >
        <RefreshIcon />
      </button>
    </header>
  );
}

export function getBreadcrumbs(pathname: string) {
  const exact: Record<string, { label: string; route: string }[]> = {
    '/admin/dashboard': [{ label: 'Dashboard', route: '/admin/dashboard' }],
    '/admin/posts': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Posts', route: '/admin/posts' }],
    '/admin/posts/create': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Posts', route: '/admin/posts' }, { label: 'New Post', route: '/admin/posts/create' }],
    '/admin/events': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Events', route: '/admin/events' }],
    '/admin/events/create': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Events', route: '/admin/events' }, { label: 'New Event', route: '/admin/events/create' }],
    '/admin/groups': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Groups', route: '/admin/groups' }],
    '/admin/groups/create': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Groups', route: '/admin/groups' }, { label: 'New Group', route: '/admin/groups/create' }],
    '/admin/encounters': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Encounters', route: '/admin/encounters' }],
    '/admin/encounters/create': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Encounters', route: '/admin/encounters' }, { label: 'New Encounter', route: '/admin/encounters/create' }],
    '/admin/videos': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Videos', route: '/admin/videos' }],
    '/admin/videos/create': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Videos', route: '/admin/videos' }, { label: 'New Video', route: '/admin/videos/create' }],
    '/admin/calendar': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Calendar', route: '/admin/calendar' }],
    '/admin/calendar/create': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Calendar', route: '/admin/calendar' }, { label: 'New Item', route: '/admin/calendar/create' }],
    '/admin/users': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Users', route: '/admin/users' }],
    '/admin/attendance': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Attendance', route: '/admin/attendance' }],
    '/admin/attendance/event': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Attendance', route: '/admin/attendance' }, { label: 'Events', route: '/admin/attendance/event' }],
    '/admin/attendance/service': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Attendance', route: '/admin/attendance' }, { label: 'Services', route: '/admin/attendance/service' }],
    '/admin/attendance/kids-service': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Attendance', route: '/admin/attendance' }, { label: 'Kids Services', route: '/admin/attendance/kids-service' }],
    '/admin/attendance/reports': [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Attendance', route: '/admin/attendance' }, { label: 'Reports', route: '/admin/attendance/reports' }],
  };
  if (exact[pathname]) return exact[pathname];
  if (/^\/admin\/posts\/[^/]+$/.test(pathname)) return [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Posts', route: '/admin/posts' }, { label: 'Edit Post', route: pathname }];
  if (/^\/admin\/events\/[^/]+$/.test(pathname)) return [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Events', route: '/admin/events' }, { label: 'Edit Event', route: pathname }];
  if (/^\/admin\/groups\/[^/]+$/.test(pathname)) return [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Groups', route: '/admin/groups' }, { label: 'Edit Group', route: pathname }];
  if (/^\/admin\/encounters\/[^/]+$/.test(pathname)) return [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Encounters', route: '/admin/encounters' }, { label: 'Edit Encounter', route: pathname }];
  if (/^\/admin\/videos\/[^/]+$/.test(pathname)) return [{ label: 'Dashboard', route: '/admin/dashboard' }, { label: 'Videos', route: '/admin/videos' }, { label: 'Edit Video', route: pathname }];
  return [{ label: 'Dashboard', route: '/admin/dashboard' }];
}

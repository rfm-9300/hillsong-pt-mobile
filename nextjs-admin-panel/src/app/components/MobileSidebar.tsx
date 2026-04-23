'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';
import { useAuth } from '@/app/hooks';
import {
  AttendanceIcon,
  CalendarIcon,
  DashboardIcon,
  EncountersIcon,
  EventsIcon,
  PostsIcon,
  ReportsIcon,
  ServiceIcon,
  SignoutIcon,
  UsersIcon,
  VideosIcon,
  XIcon,
} from './icons/Icons';

const navItems = [
  { label: 'Dashboard', href: '/admin/dashboard', icon: DashboardIcon },
  { label: 'Posts', href: '/admin/posts', icon: PostsIcon },
  { label: 'Events', href: '/admin/events', icon: EventsIcon },
  { label: 'Encounters', href: '/admin/encounters', icon: EncountersIcon },
  { label: 'Videos', href: '/admin/videos', icon: VideosIcon },
  { label: 'Calendar', href: '/admin/calendar', icon: CalendarIcon },
  { label: 'Users', href: '/admin/users', icon: UsersIcon },
  { label: 'Attendance', href: '/admin/attendance', icon: AttendanceIcon },
  { label: 'Services', href: '/admin/attendance/service', icon: ServiceIcon },
  { label: 'Reports', href: '/admin/attendance/reports', icon: ReportsIcon },
];

interface MobileSidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const MobileSidebar: React.FC<MobileSidebarProps> = ({ isOpen, onClose }) => {
  const pathname = usePathname();
  const { user, loading, logout } = useAuth();

  const handleLogout = () => {
    onClose();
    logout();
  };

  return (
    <>
      {isOpen && <div className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm md:hidden" onClick={onClose} />}
      <div className={cn('fixed inset-y-0 left-0 z-50 flex w-72 flex-col bg-[var(--color-sidebar-bg)] text-white shadow-2xl transition-transform duration-300 md:hidden', isOpen ? 'translate-x-0' : '-translate-x-full')}>
        <div className="flex items-center justify-between border-b border-[var(--color-sidebar-border)] p-5">
          <div className="flex items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-[8px] border border-[rgba(201,149,42,0.3)] bg-[var(--color-accent-sub)]">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M7 1v12M4 3.5v7M10 3.5v7M1 7h12" stroke="var(--color-accent)" strokeWidth="1.8" strokeLinecap="round" />
              </svg>
            </div>
            <div>
              <h2 className="font-display text-[16px] text-[#F9F8F6]">Hillsong PT</h2>
              <p className="text-[10px] uppercase tracking-[1.2px] text-[var(--color-sidebar-text)]">Admin Panel</p>
            </div>
          </div>
          <button onClick={onClose} className="rounded-[7px] p-2 text-[var(--color-sidebar-text)] hover:bg-white/[0.04] hover:text-white" aria-label="Close navigation menu">
            <XIcon />
          </button>
        </div>

        {!loading && user && (
          <div className="px-4 py-4">
            <div className="flex items-center gap-3 rounded-[8px] bg-white/[0.04] p-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-full border border-[rgba(201,149,42,0.45)] bg-[var(--color-accent-sub)] font-display text-[13px] text-[var(--color-accent)]">
                {(user.firstName || user.email).charAt(0).toUpperCase()}
              </div>
              <div className="min-w-0">
                <p className="truncate text-[12px] font-semibold text-white">{user.firstName || user.email.split('@')[0]}</p>
                <p className="text-[10px] text-[var(--color-sidebar-text)]">{user.isAdmin ? 'Admin' : 'User'}</p>
              </div>
            </div>
          </div>
        )}

        <nav className="flex-1 overflow-y-auto px-3 py-2">
          {navItems.map(({ label, href, icon: Icon }) => {
            const active = href === '/admin/attendance' ? pathname === href : pathname.startsWith(href);
            return (
              <Link key={href} href={href} onClick={onClose} className={cn('mb-px flex items-center gap-2.5 rounded-[7px] border-l-2 px-3 py-2.5 text-[13px]', active ? 'border-l-[var(--color-accent)] bg-[var(--color-sidebar-active-bg)] font-semibold text-white' : 'border-l-transparent text-[var(--color-sidebar-text)] hover:bg-white/[0.04]')}>
                <Icon className={active ? 'text-[var(--color-accent)]' : 'text-[var(--color-sidebar-text)]'} />
                {label}
              </Link>
            );
          })}
        </nav>

        <div className="border-t border-[var(--color-sidebar-border)] p-4">
          <button onClick={handleLogout} className="flex w-full items-center justify-center gap-2 rounded-[7px] bg-white/[0.04] px-4 py-3 text-[13px] font-semibold text-[var(--color-sidebar-text)] hover:text-white">
            <SignoutIcon />
            Sign Out
          </button>
        </div>
      </div>
    </>
  );
};

export default MobileSidebar;

'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';
import { useAuth as useProfileAuth } from '@/app/hooks';
import { useAuth as useSessionAuth } from '@/app/context/AuthContext';
import {
  AttendanceIcon,
  CalendarIcon,
  ChevronDIcon,
  DashboardIcon,
  EncountersIcon,
  EventsIcon,
  GroupsIcon,
  KidsIcon,
  OverviewIcon,
  PostsIcon,
  ReportsIcon,
  ServiceIcon,
  SignoutIcon,
  UsersIcon,
  VideosIcon,
} from './icons/Icons';

const navItems = [
  { label: 'Dashboard', href: '/admin/dashboard', icon: DashboardIcon },
  { label: 'Posts', href: '/admin/posts', icon: PostsIcon },
  { label: 'Events', href: '/admin/events', icon: EventsIcon },
  { label: 'Groups', href: '/admin/groups', icon: GroupsIcon },
  { label: 'Encounters', href: '/admin/encounters', icon: EncountersIcon },
  { label: 'Videos', href: '/admin/videos', icon: VideosIcon },
  { label: 'Calendar', href: '/admin/calendar', icon: CalendarIcon },
  { label: 'Users', href: '/admin/users', icon: UsersIcon },
];

const attendanceItems = [
  { label: 'Overview', href: '/admin/attendance', icon: OverviewIcon },
  { label: 'Events', href: '/admin/attendance/event', icon: EventsIcon },
  { label: 'Services', href: '/admin/attendance/service', icon: ServiceIcon },
  { label: 'Kids Services', href: '/admin/attendance/kids-service', icon: KidsIcon },
  { label: 'Reports', href: '/admin/attendance/reports', icon: ReportsIcon },
];

export default function Sidebar() {
  const pathname = usePathname();
  const { user, loading, logout: profileLogout } = useProfileAuth();
  const { logout: sessionLogout } = useSessionAuth();
  const [attendanceOpen, setAttendanceOpen] = useState(pathname.startsWith('/admin/attendance'));

  const isActive = (href: string) => href === '/admin/attendance' ? pathname === href : pathname.startsWith(href);
  const attendanceActive = pathname.startsWith('/admin/attendance');
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.fullName || user?.email?.split('@')[0] || 'Admin';

  return (
    <aside className="hidden h-screen w-[232px] shrink-0 flex-col overflow-hidden border-r border-[var(--color-sidebar-border)] bg-[var(--color-sidebar-bg)] md:flex">
      <div className="border-b border-[var(--color-sidebar-border)] px-5 pb-4 pt-5">
        <div className="flex items-center gap-3">
          <div className="flex h-8 w-8 items-center justify-center rounded-[8px] border border-[rgba(201,149,42,0.3)] bg-[var(--color-accent-sub)]">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M7 1v12M4 3.5v7M10 3.5v7M1 7h12" stroke="var(--color-accent)" strokeWidth="1.8" strokeLinecap="round" />
            </svg>
          </div>
          <div>
            <div className="font-display text-[16px] leading-none tracking-[-0.2px] text-[#F9F8F6]">Hillsong PT</div>
            <div className="mt-0.5 text-[10px] uppercase tracking-[1.2px] text-[var(--color-sidebar-text)]">Admin Panel</div>
          </div>
        </div>
      </div>

      {!loading && user && (
        <div className="border-b border-[var(--color-sidebar-border)] px-[14px] py-3">
          <div className="flex items-center gap-2.5 rounded-[8px] bg-white/[0.04] px-2 py-2.5">
            <div className="flex h-[30px] w-[30px] shrink-0 items-center justify-center rounded-full border border-[rgba(201,149,42,0.45)] bg-[var(--color-accent-sub)] font-display text-[13px] text-[var(--color-accent)]">
              {fullName.charAt(0).toUpperCase()}
            </div>
            <div className="min-w-0">
              <div className="truncate text-[12px] font-semibold text-[#F9F8F6]">{fullName}</div>
              <div className="text-[10px] text-[var(--color-sidebar-text)]">{user.isAdmin ? 'Administrator' : 'User'}</div>
            </div>
          </div>
        </div>
      )}

      <nav className="flex-1 overflow-auto px-2.5 pt-2.5">
        <SectionLabel>Navigation</SectionLabel>
        {navItems.map(({ label, href, icon: Icon }) => (
          <NavLink key={href} href={href} active={isActive(href)} icon={<Icon />}>{label}</NavLink>
        ))}

        <button
          type="button"
          className={cn(navClass(attendanceActive), 'w-full')}
          onClick={() => setAttendanceOpen((open) => !open)}
        >
          <AttendanceIcon className={attendanceActive ? 'text-[var(--color-accent)]' : 'text-[var(--color-sidebar-text)]'} />
          <span className="flex-1">Attendance</span>
          <ChevronDIcon className={cn('transition-transform duration-150', attendanceOpen && 'rotate-180')} />
        </button>
        {(attendanceOpen || attendanceActive) && (
          <div className="ml-2.5 border-l border-[var(--color-sidebar-border)] pl-[14px]">
            {attendanceItems.map(({ label, href, icon: Icon }) => (
              <Link
                key={href}
                href={href}
                className={cn(
                  'mb-px flex items-center gap-2 rounded-[6px] px-2 py-1.5 text-[12px] transition-colors duration-150',
                  isActive(href) ? 'bg-[rgba(201,149,42,0.08)] font-semibold text-[var(--color-accent)]' : 'text-[var(--color-sidebar-text)] hover:bg-white/[0.04]'
                )}
              >
                <Icon size={14} />
                {label}
              </Link>
            ))}
          </div>
        )}

        <div className="mx-1.5 my-2.5 h-px bg-[var(--color-sidebar-border)]" />
        <SectionLabel>Account</SectionLabel>
        <button
          type="button"
          onClick={() => {
            sessionLogout();
            profileLogout();
          }}
          className={cn(navClass(false), 'w-full')}
        >
          <SignoutIcon className="text-[var(--color-sidebar-text)]" />
          <span>Sign Out</span>
        </button>
      </nav>

      <div className="border-t border-[var(--color-sidebar-border)] px-5 py-2.5 text-center text-[10px] text-[rgba(156,163,175,0.3)]">
        v1.0.0 · Hillsong Admin
      </div>
    </aside>
  );
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return <div className="px-2.5 pb-2 pt-1.5 text-[10px] font-semibold uppercase tracking-[1.5px] text-[rgba(156,163,175,0.5)]">{children}</div>;
}

function NavLink({ href, active, icon, children }: { href: string; active: boolean; icon: React.ReactNode; children: React.ReactNode }) {
  return (
    <Link href={href} className={navClass(active)}>
      <span className={active ? 'text-[var(--color-accent)]' : 'text-[var(--color-sidebar-text)]'}>{icon}</span>
      <span>{children}</span>
    </Link>
  );
}

function navClass(active: boolean) {
  return cn(
    'mb-px flex cursor-pointer items-center gap-[9px] rounded-[7px] border-l-2 px-2.5 py-[7px] text-[13px] transition-colors duration-150',
    active
      ? 'border-l-[var(--color-sidebar-active-border)] bg-[var(--color-sidebar-active-bg)] font-semibold text-[#F9F8F6]'
      : 'border-l-transparent font-normal text-[var(--color-sidebar-text)] hover:bg-white/[0.04]'
  );
}

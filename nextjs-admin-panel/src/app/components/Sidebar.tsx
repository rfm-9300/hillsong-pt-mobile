'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';
import { useAuth } from '@/app/hooks';
import UserAvatar from './ui/UserAvatar';

interface NavItem {
  label: string;
  href: string;
  icon: string;
  subItems?: NavItem[];
}

const navItems: NavItem[] = [
  {
    label: 'Dashboard',
    href: '/admin/dashboard',
    icon: '📊',
  },
  {
    label: 'Posts',
    href: '/admin/posts',
    icon: '📝',
  },
  {
    label: 'Events',
    href: '/admin/events',
    icon: '🎉',
  },
  {
    label: 'Encounters',
    href: '/admin/encounters',
    icon: '🤝',
  },
  {
    label: 'Calendar',
    href: '/admin/calendar',
    icon: '📅',
  },
  {
    label: 'Users',
    href: '/admin/users',
    icon: '👥',
  },
  {
    label: 'Attendance',
    href: '/admin/attendance',
    icon: '📋',
    subItems: [
      {
        label: 'Overview',
        href: '/admin/attendance',
        icon: '📊',
      },
      {
        label: 'Events',
        href: '/admin/attendance/event',
        icon: '🎉',
      },
      {
        label: 'Services',
        href: '/admin/attendance/service',
        icon: '⛪',
      },
      {
        label: 'Kids Services',
        href: '/admin/attendance/kids-service',
        icon: '👶',
      },
      {
        label: 'Reports',
        href: '/admin/attendance/reports',
        icon: '📈',
      },
    ],
  },
];

const Sidebar: React.FC = () => {
  const pathname = usePathname();
  const { user, loading, logout } = useAuth();

  const isActiveLink = (href: string) => {
    if (href === '/admin/attendance') {
      return pathname === href;
    }
    return pathname.startsWith(href);
  };

  const isParentActive = (item: NavItem) => {
    if (item.subItems) {
      return item.subItems.some(subItem => isActiveLink(subItem.href));
    }
    return isActiveLink(item.href);
  };

  return (
    <aside className="w-64 bg-[#0f172a] text-white flex flex-col h-full hidden md:flex shadow-xl border-r border-[#1e293b]">
      {/* Brand Header */}
      <div className="p-6 border-b border-[#1e293b]/50">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-blue-500/30">
            <span className="text-xl">✝️</span>
          </div>
          <div>
            <h2 className="text-xl font-bold tracking-tight text-white">Admin Panel</h2>
            <p className="text-slate-400 text-xs font-medium">Hillsong PT</p>
          </div>
        </div>
      </div>

      {/* User Profile Mini-Card (Integrated Top) */}
      {!loading && user && (
        <div className="px-4 py-6">
          <div className="relative overflow-hidden bg-[#1e293b] rounded-xl p-4 border border-[#334155]/50 group transition-all duration-300 hover:border-blue-500/50 hover:shadow-lg hover:shadow-blue-900/10">
            {/* Gradient Glow */}
            <div className="absolute top-0 right-0 w-24 h-24 bg-blue-500/10 rounded-full blur-2xl -mr-12 -mt-12 transition-all group-hover:bg-blue-500/20"></div>

            <div className="flex items-center gap-3 relative z-10">
              <UserAvatar user={user} size="md" className="ring-2 ring-offset-2 ring-offset-[#1e293b] ring-blue-500" />
              <div className="overflow-hidden">
                <p className="text-sm font-semibold text-white truncate">
                  {user.firstName || user.email.split('@')[0]}
                </p>
                <p className="text-xs text-slate-400 truncate">
                  {user.isAdmin ? 'Administrator' : 'User'}
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto px-3 py-2 custom-scrollbar">
        <p className="px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">
          Menu
        </p>
        <ul className="space-y-1">
          {navItems.map((item) => (
            <li key={item.href}>
              <Link
                href={item.href}
                className={cn(
                  'flex items-center px-4 py-3 rounded-xl transition-all duration-200 group',
                  isParentActive(item)
                    ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20 font-medium'
                    : 'text-slate-400 hover:text-white hover:bg-[#1e293b]'
                )}
              >
                <span className={cn(
                  "mr-3 text-xl transition-colors",
                  isParentActive(item) ? "text-white" : "text-slate-500 group-hover:text-blue-400"
                )}>{item.icon}</span>
                <span className="flex-1">{item.label}</span>
                {isParentActive(item) && (
                  <div className="w-1.5 h-1.5 bg-white rounded-full ml-2"></div>
                )}
              </Link>

              {/* Sub-navigation */}
              {item.subItems && isParentActive(item) && (
                <ul className="mt-1 ml-4 pl-4 border-l border-[#334155] space-y-1 mb-2">
                  {item.subItems.map((subItem) => (
                    <li key={subItem.href}>
                      <Link
                        href={subItem.href}
                        className={cn(
                          'flex items-center px-3 py-2 rounded-lg text-sm transition-all duration-200',
                          isActiveLink(subItem.href)
                            ? 'text-blue-400 bg-blue-500/10 font-medium'
                            : 'text-slate-400 hover:text-white hover:bg-[#1e293b]/50'
                        )}
                      >
                        <span className="mr-2 text-xs opacity-70">{subItem.icon}</span>
                        <span>{subItem.label}</span>
                      </Link>
                    </li>
                  ))}
                </ul>
              )}
            </li>
          ))}
        </ul>

        {/* Divider */}
        <div className="my-4 border-t border-[#1e293b]/50 mx-4"></div>

        <p className="px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">
          Account
        </p>
        <ul className="space-y-1">
          <li>
            {/* Logout Button as Nav Item */}
            <button
              onClick={logout}
              className="w-full flex items-center px-4 py-3 rounded-xl text-slate-400 hover:text-red-400 hover:bg-red-500/10 transition-all duration-200 group"
            >
              <span className="mr-3 text-xl group-hover:text-red-400 transition-colors">🚪</span>
              <span className="font-medium">Sign Out</span>
            </button>
          </li>
        </ul>
      </nav>

      {/* Footer Version */}
      <div className="p-4 text-center">
        <p className="text-[10px] text-slate-600 font-medium opacity-50">
          v1.0.0 • Hillsong Admin
        </p>
      </div>
    </aside>
  );
};

export default Sidebar;

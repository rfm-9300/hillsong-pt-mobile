'use client';

import React, { useState } from 'react';
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

interface MobileSidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const MobileSidebar: React.FC<MobileSidebarProps> = ({ isOpen, onClose }) => {
  const pathname = usePathname();
  const [expandedItems, setExpandedItems] = useState<string[]>([]);
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

  const toggleExpanded = (href: string) => {
    setExpandedItems(prev =>
      prev.includes(href)
        ? prev.filter(item => item !== href)
        : [...prev, href]
    );
  };

  const handleLinkClick = () => {
    onClose();
  };

  const handleLogout = () => {
    onClose();
    logout();
  };

  return (
    <>
      {/* Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/60 backdrop-blur-sm z-40 md:hidden animate-in fade-in"
          style={{ animationDuration: '200ms' }}
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <div
        className={cn(
          'fixed inset-y-0 left-0 w-72 bg-[#0f172a] text-white flex flex-col transform transition-transform duration-300 ease-in-out z-50 md:hidden shadow-2xl',
          isOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        {/* Header */}
        <div className="p-6 border-b border-[#1e293b]/50 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg flex items-center justify-center shadow-lg">
              <span className="text-sm">✝️</span>
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">Admin</h2>
            </div>
          </div>

          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-2 rounded-lg hover:bg-[#1e293b] transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* User Profile Mini-Card (Integrated Top) */}
        {!loading && user && (
          <div className="px-4 py-4">
            <div className="flex items-center gap-3 p-3 bg-[#1e293b] rounded-xl border border-[#334155]/50">
              <UserAvatar user={user} size="sm" className="ring-2 ring-blue-500" />
              <div className="overflow-hidden">
                <p className="text-sm font-semibold text-white truncate">
                  {user.firstName || user.email.split('@')[0]}
                </p>
                <p className="text-xs text-slate-400 truncate">
                  {user.isAdmin ? 'Admin' : 'User'}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto px-3 py-2 custom-scrollbar">
          <ul className="space-y-1">
            {navItems.map((item) => (
              <li key={item.href}>
                <div className="flex items-center">
                  <Link
                    href={item.href}
                    onClick={handleLinkClick}
                    className={cn(
                      'flex items-center px-4 py-3 rounded-xl transition-all duration-200 flex-1 group',
                      isParentActive(item)
                        ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20'
                        : 'text-slate-400 hover:text-white hover:bg-[#1e293b]'
                    )}
                  >
                    <span className={cn(
                      "mr-3 text-lg transition-colors",
                      isParentActive(item) ? "text-white" : "text-slate-500 group-hover:text-blue-400"
                    )}>{item.icon}</span>
                    <span className="font-medium">{item.label}</span>
                  </Link>

                  {/* Expand/Collapse button for items with sub-items */}
                  {item.subItems && (
                    <button
                      onClick={() => toggleExpanded(item.href)}
                      className="p-3 text-slate-500 hover:text-white rounded-lg hover:bg-[#1e293b] ml-1"
                    >
                      <svg
                        className={cn(
                          'w-4 h-4 transition-transform duration-200',
                          (expandedItems.includes(item.href) || isParentActive(item)) && 'rotate-90'
                        )}
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" />
                      </svg>
                    </button>
                  )}
                </div>

                {/* Sub-navigation */}
                {item.subItems && (expandedItems.includes(item.href) || isParentActive(item)) && (
                  <ul className="mt-1 ml-4 pl-4 border-l border-[#334155] space-y-1">
                    {item.subItems.map((subItem) => (
                      <li key={subItem.href}>
                        <Link
                          href={subItem.href}
                          onClick={handleLinkClick}
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
        </nav>

        {/* Footer Actions */}
        <div className="p-4 border-t border-[#1e293b]/50">
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center px-4 py-3 rounded-xl bg-[#1e293b] text-slate-400 hover:text-red-400 hover:bg-red-500/10 transition-all duration-200 font-medium border border-[#334155]"
          >
            <span className="mr-2">🚪</span>
            Sign Out
          </button>
        </div>
      </div>
    </>
  );
};

export default MobileSidebar;

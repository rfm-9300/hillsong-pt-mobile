'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';

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
    onClose(); // Close mobile sidebar when a link is clicked
  };

  return (
    <>
      {/* Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-40 md:hidden animate-in fade-in"
          style={{ animationDuration: '200ms' }}
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <div
        className={cn(
          'fixed inset-y-0 left-0 w-64 sm:w-72 bg-gray-800 text-white p-4 transform transition-transform duration-300 ease-in-out z-50 md:hidden',
          isOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <div>
            <h2 className="text-lg sm:text-xl font-bold">Admin Panel</h2>
            <p className="text-gray-400 text-xs">Hillsong PT</p>
          </div>
          <button
            onClick={onClose}
            className="text-white hover:text-gray-300 focus:outline-none touch-target p-2 rounded-md hover:bg-gray-700 transition-colors duration-200 active:scale-95"
            aria-label="Close navigation menu"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto">
          <ul className="space-y-2">
            {navItems.map((item) => (
              <li key={item.href}>
                <div className="flex items-center">
                  <Link
                    href={item.href}
                    onClick={handleLinkClick}
                    className={cn(
                      'flex items-center px-3 py-3 rounded-lg transition-all duration-200 flex-1 touch-target active:scale-95',
                      isParentActive(item)
                        ? 'bg-blue-600 text-white'
                        : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                    )}
                  >
                    <span className="mr-3 text-lg">{item.icon}</span>
                    <span className="font-medium">{item.label}</span>
                  </Link>

                  {/* Expand/Collapse button for items with sub-items */}
                  {item.subItems && (
                    <button
                      onClick={() => toggleExpanded(item.href)}
                      className="p-2 text-gray-400 hover:text-white touch-target rounded-md hover:bg-gray-700 transition-colors duration-200 active:scale-95"
                      aria-label={`Toggle ${item.label} submenu`}
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
                  <ul className="mt-2 ml-6 space-y-1">
                    {item.subItems.map((subItem) => (
                      <li key={subItem.href}>
                        <Link
                          href={subItem.href}
                          onClick={handleLinkClick}
                          className={cn(
                            'flex items-center px-3 py-2 rounded-md text-sm transition-all duration-200 touch-target active:scale-95',
                            isActiveLink(subItem.href)
                              ? 'bg-blue-500 text-white'
                              : 'text-gray-400 hover:bg-gray-700 hover:text-gray-200'
                          )}
                        >
                          <span className="mr-2 text-sm">{subItem.icon}</span>
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

        {/* Footer */}
        <div className="mt-6 pt-4 border-t border-gray-700">
          <div className="text-xs text-gray-500 text-center">
            <p>Admin Panel v1.0</p>
          </div>
        </div>
      </div>
    </>
  );
};

export default MobileSidebar;

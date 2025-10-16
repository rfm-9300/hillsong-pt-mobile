'use client';

import React from 'react';
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
    <aside className="w-64 bg-gray-800 text-white p-4 hidden md:block">
      <div className="mb-8">
        <h2 className="text-2xl font-bold">Admin Panel</h2>
        <p className="text-gray-400 text-sm mt-1">Hillsong PT Management</p>
      </div>
      
      <nav>
        <ul className="space-y-2">
          {navItems.map((item) => (
            <li key={item.href}>
              <Link
                href={item.href}
                className={cn(
                  'flex items-center px-3 py-2 rounded-lg transition-colors duration-200',
                  isParentActive(item)
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                )}
              >
                <span className="mr-3 text-lg">{item.icon}</span>
                <span className="font-medium">{item.label}</span>
              </Link>
              
              {/* Sub-navigation for Attendance */}
              {item.subItems && isParentActive(item) && (
                <ul className="mt-2 ml-6 space-y-1">
                  {item.subItems.map((subItem) => (
                    <li key={subItem.href}>
                      <Link
                        href={subItem.href}
                        className={cn(
                          'flex items-center px-3 py-1.5 rounded-md text-sm transition-colors duration-200',
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
      <div className="absolute bottom-4 left-4 right-4">
        <div className="text-xs text-gray-500 text-center">
          <p>Admin Panel v1.0</p>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;

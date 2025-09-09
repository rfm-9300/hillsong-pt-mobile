'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';

export interface BreadcrumbItem {
  label: string;
  href?: string;
  current?: boolean;
}

interface BreadcrumbProps {
  items?: BreadcrumbItem[];
  className?: string;
}

const Breadcrumb: React.FC<BreadcrumbProps> = ({ items, className }) => {
  const pathname = usePathname();

  // Auto-generate breadcrumbs from pathname if items not provided
  const breadcrumbItems = items || generateBreadcrumbsFromPath(pathname);

  if (breadcrumbItems.length <= 1) {
    return null; // Don't show breadcrumbs for single-level pages
  }

  return (
    <nav className={cn('flex', className)} aria-label="Breadcrumb">
      <ol className="inline-flex items-center space-x-1 md:space-x-3">
        {breadcrumbItems.map((item, index) => (
          <li key={index} className="inline-flex items-center">
            {index > 0 && (
              <svg
                className="w-3 h-3 text-gray-400 mx-1"
                aria-hidden="true"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 6 10"
              >
                <path
                  stroke="currentColor"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="m1 9 4-4-4-4"
                />
              </svg>
            )}
            {item.current || !item.href ? (
              <span className="ml-1 text-sm font-medium text-gray-500 md:ml-2">
                {item.label}
              </span>
            ) : (
              <Link
                href={item.href}
                className="inline-flex items-center ml-1 text-sm font-medium text-gray-700 hover:text-blue-600 md:ml-2 transition-colors"
              >
                {index === 0 && (
                  <svg
                    className="w-3 h-3 mr-2.5"
                    aria-hidden="true"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path d="m19.707 9.293-2-2-7-7a1 1 0 0 0-1.414 0l-7 7-2 2a1 1 0 0 0 1.414 1.414L2 10.414V18a2 2 0 0 0 2 2h3a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h3a2 2 0 0 0 2-2v-7.586l.293.293a1 1 0 0 0 1.414-1.414Z" />
                  </svg>
                )}
                {item.label}
              </Link>
            )}
          </li>
        ))}
      </ol>
    </nav>
  );
};

function generateBreadcrumbsFromPath(pathname: string): BreadcrumbItem[] {
  const segments = pathname.split('/').filter(Boolean);
  const breadcrumbs: BreadcrumbItem[] = [];

  // Always start with Dashboard
  breadcrumbs.push({
    label: 'Dashboard',
    href: '/admin/dashboard',
  });

  let currentPath = '';
  
  segments.forEach((segment, index) => {
    currentPath += `/${segment}`;
    
    // Skip 'admin' segment as it's redundant
    if (segment === 'admin') {
      return;
    }

    const isLast = index === segments.length - 1;
    const label = formatSegmentLabel(segment, segments, index);
    
    breadcrumbs.push({
      label,
      href: isLast ? undefined : currentPath,
      current: isLast,
    });
  });

  return breadcrumbs;
}

function formatSegmentLabel(segment: string, segments: string[], index: number): string {
  // Handle dynamic routes (IDs)
  if (segment.match(/^[a-f0-9-]{36}$/) || segment.match(/^\d+$/)) {
    const previousSegment = segments[index - 1];
    if (previousSegment === 'posts') {
      return 'Edit Post';
    } else if (previousSegment === 'events') {
      return 'Edit Event';
    } else if (previousSegment === 'users') {
      return 'Edit User';
    }
    return 'Edit';
  }

  // Handle specific segments
  const segmentMap: Record<string, string> = {
    'dashboard': 'Dashboard',
    'posts': 'Posts',
    'events': 'Events',
    'users': 'Users',
    'attendance': 'Attendance',
    'create': 'Create',
    'edit': 'Edit',
    'event': 'Event Attendance',
    'service': 'Service Attendance',
    'kids-service': 'Kids Service Attendance',
    'reports': 'Reports & Analytics',
  };

  return segmentMap[segment] || segment.charAt(0).toUpperCase() + segment.slice(1);
}

export default Breadcrumb;
'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import Card from './Card';
import { cn } from '@/lib/utils';

interface QuickAction {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  href?: string;
  onClick?: () => void;
  color?: 'blue' | 'green' | 'purple' | 'orange';
}

interface QuickActionsProps {
  actions?: QuickAction[];
  className?: string;
}

const defaultActions: QuickAction[] = [
  {
    id: 'create-post',
    title: 'Create Post',
    description: 'Write a new blog post',
    href: '/admin/posts/create',
    color: 'blue',
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
        />
      </svg>
    ),
  },
  {
    id: 'create-event',
    title: 'Create Event',
    description: 'Schedule a new event',
    href: '/admin/events/create',
    color: 'green',
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
        />
      </svg>
    ),
  },
  {
    id: 'manage-users',
    title: 'Manage Users',
    description: 'View and edit users',
    href: '/admin/users',
    color: 'purple',
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
        />
      </svg>
    ),
  },
  {
    id: 'view-attendance',
    title: 'View Attendance',
    description: 'Check attendance records',
    href: '/admin/attendance',
    color: 'orange',
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
        />
      </svg>
    ),
  },
];

const QuickActions: React.FC<QuickActionsProps> = ({
  actions = defaultActions,
  className,
}) => {
  const router = useRouter();

  const colorClasses = {
    blue: {
      bg: 'bg-blue-50 hover:bg-blue-100',
      icon: 'text-blue-600',
      border: 'border-blue-200',
    },
    green: {
      bg: 'bg-green-50 hover:bg-green-100',
      icon: 'text-green-600',
      border: 'border-green-200',
    },
    purple: {
      bg: 'bg-purple-50 hover:bg-purple-100',
      icon: 'text-purple-600',
      border: 'border-purple-200',
    },
    orange: {
      bg: 'bg-orange-50 hover:bg-orange-100',
      icon: 'text-orange-600',
      border: 'border-orange-200',
    },
  };

  const handleActionClick = (action: QuickAction) => {
    if (action.href) {
      router.push(action.href);
    } else if (action.onClick) {
      action.onClick();
    }
  };

  return (
    <Card className={cn('p-6', className)}>
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {actions.map((action) => {
          const colors = colorClasses[action.color || 'blue'];
          
          return (
            <button
              key={action.id}
              onClick={() => handleActionClick(action)}
              className={cn(
                'p-4 rounded-lg border-2 border-dashed transition-all duration-200 text-left',
                'hover:border-solid hover:shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500',
                colors.bg,
                colors.border
              )}
            >
              <div className="flex items-start space-x-3">
                <div className={cn('flex-shrink-0', colors.icon)}>
                  {action.icon}
                </div>
                <div className="flex-1 min-w-0">
                  <h4 className="text-sm font-medium text-gray-900 mb-1">
                    {action.title}
                  </h4>
                  <p className="text-sm text-gray-500">
                    {action.description}
                  </p>
                </div>
              </div>
            </button>
          );
        })}
      </div>
    </Card>
  );
};

export default QuickActions;
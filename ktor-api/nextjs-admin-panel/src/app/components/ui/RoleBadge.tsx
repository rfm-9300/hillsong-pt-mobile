'use client';

import { User } from '@/lib/types';
import { isUserAdmin, getUserRole } from '@/lib/userUtils';

interface RoleBadgeProps {
  user: User;
  className?: string;
}

export default function RoleBadge({ user, className = '' }: RoleBadgeProps) {
  const isAdmin = isUserAdmin(user);
  const role = getUserRole(user);
  
  return (
    <span
      className={`
        inline-flex
        items-center
        px-2
        py-1
        rounded-full
        text-xs
        font-medium
        ${isAdmin 
          ? 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200' 
          : 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200'
        }
        ${className}
      `}
    >
      {role}
    </span>
  );
}
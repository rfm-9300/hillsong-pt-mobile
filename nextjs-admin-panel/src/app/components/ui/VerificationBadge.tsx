'use client';

import { User } from '@/lib/types';

interface VerificationBadgeProps {
  user: User;
  className?: string;
}

export default function VerificationBadge({ user, className = '' }: VerificationBadgeProps) {
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
        ${user.verified 
          ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' 
          : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        }
        ${className}
      `}
    >
      {user.verified ? 'Verified' : 'Unverified'}
    </span>
  );
}
'use client';

import { User } from '@/lib/types';
import { getUserInitials, getUserAvatarColor } from '@/lib/userUtils';

interface UserAvatarProps {
  user: User;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
}

const sizeClasses = {
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-12 h-12 text-base',
  xl: 'w-16 h-16 text-lg'
};

export default function UserAvatar({ user, size = 'md', className = '' }: UserAvatarProps) {
  const initials = getUserInitials(user);
  const backgroundColor = getUserAvatarColor(user);
  
  return (
    <div
      className={`
        ${sizeClasses[size]}
        rounded-full
        flex
        items-center
        justify-center
        font-semibold
        text-white
        ${className}
      `}
      style={{ backgroundColor }}
      title={`Avatar for ${user.email}`}
    >
      {initials}
    </div>
  );
}
'use client';

import { User } from '@/lib/types';
import { isUserAdmin, getUserRole } from '@/lib/userUtils';
import Badge from './Badge';

export default function RoleBadge({ user, className = '' }: { user: User; className?: string }) {
  const isAdmin = isUserAdmin(user);
  return (
    <Badge color={isAdmin ? 'amber' : 'neutral'} className={className}>
      {getUserRole(user)}
    </Badge>
  );
}

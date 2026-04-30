'use client';

import { User } from '@/lib/types';
import Badge from './Badge';

export default function VerificationBadge({ user, className = '' }: { user: User; className?: string }) {
  return (
    <Badge color={user.verified ? 'green' : 'yellow'} className={className}>
      {user.verified ? 'Verified' : 'Unverified'}
    </Badge>
  );
}

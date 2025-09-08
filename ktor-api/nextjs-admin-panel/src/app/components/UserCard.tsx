'use client';

import { User } from '@/lib/types';
import { getUserDisplayName } from '@/lib/userUtils';
import UserAvatar from './ui/UserAvatar';
import RoleBadge from './ui/RoleBadge';
import VerificationBadge from './ui/VerificationBadge';
import Card from './ui/Card';
import Button from './ui/Button';

interface UserCardProps {
  user: User;
  onEdit?: (user: User) => void;
  onDelete?: (user: User) => void;
}

export default function UserCard({ user, onEdit, onDelete }: UserCardProps) {
  const displayName = getUserDisplayName(user);
  
  return (
    <Card hover className="p-6">
      <div className="flex items-start space-x-4">
        <UserAvatar user={user} size="lg" />
        
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white truncate">
              {displayName}
            </h3>
            <div className="flex space-x-2">
              <RoleBadge user={user} />
              <VerificationBadge user={user} />
            </div>
          </div>
          
          <p className="text-sm text-gray-600 dark:text-gray-400 mb-4 truncate">
            {user.email}
          </p>
          
          <div className="flex space-x-2">
            {onEdit && (
              <Button
                variant="secondary"
                size="sm"
                onClick={() => onEdit(user)}
              >
                Edit
              </Button>
            )}
            {onDelete && (
              <Button
                variant="danger"
                size="sm"
                onClick={() => onDelete(user)}
              >
                Delete
              </Button>
            )}
          </div>
        </div>
      </div>
    </Card>
  );
}
'use client';

import { User } from '@/lib/types';
import { getUserDisplayName, isUserAdmin } from '@/lib/userUtils';
import UserAvatar from './ui/UserAvatar';
import Card from './ui/Card';
import Button from './ui/Button';

interface UserCardProps {
  user: User;
  onEdit?: (user: User) => void;
  onDelete?: (user: User) => void;
}

export default function UserCard({ user, onEdit, onDelete }: UserCardProps) {
  const displayName = getUserDisplayName(user);
  const isAdmin = isUserAdmin(user);

  return (
    <Card hover className="relative overflow-hidden group border border-gray-100 dark:border-gray-700 hover:border-blue-200 dark:hover:border-blue-900 transition-all duration-300">
      {/* Background decoration */}
      <div className="absolute top-0 left-0 w-full h-24 bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-gray-800 dark:to-gray-800 opacity-50 z-0"></div>

      {/* Status Badges - Top Right */}
      <div className="absolute top-3 right-3 flex flex-col items-end space-y-1 z-10">
        {isAdmin && (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 shadow-sm border border-blue-200 dark:border-blue-800">
            Admin
          </span>
        )}
        {user.verified ? (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 shadow-sm border border-green-200 dark:border-green-800">
            Verified
          </span>
        ) : (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300">
            Pending
          </span>
        )}
      </div>

      <div className="relative z-10 p-6 flex flex-col items-center text-center">
        {/* Avatar */}
        <div className="mb-4 transform group-hover:scale-105 transition-transform duration-300">
          <div className="p-1 bg-white dark:bg-gray-800 rounded-full shadow-sm">
            <UserAvatar user={user} size="lg" className="w-20 h-20 text-2xl" />
          </div>
        </div>

        {/* User Info */}
        <div className="w-full mb-6 min-h-[5rem] flex flex-col justify-center">
          <h3 className="text-xl font-bold text-gray-900 dark:text-white leading-tight mb-1 line-clamp-2" title={displayName}>
            {displayName}
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400 truncate px-2" title={user.email}>
            {user.email}
          </p>
          {user.phone && (
            <p className="text-xs text-gray-400 dark:text-gray-500 mt-1 truncate">
              {user.phone}
            </p>
          )}
        </div>

        {/* Actions */}
        <div className="grid grid-cols-2 gap-3 w-full mt-auto">
          {onEdit && (
            <Button
              variant="secondary"
              size="sm"
              onClick={() => onEdit(user)}
              className="w-full bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600 shadow-sm"
            >
              Edit
            </Button>
          )}
          {onDelete && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onDelete(user)}
              className="w-full text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/20"
            >
              Delete
            </Button>
          )}
        </div>
      </div>
    </Card>
  );
}
'use client';

import { useState, useMemo } from 'react';
import { User } from '@/lib/types';
import { getUserDisplayName, isUserAdmin } from '@/lib/userUtils';
import UserCard from './UserCard';
import { Input } from './forms';
import { EmptyState, LoadingOverlay, Alert, AnimatedGrid } from './ui';

interface UsersListProps {
  users: User[];
  loading?: boolean;
  error?: string | null;
  onEdit?: (user: User) => void;
  onDelete?: (user: User) => void;
}

type RoleFilter = 'all' | 'admin' | 'user';
type VerificationFilter = 'all' | 'verified' | 'unverified';

export default function UsersList({ 
  users, 
  loading = false, 
  error, 
  onEdit, 
  onDelete
}: UsersListProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState<RoleFilter>('all');
  const [verificationFilter, setVerificationFilter] = useState<VerificationFilter>('all');

  // Filter and search users
  const filteredUsers = useMemo(() => {
    return users.filter(user => {
      // Search filter
      const displayName = getUserDisplayName(user).toLowerCase();
      const email = user.email.toLowerCase();
      const query = searchQuery.toLowerCase();
      const matchesSearch = displayName.includes(query) || email.includes(query);

      // Role filter
      const matchesRole = roleFilter === 'all' || 
        (roleFilter === 'admin' && isUserAdmin(user)) ||
        (roleFilter === 'user' && !isUserAdmin(user));

      // Verification filter
      const matchesVerification = verificationFilter === 'all' ||
        (verificationFilter === 'verified' && user.verified) ||
        (verificationFilter === 'unverified' && !user.verified);

      return matchesSearch && matchesRole && matchesVerification;
    });
  }, [users, searchQuery, roleFilter, verificationFilter]);

  const totalUsers = users.length;
  const filteredCount = filteredUsers.length;

  if (error) {
    return (
      <Alert 
        type="error" 
        message={error}
        className="mb-6"
      />
    );
  }

  return (
    <div className="space-y-6">
      {/* Search and Filters */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-4 md:p-6 animate-in fade-in" style={{ animationDuration: '300ms' }}>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {/* Search Input */}
          <div>
            <Input
              placeholder="Search by name or email..."
              value={searchQuery}
              onChange={setSearchQuery}
              className="w-full"
            />
          </div>

          {/* Role Filter */}
          <div>
            <select
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value as RoleFilter)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
            >
              <option value="all">All Roles</option>
              <option value="admin">Admins</option>
              <option value="user">Users</option>
            </select>
          </div>

          {/* Verification Filter */}
          <div>
            <select
              value={verificationFilter}
              onChange={(e) => setVerificationFilter(e.target.value as VerificationFilter)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
            >
              <option value="all">All Status</option>
              <option value="verified">Verified</option>
              <option value="unverified">Unverified</option>
            </select>
          </div>
        </div>

        {/* Results Count */}
        <div className="mt-4 text-sm text-gray-600 dark:text-gray-400">
          Showing {filteredCount} of {totalUsers} users
          {(searchQuery || roleFilter !== 'all' || verificationFilter !== 'all') && (
            <button
              onClick={() => {
                setSearchQuery('');
                setRoleFilter('all');
                setVerificationFilter('all');
              }}
              className="ml-2 text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
            >
              Clear filters
            </button>
          )}
        </div>
      </div>

      {/* Loading State */}
      <LoadingOverlay show={loading} message="Loading users..." />

      {/* Users Grid */}
      {!loading && (
        <>
          {filteredUsers.length > 0 ? (
            <AnimatedGrid cols="grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {filteredUsers.map((user) => (
                <UserCard
                  key={user.id}
                  user={user}
                  onEdit={onEdit}
                  onDelete={onDelete}
                />
              ))}
            </AnimatedGrid>
          ) : (
            <EmptyState
              title={searchQuery || roleFilter !== 'all' || verificationFilter !== 'all' 
                ? "No users found" 
                : "No users yet"
              }
              description={searchQuery || roleFilter !== 'all' || verificationFilter !== 'all'
                ? "Try adjusting your search or filter criteria."
                : "Users will appear here once they are added to the system."
              }
              actionText={searchQuery || roleFilter !== 'all' || verificationFilter !== 'all' 
                ? "Clear filters" 
                : undefined
              }
              onAction={searchQuery || roleFilter !== 'all' || verificationFilter !== 'all' 
                ? () => {
                    setSearchQuery('');
                    setRoleFilter('all');
                    setVerificationFilter('all');
                  }
                : undefined
              }
            />
          )}
        </>
      )}
    </div>
  );
}
'use client';

import { useState, useEffect } from 'react';
import { User } from '@/lib/types';
import { api, ENDPOINTS } from '@/lib/api';

interface UseUsersReturn {
  users: User[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  deleteUser: (userId: string) => Promise<boolean>;
}

export function useUsers(): UseUsersReturn {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await api.get<User[]>(ENDPOINTS.USERS);
      setUsers(response || []);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const deleteUser = async (userId: string): Promise<boolean> => {
    try {
      await api.delete(`/users/${userId}`);
      
      // Remove user from local state
      setUsers(prev => prev.filter(user => user.id !== userId));
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete user');
      return false;
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  return {
    users,
    loading,
    error,
    refetch: fetchUsers,
    deleteUser
  };
}
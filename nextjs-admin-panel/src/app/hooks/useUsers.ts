'use client';

import { useState, useEffect } from 'react';
import { api, ENDPOINTS } from '@/lib/api';
import { User, ApiResponse, CreateUserRequest, AdminUpdateUserRequest } from '@/lib/types';

interface UseUsersReturn {
  users: User[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  deleteUser: (userId: string) => Promise<boolean>;
  updateUserAdminStatus: (userId: string, isAdmin: boolean) => Promise<boolean>;
  createUser: (data: CreateUserRequest) => Promise<boolean>;
  updateUser: (userId: string, data: AdminUpdateUserRequest) => Promise<boolean>;
}

export function useUsers(): UseUsersReturn {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.get<ApiResponse<User[]>>(ENDPOINTS.PROFILE_ALL);
      if (response && response.success && Array.isArray(response.data)) {
        setUsers(response.data);
      } else {
        setUsers([]);
        if (response && !response.success) {
          throw new Error(response.message || 'Failed to fetch users');
        }
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const deleteUser = async (userId: string): Promise<boolean> => {
    try {
      await api.delete(ENDPOINTS.PROFILE_DELETE(userId));

      // Remove user from local state
      setUsers(prev => prev.filter(user => user.id !== userId));
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete user');
      return false;
    }
  };

  const updateUserAdminStatus = async (userId: string, isAdmin: boolean): Promise<boolean> => {
    try {
      await api.put(ENDPOINTS.PROFILE_ADMIN_STATUS(userId), { isAdmin });

      // Update local state
      setUsers(prev => prev.map(user =>
        user.id === userId ? { ...user, isAdmin } : user
      ));
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update user status');
      throw err;
    }
  };

  const createUser = async (data: CreateUserRequest): Promise<boolean> => {
    try {
      await api.post(ENDPOINTS.PROFILE_CREATE, data);
      await fetchUsers();
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create user');
      throw err;
    }
  };

  const updateUser = async (userId: string, data: AdminUpdateUserRequest): Promise<boolean> => {
    try {
      await api.put(ENDPOINTS.PROFILE_UPDATE_ADMIN(userId), data);
      await fetchUsers();
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update user');
      throw err;
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
    deleteUser,
    updateUserAdminStatus,
    createUser,
    updateUser
  };
}
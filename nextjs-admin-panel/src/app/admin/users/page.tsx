'use client';

import { useState } from 'react';
import { User } from '@/lib/types';
import {
  UsersList,
  DeleteConfirmationModal,
  UserEditModal,
  Alert,
  NavigationHeader
} from '@/app/components';
import { useUsers } from '@/app/hooks';
import { getUserDisplayName } from '@/lib/userUtils';

export default function UsersPage() {
  const { users, loading, error, refetch, deleteUser, updateUserAdminStatus } = useUsers();
  const [userToDelete, setUserToDelete] = useState<User | null>(null);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [updateLoading, setUpdateLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleEdit = (user: User) => {
    setEditingUser(user);
  };

  const handleUserUpdate = async (userId: string, isAdmin: boolean) => {
    setUpdateLoading(true);
    try {
      await updateUserAdminStatus(userId, isAdmin);
      setSuccessMessage(`User permissions updated successfully.`);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch {
      // Error is handled in the modal
    } finally {
      setUpdateLoading(false);
    }
  };

  const handleDeleteClick = (user: User) => {
    setUserToDelete(user);
    setDeleteError(null);
  };

  const handleDeleteConfirm = async () => {
    if (!userToDelete) return;

    setDeleteLoading(true);
    setDeleteError(null);

    const success = await deleteUser(userToDelete.id);

    if (success) {
      setSuccessMessage(`User ${getUserDisplayName(userToDelete)} has been deleted successfully.`);
      setUserToDelete(null);

      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(null), 3000);
    } else {
      setDeleteError('Failed to delete user. Please try again.');
    }

    setDeleteLoading(false);
  };

  const handleDeleteCancel = () => {
    setUserToDelete(null);
    setDeleteError(null);
  };

  return (
    <div className="space-y-6">
      <NavigationHeader
        title="Users Management"
        subtitle="Manage system users, roles, and permissions"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Users', current: true },
        ]}
      />

      {successMessage && (
        <Alert
          type="success"
          message={successMessage}
          onClose={() => setSuccessMessage(null)}
        />
      )}

      {deleteError && (
        <Alert
          type="error"
          message={deleteError}
          onClose={() => setDeleteError(null)}
        />
      )}

      <UsersList
        users={users}
        loading={loading}
        error={error}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
      />

      {/* Delete Confirmation Modal */}
      <DeleteConfirmationModal
        show={!!userToDelete}
        title="Delete User"
        message={userToDelete ?
          `Are you sure you want to delete ${getUserDisplayName(userToDelete)}? This action cannot be undone.` :
          ''
        }
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        loading={deleteLoading}
      />

      {/* Edit User Modal */}
      <UserEditModal
        user={editingUser}
        show={!!editingUser}
        onClose={() => setEditingUser(null)}
        onSave={handleUserUpdate}
        loading={updateLoading}
      />
    </div>
  );
}
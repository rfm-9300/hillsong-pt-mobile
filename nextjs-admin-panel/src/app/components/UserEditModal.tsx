import { useState, useEffect } from 'react';
import { User } from '@/lib/types';
import { Modal, Button, Alert } from './ui';
import { Checkbox } from './forms';
import { getUserDisplayName } from '@/lib/userUtils';

interface UserEditModalProps {
    user: User | null;
    show: boolean;
    onClose: () => void;
    onSave: (userId: string, isAdmin: boolean) => Promise<void>;
    loading?: boolean;
}

export default function UserEditModal({
    user,
    show,
    onClose,
    onSave,
    loading = false
}: UserEditModalProps) {
    const [isAdmin, setIsAdmin] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (user) {
            setIsAdmin(user.isAdmin);
            setError(null);
        }
    }, [user]);

    if (!user) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!user) return;

        try {
            await onSave(user.id, isAdmin);
            onClose();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to update user');
        }
    };

    return (
        <Modal
            show={show}
            title={`Edit User: ${getUserDisplayName(user)}`}
            onClose={onClose}
        >
            <form onSubmit={handleSubmit} className="space-y-6">
                {error && (
                    <Alert
                        type="error"
                        message={error}
                        onClose={() => setError(null)}
                    />
                )}

                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                            Email
                        </label>
                        <div className="text-gray-900 dark:text-white px-3 py-2 bg-gray-50 dark:bg-gray-700 rounded-md border border-gray-300 dark:border-gray-600">
                            {user.email}
                        </div>
                    </div>

                    <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-md border border-blue-100 dark:border-blue-800">
                        <h4 className="text-sm font-medium text-blue-900 dark:text-blue-100 mb-2">
                            Permissions
                        </h4>
                        <Checkbox
                            label="Grant Admin Privileges"
                            checked={isAdmin}
                            onChange={setIsAdmin}
                        />
                        <p className="mt-2 text-xs text-blue-700 dark:text-blue-300">
                            Admins have full access to manage users, events, and system settings.
                        </p>
                    </div>
                </div>

                <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200 dark:border-gray-700">
                    <Button
                        variant="ghost"
                        onClick={onClose}
                        disabled={loading}
                        type="button"
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        loading={loading}
                    >
                        Save Changes
                    </Button>
                </div>
            </form>
        </Modal>
    );
}

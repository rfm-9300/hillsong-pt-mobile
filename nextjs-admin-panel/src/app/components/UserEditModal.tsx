import { useState, useEffect } from 'react';
import { User, AdminUpdateUserRequest } from '@/lib/types';
import { Modal, Button, Alert } from './ui';
import { Checkbox, Input } from './forms';
import { getUserDisplayName } from '@/lib/userUtils';

interface UserEditModalProps {
    user: User | null;
    show: boolean;
    onClose: () => void;
    onSave: (userId: string, data: AdminUpdateUserRequest) => Promise<void>;
    loading?: boolean;
}

export default function UserEditModal({
    user,
    show,
    onClose,
    onSave,
    loading = false
}: UserEditModalProps) {
    const [formData, setFormData] = useState<AdminUpdateUserRequest>({
        firstName: '',
        lastName: '',
        phone: '',
        isAdmin: false
    });
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (user) {
            setFormData({
                firstName: user.firstName || '',
                lastName: user.lastName || '',
                phone: user.phone || '',
                isAdmin: user.isAdmin
            });
            setError(null);
        }
    }, [user]);

    if (!user) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await onSave(user.id, formData);
            onClose();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to update user');
        }
    };

    const handleChange = (field: keyof AdminUpdateUserRequest, value: string | boolean) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));
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
                        <div className="text-gray-900 dark:text-white px-3 py-2 bg-gray-50 dark:bg-gray-700 rounded-md border border-gray-300 dark:border-gray-600 opacity-70">
                            {user.email} <span className="text-xs ml-2 text-gray-500">(Cannot be changed)</span>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <Input
                            label="First Name"
                            value={formData.firstName || ''}
                            onChange={(value) => handleChange('firstName', value)}
                            placeholder="John"
                        />
                        <Input
                            label="Last Name"
                            value={formData.lastName || ''}
                            onChange={(value) => handleChange('lastName', value)}
                            placeholder="Doe"
                        />
                    </div>

                    <Input
                        label="Phone"
                        value={formData.phone || ''}
                        onChange={(value) => handleChange('phone', value)}
                        placeholder="+1234567890"
                    />

                    <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-md border border-blue-100 dark:border-blue-800">
                        <h4 className="text-sm font-medium text-blue-900 dark:text-blue-100 mb-2">
                            Permissions
                        </h4>
                        <Checkbox
                            label="Grant Admin Privileges"
                            checked={!!formData.isAdmin}
                            onChange={(checked) => handleChange('isAdmin', checked)}
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

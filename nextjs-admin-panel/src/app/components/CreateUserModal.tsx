import { useState } from 'react';
import { CreateUserRequest } from '@/lib/types';
import { Modal, Button, Alert } from './ui';
import { Checkbox, Input } from './forms';

interface CreateUserModalProps {
    show: boolean;
    onClose: () => void;
    onSave: (data: CreateUserRequest) => Promise<void>;
    loading?: boolean;
}

export default function CreateUserModal({
    show,
    onClose,
    onSave,
    loading = false
}: CreateUserModalProps) {
    const [formData, setFormData] = useState<CreateUserRequest>({
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        phone: '',
        isAdmin: false
    });
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        // Basic validation
        if (!formData.email || !formData.password || !formData.firstName || !formData.lastName) {
            setError('Please fill in all required fields');
            return;
        }

        if (formData.password.length < 8) {
            setError('Password must be at least 8 characters long');
            return;
        }

        try {
            await onSave(formData);
            // Reset form on success (the modal will stick around until parent closes it, or we can reset here)
            setFormData({
                email: '',
                password: '',
                firstName: '',
                lastName: '',
                phone: '',
                isAdmin: false
            });
            onClose();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to create user');
        }
    };

    const handleChange = (field: keyof CreateUserRequest, value: string | boolean) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));
    };

    return (
        <Modal
            show={show}
            title="Create New User"
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
                    <Input
                        label="Email"
                        type="email"
                        value={formData.email}
                        onChange={(value) => handleChange('email', value)}
                        placeholder="user@example.com"
                        required
                    />

                    <div>
                        <Input
                            label="Password"
                            type="password"
                            value={formData.password || ''}
                            onChange={(value) => handleChange('password', value)}
                            placeholder="••••••••"
                            required
                        />
                        <p className="mt-1 text-xs text-gray-500">Must be at least 8 characters</p>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <Input
                            label="First Name"
                            value={formData.firstName}
                            onChange={(value) => handleChange('firstName', value)}
                            placeholder="John"
                            required
                        />
                        <Input
                            label="Last Name"
                            value={formData.lastName}
                            onChange={(value) => handleChange('lastName', value)}
                            placeholder="Doe"
                            required
                        />
                    </div>

                    <Input
                        label="Phone"
                        value={formData.phone || ''}
                        onChange={(value) => handleChange('phone', value)}
                        placeholder="+1234567890"
                    />

                    <div className="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-md border border-gray-200 dark:border-gray-600">
                        <Checkbox
                            label="Grant Admin Privileges"
                            checked={!!formData.isAdmin}
                            onChange={(checked) => handleChange('isAdmin', checked)}
                        />
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
                        Create User
                    </Button>
                </div>
            </form>
        </Modal>
    );
}

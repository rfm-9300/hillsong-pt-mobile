'use client';

import { User } from '@/lib/types';
import { getUserDisplayName, isUserAdmin } from '@/lib/userUtils';
import UserAvatar from './ui/UserAvatar';
import Button from './ui/Button';

interface UsersTableProps {
    users: User[];
    onEdit?: (user: User) => void;
    onDelete?: (user: User) => void;
}

export default function UsersTable({ users, onEdit, onDelete }: UsersTableProps) {
    const formatDate = (dateString?: string) => {
        if (!dateString) return 'N/A';
        try {
            return new Date(dateString).toLocaleDateString(undefined, {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch {
            return 'Invalid Date';
        }
    };

    return (
        <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 shadow-sm animate-in fade-in zoom-in-95 duration-300">
            <table className="w-full text-left text-sm whitespace-nowrap">
                <thead className="bg-gray-50 dark:bg-gray-700/50 border-b border-gray-200 dark:border-gray-700">
                    <tr>
                        <th className="px-6 py-4 font-semibold text-gray-900 dark:text-gray-100">User</th>
                        <th className="px-6 py-4 font-semibold text-gray-900 dark:text-gray-100">Contact</th>
                        <th className="px-6 py-4 font-semibold text-gray-900 dark:text-gray-100">Role</th>
                        <th className="px-6 py-4 font-semibold text-gray-900 dark:text-gray-100">Status</th>
                        <th className="px-6 py-4 font-semibold text-gray-900 dark:text-gray-100">Joined</th>
                        <th className="px-6 py-4 font-semibold text-right text-gray-900 dark:text-gray-100">Actions</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                    {users.map((user) => {
                        const isAdmin = isUserAdmin(user);
                        return (
                            <tr
                                key={user.id}
                                className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors duration-150"
                            >
                                <td className="px-6 py-4">
                                    <div className="flex items-center space-x-3">
                                        <UserAvatar user={user} size="sm" />
                                        <div>
                                            <div className="font-medium text-gray-900 dark:text-white">
                                                {getUserDisplayName(user)}
                                            </div>
                                            <div className="text-xs text-gray-500 dark:text-gray-400">
                                                {user.email}
                                            </div>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4 text-gray-600 dark:text-gray-300">
                                    {user.phone || <span className="text-gray-400 italic">No phone</span>}
                                </td>
                                <td className="px-6 py-4">
                                    {isAdmin ? (
                                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 border border-blue-200 dark:border-blue-800">
                                            Admin
                                        </span>
                                    ) : (
                                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-600">
                                            User
                                        </span>
                                    )}
                                </td>
                                <td className="px-6 py-4">
                                    {user.verified ? (
                                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
                                            <span className="w-1.5 h-1.5 bg-green-500 rounded-full mr-1.5"></span>
                                            Verified
                                        </span>
                                    ) : (
                                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200">
                                            <span className="w-1.5 h-1.5 bg-yellow-500 rounded-full mr-1.5"></span>
                                            Pending
                                        </span>
                                    )}
                                </td>
                                <td className="px-6 py-4 text-gray-600 dark:text-gray-300">
                                    {formatDate(user.joinedAt?.toString())}
                                </td>
                                <td className="px-6 py-4 text-right">
                                    <div className="flex justify-end items-center space-x-2">
                                        {onEdit && (
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => onEdit(user)}
                                                className="text-blue-600 hover:text-blue-700 hover:bg-blue-50 dark:hover:bg-blue-900/20"
                                            >
                                                Edit
                                            </Button>
                                        )}
                                        {onDelete && (
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => onDelete(user)}
                                                className="text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/20"
                                            >
                                                Delete
                                            </Button>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}

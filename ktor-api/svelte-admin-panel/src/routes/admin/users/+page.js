import { error } from '@sveltejs/kit';
import { browser } from '$app/environment';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
    if (browser) {
        const token = localStorage.getItem('authToken');
        if (!token) {
            throw error(401, 'Unauthorized');
        }
        const response = await fetch('/api/users', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (response.ok) {
            const data = await response.json();
            if (data.data && data.data.users) {
                return {
                    users: data.data.users
                };
            }
            throw error(404, 'Users not found');
        }
        throw error(response.status, 'Failed to fetch users');
    }
    return { users: [] }; // Return empty array if not in browser environment
}
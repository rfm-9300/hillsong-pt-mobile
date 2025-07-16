import { error } from '@sveltejs/kit';
import { browser } from '$app/environment';
import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
    if (browser) {
        const token = localStorage.getItem('authToken');
        if (!token) {
            throw error(401, 'Unauthorized');
        }
        try {
            const data = await api.getUsers(fetch);
            if (data.data && data.data.users) {
                return {
                    users: data.data.users
                };
            }
            throw error(404, 'Users not found');
        } catch (err) {
            throw error(err.status || 500, err.message || 'Failed to fetch users');
        }
    }
    return { users: [] }; // Return empty array if not in browser environment
}
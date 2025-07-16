import { redirect } from '@sveltejs/kit';
import { browser } from '$app/environment';
import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
    if (browser) {
        const token = localStorage.getItem('authToken');
        if (!token) {
            throw redirect(307, '/login');
        }
    }
    try {
        const stats = {
            posts: { count: 0, loading: false },
            events: { count: 0, loading: false },
            users: { count: 0, loading: false }
        };

        // Use Promise.allSettled to prevent one failed request from failing the whole load
        const results = await Promise.allSettled([
            api.get(api.endpoints.POSTS, fetch),
            api.get(api.endpoints.EVENTS, fetch),
            api.get(api.endpoints.USERS, fetch)
        ]);

        // Handle each result independently
        if (results[0].status === 'fulfilled') {
            stats.posts.count = results[0].value?.data?.postList?.length || 0;
        }
        
        if (results[1].status === 'fulfilled') {
            stats.events.count = results[1].value?.data?.length || 0;
        }
        
        if (results[2].status === 'fulfilled') {
            stats.users.count = results[2].value?.data?.users?.length || 0;
        }

        return { stats };
    } catch (error) {
        console.error('Error fetching data:', error);
        return {
            stats: {
                posts: { count: 0, loading: false },
                events: { count: 0, loading: false },
                users: { count: 0, loading: false }
            }
        };
    }
}
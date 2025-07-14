import { redirect } from '@sveltejs/kit';
import { browser } from '$app/environment';

/** @type {import('./$types').PageLoad} */
export async function load() {
    if (browser) {
        const token = localStorage.getItem('authToken');
        if (!token) {
            throw redirect(307, '/login');
        }
    }
    try {
        const token = 'your-token-here'; // Replace with actual token retrieval logic, e.g., from cookies or session
        const stats = {
            posts: { count: 0, loading: false },
            events: { count: 0, loading: false },
            users: { count: 0, loading: false }
        };

        // Fetch posts count
        const postsRes = await fetch('/api/posts', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (postsRes.ok) {
            const postsData = await postsRes.json();
            stats.posts.count = postsData.data?.postList?.length || 0;
        }

        // Fetch events count
        const eventsRes = await fetch('/api/events', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (eventsRes.ok) {
            const eventsData = await eventsRes.json();
            stats.events.count = eventsData.data?.length || 0;
        }

        // Fetch users count
        const usersRes = await fetch('/api/users', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (usersRes.ok) {
            const usersData = await usersRes.json();
            stats.users.count = usersData.data?.users?.length || 0;
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
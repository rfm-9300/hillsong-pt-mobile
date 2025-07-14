import { error } from '@sveltejs/kit';
import { apiFetch } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
    try {
        const data = await apiFetch('/posts', { customFetch: fetch });
        
        if (data.data && data.data.postList) {
            return {
                posts: data.data.postList
            };
        }
        throw error(404, 'Posts not found');
    } catch (err) {
        throw error(err.status || 500, err.message || 'Failed to fetch posts');
    }
}
import { error } from '@sveltejs/kit';
import { apiFetch } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ params, fetch }) {
    const postId = params.id;
    
    try {
        const data = await apiFetch(`/posts/${postId}`, { customFetch: fetch });
        
        if (data.data && data.data.post) {
            return {
                post: data.data.post
            };
        }
        throw error(404, 'Post not found');
    } catch (err) {
        throw error(err.status || 500, err.message || 'Failed to fetch post');
    }
}
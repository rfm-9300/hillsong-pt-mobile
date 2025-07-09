import { error } from '@sveltejs/kit';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
    const response = await fetch('/api/posts');
    if (response.ok) {
        const data = await response.json();
        if (data.data && data.data.postList) {
            return {
                posts: data.data.postList
            };
        }
        throw error(404, 'Posts not found');
    }
    throw error(response.status, 'Failed to fetch posts');
}
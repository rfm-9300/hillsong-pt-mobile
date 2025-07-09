import { error } from '@sveltejs/kit';

/** @type {import('./$types').PageLoad} */
export async function load({ params, fetch }) {
    const postId = params.id;
    const response = await fetch(`/api/posts/${postId}`);

    if (response.ok) {
        const data = await response.json();
        if (data.data && data.data.post) {
            return {
                post: data.data.post
            };
        }
        throw error(404, 'Post not found');
    }
    throw error(response.status, 'Failed to fetch post');
}
import { error } from '@sveltejs/kit';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
    const response = await fetch('/api/events');
    if (response.ok) {
        const data = await response.json();
        if (data.data) {
            return {
                events: data.data
            };
        }
        throw error(404, 'Events not found');
    }
    throw error(response.status, 'Failed to fetch events');
}
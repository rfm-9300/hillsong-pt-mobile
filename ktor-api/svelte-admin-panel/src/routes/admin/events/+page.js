import { error } from '@sveltejs/kit';
import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export const ssr = false; // Disable server-side rendering for this route
export async function load({ fetch }) {
    try {
        const data = await api.get(api.endpoints.EVENTS, fetch);
        if (data.data && data.data.events) {
            return {
                events: data.data.events
            };
        }
        throw error(404, 'Events not found');
    } catch (err) {
        throw error(err.status || 500, err.message || 'Failed to fetch events');
    }
}
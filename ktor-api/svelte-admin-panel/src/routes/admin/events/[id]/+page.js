import { error } from '@sveltejs/kit';
import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export const ssr = false; // Disable server-side rendering for this route
export async function load({ params, fetch }) {
    const eventId = params.id;

    try {
        const data = await api.get(api.endpoints.EVENT_BY_ID(eventId), fetch);
        if (data.data && data.data.event) {
            return {
                event: data.data.event
            };
        } else if (data.data) {
            // Fallback to the old structure if the new one isn't available
            return {
                event: data.data
            };
        }
        throw error(404, 'Event not found');
    } catch (err) {
        throw error(err.status || 500, err.message || 'Failed to fetch event');
    }
}
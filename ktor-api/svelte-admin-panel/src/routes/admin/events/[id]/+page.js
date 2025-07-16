import { error } from '@sveltejs/kit';
import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ params, fetch }) {
    const eventId = params.id;
    
    try {
        const data = await api.getEvent(eventId, fetch);
        if (data.data) {
            return {
                event: data.data
            };
        }
        throw error(404, 'Event not found');
    } catch (err) {
        throw error(err.status || 500, err.message || 'Failed to fetch event');
    }
}
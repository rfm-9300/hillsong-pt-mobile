import { error } from '@sveltejs/kit';

/** @type {import('./$types').PageLoad} */
export async function load({ params, fetch }) {
    const eventId = params.id;
    const response = await fetch(`/api/events/${eventId}`);

    if (response.ok) {
        const data = await response.json();
        if (data.data) {
            return {
                event: data.data
            };
        }
        throw error(404, 'Event not found');
    }
    throw error(response.status, 'Failed to fetch event');
}
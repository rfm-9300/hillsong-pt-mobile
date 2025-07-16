import { error } from '@sveltejs/kit';
import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
    try {
        const data = await api.getEvents(fetch);
        if (data.data) {
            return {
                events: data.data
            };
        }
        throw error(404, 'Events not found');
    } catch (err) {
        throw error(err.status || 500, err.message || 'Failed to fetch events');
    }
}
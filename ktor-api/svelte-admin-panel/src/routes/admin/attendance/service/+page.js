import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
    try {
        console.log('Fetching services from API endpoint...');
        const response = await api.get(api.endpoints.SERVICES, fetch);
        console.log('Services API response:', response);

        // Extract services from the response - handle different possible response structures
        let services = [];

        if (response) {
            if (Array.isArray(response)) {
                services = response;
            } else if (response.data) {
                if (Array.isArray(response.data)) {
                    services = response.data;
                } else if (response.data.services && Array.isArray(response.data.services)) {
                    services = response.data.services;
                }
            } else if (response.services && Array.isArray(response.services)) {
                services = response.services;
            }
        }

        console.log(`Services fetched successfully: ${services.length} services found`);
        console.log('Services data:', services);

        return {
            services
        };
    } catch (err) {
        console.error('Error fetching services:', err);
        return {
            services: [],
            error: err.message || 'Failed to load services'
        };
    }
}
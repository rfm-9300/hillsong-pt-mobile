import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
    try {
        console.log('Fetching kids services from API endpoint...');
        const response = await api.get(api.endpoints.KIDS_SERVICES, fetch);
        console.log('Kids services API response:', response);
        
        // Extract kids services from the response - handle different possible response structures
        let kidsServices = [];
        
        if (response) {
            if (Array.isArray(response)) {
                kidsServices = response;
            } else if (response.data) {
                if (Array.isArray(response.data)) {
                    kidsServices = response.data;
                } else if (response.data.kidsServices && Array.isArray(response.data.kidsServices)) {
                    kidsServices = response.data.kidsServices;
                }
            } else if (response.kidsServices && Array.isArray(response.kidsServices)) {
                kidsServices = response.kidsServices;
            }
        }
        
        console.log(`Kids services fetched successfully: ${kidsServices.length} services found`);
        console.log('Kids services data:', kidsServices);
        
        return {
            kidsServices
        };
    } catch (err) {
        console.error('Error fetching kids services:', err);
        return {
            kidsServices: [],
            error: err.message || 'Failed to load kids services'
        };
    }
}
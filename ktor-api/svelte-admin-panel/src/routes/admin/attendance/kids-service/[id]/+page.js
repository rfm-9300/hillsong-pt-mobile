import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ params, fetch }) {
    const kidsServiceId = params.id;
    
    try {
        console.log('Fetching kids service details for ID:', kidsServiceId);
        const response = await api.get(api.endpoints.KIDS_SERVICE_BY_ID(kidsServiceId), fetch);
        console.log('Kids service API response:', response);
        
        // Extract kids service from the response
        let kidsService = null;
        if (response && response.data) {
            kidsService = response.data.kidsService || response.data;
        } else if (response && response.kidsService) {
            kidsService = response.kidsService;
        } else if (response) {
            kidsService = response;
        }
        
        if (!kidsService) {
            throw new Error('Kids service not found');
        }
        
        console.log('Kids service loaded successfully:', kidsService);
        
        return {
            kidsService,
            kidsServiceId
        };
    } catch (err) {
        console.error('Error fetching kids service:', err);
        return {
            kidsService: null,
            kidsServiceId,
            error: err.message || 'Failed to load kids service'
        };
    }
}
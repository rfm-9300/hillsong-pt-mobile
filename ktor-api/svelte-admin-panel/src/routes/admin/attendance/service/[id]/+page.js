import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ params, fetch }) {
    const serviceId = params.id;
    
    try {
        console.log(`Fetching attendance data for service ID: ${serviceId}`);
        
        // Fetch service details using the SERVICE_BY_ID endpoint
        const serviceResponse = await api.get(api.endpoints.SERVICE_BY_ID(serviceId), fetch);
        console.log('Service details response:', serviceResponse);
        
        // Fetch attendance for this service using the ATTENDANCE_SERVICE endpoint
        const attendanceResponse = await api.get(api.endpoints.ATTENDANCE_SERVICE(serviceId), fetch);
        console.log('Attendance response:', attendanceResponse);
        
        // Extract service details
        let service = null;
        if (serviceResponse && serviceResponse.data && serviceResponse.data.service) {
            service = serviceResponse.data.service;
        } else if (serviceResponse && serviceResponse.service) {
            service = serviceResponse.service;
        } else if (serviceResponse && serviceResponse.data) {
            service = serviceResponse.data;
        }
        
        // Extract attendance data
        let attendanceData = [];
        if (attendanceResponse && attendanceResponse.data && attendanceResponse.data.attendances) {
            attendanceData = attendanceResponse.data.attendances;
        } else if (attendanceResponse && Array.isArray(attendanceResponse)) {
            attendanceData = attendanceResponse;
        } else if (attendanceResponse && attendanceResponse.data && Array.isArray(attendanceResponse.data)) {
            attendanceData = attendanceResponse.data;
        }
        
        console.log(`Attendance data fetched successfully for service ID ${serviceId}`);
        
        return {
            service,
            attendance: attendanceData,
            serviceId
        };
    } catch (err) {
        console.error(`Error fetching attendance data for service ID ${serviceId}:`, err);
        return {
            service: null,
            attendance: [],
            serviceId,
            error: err.message || 'Failed to load attendance data'
        };
    }
}
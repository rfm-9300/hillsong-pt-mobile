import { api } from '$lib/api';

/** @type {import('./$types').PageLoad} */
export async function load({ params, fetch }) {
    const kidsServiceId = params.id;
    
    try {
        console.log(`Fetching attendance data for kids service ID: ${kidsServiceId}`);
        
        // Fetch kids service details using the KIDS_SERVICE_BY_ID endpoint
        const kidsServiceResponse = await api.get(api.endpoints.KIDS_SERVICE_BY_ID(kidsServiceId), fetch);
        console.log('Kids service details response:', kidsServiceResponse);
        
        // Fetch attendance for this kids service using the ATTENDANCE_KIDS_SERVICE endpoint
        const attendanceResponse = await api.get(api.endpoints.ATTENDANCE_KIDS_SERVICE(kidsServiceId), fetch);
        console.log('Attendance response:', attendanceResponse);
        
        // Extract kids service details
        let kidsService = null;
        if (kidsServiceResponse && kidsServiceResponse.data && kidsServiceResponse.data.kidsService) {
            kidsService = kidsServiceResponse.data.kidsService;
        } else if (kidsServiceResponse && kidsServiceResponse.kidsService) {
            kidsService = kidsServiceResponse.kidsService;
        } else if (kidsServiceResponse && kidsServiceResponse.data) {
            kidsService = kidsServiceResponse.data;
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
        
        console.log(`Attendance data fetched successfully for kids service ID ${kidsServiceId}`);
        
        return {
            kidsService,
            attendance: attendanceData,
            kidsServiceId
        };
    } catch (err) {
        console.error(`Error fetching attendance data for kids service ID ${kidsServiceId}:`, err);
        return {
            kidsService: null,
            attendance: [],
            kidsServiceId,
            error: err.message || 'Failed to load attendance data'
        };
    }
}
import { api } from '../api';
import { EventType, AttendanceStatus } from '../types/attendance';

/**
 * Helper function to validate check-in/check-out requests
 * @param {Object} request - The request object to validate
 * @param {Object} options - Validation options
 * @returns {Object} Validation result with isValid and error properties
 */
const validateAttendanceRequest = (request, options = {}) => {
    const { 
        requireUserId = false,
        requireKidId = false,
        requireAttendanceId = false,
        requireCheckedInBy = false,
        requireCheckedOutBy = false,
        requireStatus = false,
        maxNotesLength = 500
    } = options;
    
    const result = { isValid: true, error: null };
    
    if (!request) {
        result.isValid = false;
        result.error = 'Request data is required';
        return result;
    }
    
    if (requireUserId && !request.userId) {
        result.isValid = false;
        result.error = 'User ID is required';
        return result;
    }
    
    if (requireKidId && !request.kidId) {
        result.isValid = false;
        result.error = 'Kid ID is required';
        return result;
    }
    
    if (requireAttendanceId && !request.attendanceId) {
        result.isValid = false;
        result.error = 'Attendance ID is required';
        return result;
    }
    
    if (requireCheckedInBy && !request.checkedInBy) {
        result.isValid = false;
        result.error = 'Staff member ID (checkedInBy) is required';
        return result;
    }
    
    if (requireCheckedOutBy && !request.checkedOutBy) {
        result.isValid = false;
        result.error = 'Staff member ID (checkedOutBy) is required';
        return result;
    }
    
    if (requireStatus) {
        if (!request.status) {
            result.isValid = false;
            result.error = 'Status is required';
            return result;
        }
        
        const validStatuses = Object.values(AttendanceStatus);
        if (!validStatuses.includes(request.status)) {
            result.isValid = false;
            result.error = `Invalid status: ${request.status}. Must be one of: ${validStatuses.join(', ')}`;
            return result;
        }
    }
    
    // Validate notes if present
    if (request.notes !== undefined) {
        // Trim notes
        const trimmedNotes = request.notes ? request.notes.trim() : '';
        
        // Check length
        if (trimmedNotes.length > maxNotesLength) {
            result.isValid = false;
            result.error = `Notes cannot exceed ${maxNotesLength} characters`;
            return result;
        }
    }
    
    return result;
};

/**
 * Service for managing attendance-related API calls
 */
export const attendanceService = {
    /**
     * Search for users by name or email
     * @param {string} query - The search query
     * @param {number} [limit=10] - Maximum number of results to return
     * @param {Object} [options] - Additional search options
     * @param {boolean} [options.includeAttendanceHistory=false] - Whether to include attendance history
     * @param {EventType} [options.eventType] - Filter by event type
     * @returns {Promise<Array<{id: number, name: string, email: string, attendanceCount?: number, lastAttendance?: string}>>}
     */
    searchUsers: async (query, limit = 10, options = {}) => {
        try {
            let url = `${api.endpoints.USERS}/search?query=${encodeURIComponent(query)}&limit=${limit}`;
            
            if (options.includeAttendanceHistory && options.eventType) {
                url += `&includeAttendanceHistory=true&eventType=${options.eventType}`;
            }
            
            const response = await api.get(url);
            return response.data.users || [];
        } catch (error) {
            console.error('Error searching users:', error);
            throw new Error('Failed to search users. Please try again.');
        }
    },
    
    /**
     * Search for kids by name or parent name
     * @param {string} query - The search query
     * @param {number} [limit=10] - Maximum number of results to return
     * @param {Object} [options] - Additional search options
     * @param {boolean} [options.includeAttendanceHistory=false] - Whether to include attendance history
     * @param {EventType} [options.eventType] - Filter by event type
     * @returns {Promise<Array<{id: number, name: string, parentName: string, attendanceCount?: number, lastAttendance?: string}>>}
     */
    searchKids: async (query, limit = 10, options = {}) => {
        try {
            let url = `${api.endpoints.KIDS}/search?query=${encodeURIComponent(query)}&limit=${limit}`;
            
            if (options.includeAttendanceHistory && options.eventType) {
                url += `&includeAttendanceHistory=true&eventType=${options.eventType}`;
            }
            
            const response = await api.get(url);
            return response.data.kids || [];
        } catch (error) {
            console.error('Error searching kids:', error);
            throw new Error('Failed to search kids. Please try again.');
        }
    },
    
    /**
     * Get recent attendees for an event type
     * @param {EventType} eventType - The type of event
     * @param {number} [limit=5] - Maximum number of results to return
     * @returns {Promise<Array<{id: number, name: string, email?: string, parentName?: string, lastAttendance: string}>>}
     */
    getRecentAttendees: async (eventType, limit = 5) => {
        try {
            const response = await api.get(`${api.endpoints.ATTENDANCE_RECENT}?eventType=${eventType}&limit=${limit}`);
            return response.data.attendees || [];
        } catch (error) {
            console.error('Error fetching recent attendees:', error);
            throw new Error('Failed to fetch recent attendees. Please try again.');
        }
    },
    
    /**
     * Get frequent attendees for an event type
     * @param {EventType} eventType - The type of event
     * @param {number} [limit=5] - Maximum number of results to return
     * @returns {Promise<Array<{id: number, name: string, email?: string, parentName?: string, attendanceCount: number}>>}
     */
    getFrequentAttendees: async (eventType, limit = 5) => {
        try {
            const response = await api.get(`${api.endpoints.ATTENDANCE_FREQUENT}?eventType=${eventType}&limit=${limit}`);
            return response.data.attendees || [];
        } catch (error) {
            console.error('Error fetching frequent attendees:', error);
            throw new Error('Failed to fetch frequent attendees. Please try again.');
        }
    },
    
    /**
     * Get attendance records for an event with pagination
     * @param {number} eventId - The ID of the event
     * @param {Object} options - Pagination and filtering options
     * @param {number} [options.page=1] - Page number
     * @param {number} [options.limit=10] - Items per page
     * @param {string} [options.status] - Filter by status
     * @param {string} [options.search] - Search query
     * @returns {Promise<{attendances: import('../types/attendance').AttendanceWithDetails[], total: number, page: number, limit: number}>}
     */
    getEventAttendance: async (eventId, options = {}) => {
        const { page = 1, limit = 10, status, search } = options;
        let url = `${api.endpoints.ATTENDANCE_EVENT(eventId)}?page=${page}&limit=${limit}`;
        
        if (status) {
            url += `&status=${status}`;
        }
        
        if (search) {
            url += `&search=${encodeURIComponent(search)}`;
        }
        
        try {
            const response = await api.get(url);
            return {
                attendances: response.data.attendances,
                total: response.data.total || response.data.attendances.length,
                page: response.data.page || page,
                limit: response.data.limit || limit
            };
        } catch (error) {
            console.error('Error fetching event attendance:', error);
            throw new Error('Failed to fetch attendance records. Please try again.');
        }
    },

    /**
     * Get attendance records for a service with pagination
     * @param {number} serviceId - The ID of the service
     * @param {Object} options - Pagination and filtering options
     * @param {number} [options.page=1] - Page number
     * @param {number} [options.limit=10] - Items per page
     * @param {string} [options.status] - Filter by status
     * @param {string} [options.search] - Search query
     * @returns {Promise<{attendances: import('../types/attendance').AttendanceWithDetails[], total: number, page: number, limit: number}>}
     */
    getServiceAttendance: async (serviceId, options = {}) => {
        const { page = 1, limit = 10, status, search } = options;
        let url = `${api.endpoints.ATTENDANCE_SERVICE(serviceId)}?page=${page}&limit=${limit}`;
        
        if (status) {
            url += `&status=${status}`;
        }
        
        if (search) {
            url += `&search=${encodeURIComponent(search)}`;
        }
        
        try {
            const response = await api.get(url);
            return {
                attendances: response.data.attendances,
                total: response.data.total || response.data.attendances.length,
                page: response.data.page || page,
                limit: response.data.limit || limit
            };
        } catch (error) {
            console.error('Error fetching service attendance:', error);
            throw new Error('Failed to fetch attendance records. Please try again.');
        }
    },

    /**
     * Get attendance records for a kids service with pagination
     * @param {number} kidsServiceId - The ID of the kids service
     * @param {Object} options - Pagination and filtering options
     * @param {number} [options.page=1] - Page number
     * @param {number} [options.limit=10] - Items per page
     * @param {string} [options.status] - Filter by status
     * @param {string} [options.search] - Search query
     * @returns {Promise<{attendances: import('../types/attendance').AttendanceWithDetails[], total: number, page: number, limit: number}>}
     */
    getKidsServiceAttendance: async (kidsServiceId, options = {}) => {
        const { page = 1, limit = 10, status, search } = options;
        let url = `${api.endpoints.ATTENDANCE_KIDS_SERVICE(kidsServiceId)}?page=${page}&limit=${limit}`;
        
        if (status) {
            url += `&status=${status}`;
        }
        
        if (search) {
            url += `&search=${encodeURIComponent(search)}`;
        }
        
        try {
            const response = await api.get(url);
            return {
                attendances: response.data.attendances,
                total: response.data.total || response.data.attendances.length,
                page: response.data.page || page,
                limit: response.data.limit || limit
            };
        } catch (error) {
            console.error('Error fetching kids service attendance:', error);
            throw new Error('Failed to fetch attendance records. Please try again.');
        }
    },

    /**
     * Get currently checked-in attendees for an event
     * @param {number} eventId - The ID of the event
     * @returns {Promise<import('../types/attendance').AttendanceWithDetails[]>}
     */
    getCurrentlyCheckedInToEvent: async (eventId) => {
        const response = await api.get(api.endpoints.ATTENDANCE_EVENT_CURRENT(eventId));
        return response.data.attendances;
    },

    /**
     * Get currently checked-in attendees for a service
     * @param {number} serviceId - The ID of the service
     * @returns {Promise<import('../types/attendance').AttendanceWithDetails[]>}
     */
    getCurrentlyCheckedInToService: async (serviceId) => {
        const response = await api.get(api.endpoints.ATTENDANCE_SERVICE_CURRENT(serviceId));
        return response.data.attendances;
    },

    /**
     * Get currently checked-in attendees for a kids service
     * @param {number} kidsServiceId - The ID of the kids service
     * @returns {Promise<import('../types/attendance').AttendanceWithDetails[]>}
     */
    getCurrentlyCheckedInToKidsService: async (kidsServiceId) => {
        const response = await api.get(api.endpoints.ATTENDANCE_KIDS_SERVICE_CURRENT(kidsServiceId));
        return response.data.attendances;
    },

    /**
     * Get attendance statistics for an event
     * @param {number} eventId - The ID of the event
     * @returns {Promise<import('../types/attendance').AttendanceStats>}
     */
    getEventAttendanceStats: async (eventId) => {
        const response = await api.get(api.endpoints.ATTENDANCE_EVENT_STATS(eventId));
        return response.data.stats;
    },

    /**
     * Get attendance statistics for a service
     * @param {number} serviceId - The ID of the service
     * @returns {Promise<import('../types/attendance').AttendanceStats>}
     */
    getServiceAttendanceStats: async (serviceId) => {
        const response = await api.get(api.endpoints.ATTENDANCE_SERVICE_STATS(serviceId));
        return response.data.stats;
    },

    /**
     * Get attendance statistics for a kids service
     * @param {number} kidsServiceId - The ID of the kids service
     * @returns {Promise<import('../types/attendance').AttendanceStats>}
     */
    getKidsServiceAttendanceStats: async (kidsServiceId) => {
        const response = await api.get(api.endpoints.ATTENDANCE_KIDS_SERVICE_STATS(kidsServiceId));
        return response.data.stats;
    },

    /**
     * Get attendance history for a user
     * @param {number} userId - The ID of the user
     * @param {string} [startDate] - Optional start date in ISO format
     * @param {string} [endDate] - Optional end date in ISO format
     * @returns {Promise<import('../types/attendance').AttendanceWithDetails[]>}
     */
    getUserAttendanceHistory: async (userId, startDate, endDate) => {
        let endpoint = api.endpoints.ATTENDANCE_USER_HISTORY(userId);
        const params = [];
        
        if (startDate) {
            params.push(`startDate=${encodeURIComponent(startDate)}`);
        }
        
        if (endDate) {
            params.push(`endDate=${encodeURIComponent(endDate)}`);
        }
        
        if (params.length > 0) {
            endpoint += `?${params.join('&')}`;
        }
        
        const response = await api.get(endpoint);
        return response.data.attendances;
    },

    /**
     * Get attendance history for a kid
     * @param {number} kidId - The ID of the kid
     * @param {string} [startDate] - Optional start date in ISO format
     * @param {string} [endDate] - Optional end date in ISO format
     * @returns {Promise<import('../types/attendance').AttendanceWithDetails[]>}
     */
    getKidAttendanceHistory: async (kidId, startDate, endDate) => {
        let endpoint = api.endpoints.ATTENDANCE_KID_HISTORY(kidId);
        const params = [];
        
        if (startDate) {
            params.push(`startDate=${encodeURIComponent(startDate)}`);
        }
        
        if (endDate) {
            params.push(`endDate=${encodeURIComponent(endDate)}`);
        }
        
        if (params.length > 0) {
            endpoint += `?${params.join('&')}`;
        }
        
        const response = await api.get(endpoint);
        return response.data.attendances;
    },

    /**
     * Check in a user to an event
     * @param {number} eventId - The ID of the event
     * @param {import('../types/attendance').CheckInRequest} checkInRequest - The check-in request data
     * @returns {Promise<import('../types/attendance').Attendance>}
     */
    checkInUserToEvent: async (eventId, checkInRequest) => {
        const response = await api.post(
            api.endpoints.ATTENDANCE_EVENT_CHECK_IN(eventId),
            checkInRequest
        );
        return response.data.attendance;
    },

    /**
     * Check in a user to a service
     * @param {number} serviceId - The ID of the service
     * @param {import('../types/attendance').CheckInRequest} checkInRequest - The check-in request data
     * @returns {Promise<import('../types/attendance').Attendance>}
     */
    checkInUserToService: async (serviceId, checkInRequest) => {
        const response = await api.post(
            api.endpoints.ATTENDANCE_SERVICE_CHECK_IN(serviceId),
            checkInRequest
        );
        return response.data.attendance;
    },

    /**
     * Check in a kid to a kids service
     * @param {number} kidsServiceId - The ID of the kids service
     * @param {import('../types/attendance').CheckInRequest} checkInRequest - The check-in request data
     * @returns {Promise<import('../types/attendance').Attendance>}
     */
    checkInKidToKidsService: async (kidsServiceId, checkInRequest) => {
        const response = await api.post(
            api.endpoints.ATTENDANCE_KIDS_SERVICE_CHECK_IN(kidsServiceId),
            checkInRequest
        );
        return response.data.attendance;
    },

    /**
     * Check out a user with enhanced validation and error handling
     * @param {import('../types/attendance').CheckOutRequest} checkOutRequest - The check-out request data
     * @returns {Promise<boolean>}
     * @throws {Error} If the request fails or validation fails
     */
    checkOutUser: async (checkOutRequest) => {
        // Validate parameters
        if (!checkOutRequest) {
            throw new Error('Check-out request data is required');
        }
        
        if (!checkOutRequest.attendanceId) {
            throw new Error('Attendance ID is required');
        }
        
        if (!checkOutRequest.checkedOutBy) {
            throw new Error('Staff member ID (checkedOutBy) is required');
        }
        
        // Trim notes if provided
        if (checkOutRequest.notes) {
            checkOutRequest.notes = checkOutRequest.notes.trim();
            
            // Validate notes length
            if (checkOutRequest.notes.length > 500) {
                throw new Error('Notes cannot exceed 500 characters');
            }
        }
        
        try {
            const response = await api.post(
                api.endpoints.ATTENDANCE_CHECK_OUT,
                checkOutRequest
            );
            
            if (!response || response.success === false) {
                throw new Error('Check-out operation failed');
            }
            
            return response.success;
        } catch (error) {
            // Enhance error messages with context
            if (error.message && error.message.includes('already checked out')) {
                throw new Error('This attendee is already checked out');
            } else if (error.message && error.message.includes('404')) {
                throw new Error('Attendance record not found');
            } else if (error.message && error.message.includes('403')) {
                throw new Error('Permission denied to check out attendee');
            } else if (error.message && error.message.includes('network')) {
                throw new Error('Network error while checking out attendee');
            } else if (error.message && error.message.includes('timeout')) {
                throw new Error('Request timed out while checking out attendee');
            } else {
                throw new Error(`Failed to check out attendee: ${error.message || 'Unknown error'}`);
            }
        }
    },

    /**
     * Check out a kid with enhanced validation and error handling
     * @param {import('../types/attendance').CheckOutRequest} checkOutRequest - The check-out request data
     * @returns {Promise<boolean>}
     * @throws {Error} If the request fails or validation fails
     */
    checkOutKid: async (checkOutRequest) => {
        // Validate parameters
        if (!checkOutRequest) {
            throw new Error('Check-out request data is required');
        }
        
        if (!checkOutRequest.attendanceId) {
            throw new Error('Attendance ID is required');
        }
        
        if (!checkOutRequest.checkedOutBy) {
            throw new Error('Staff member ID (checkedOutBy) is required');
        }
        
        // Trim notes if provided
        if (checkOutRequest.notes) {
            checkOutRequest.notes = checkOutRequest.notes.trim();
            
            // Validate notes length
            if (checkOutRequest.notes.length > 500) {
                throw new Error('Notes cannot exceed 500 characters');
            }
        }
        
        try {
            const response = await api.post(
                api.endpoints.ATTENDANCE_KID_CHECK_OUT,
                checkOutRequest
            );
            
            if (!response || response.success === false) {
                throw new Error('Check-out operation failed');
            }
            
            return response.success;
        } catch (error) {
            // Enhance error messages with context
            if (error.message && error.message.includes('already checked out')) {
                throw new Error('This kid is already checked out');
            } else if (error.message && error.message.includes('404')) {
                throw new Error('Attendance record not found');
            } else if (error.message && error.message.includes('403')) {
                throw new Error('Permission denied to check out kid');
            } else if (error.message && error.message.includes('network')) {
                throw new Error('Network error while checking out kid');
            } else if (error.message && error.message.includes('timeout')) {
                throw new Error('Request timed out while checking out kid');
            } else {
                throw new Error(`Failed to check out kid: ${error.message || 'Unknown error'}`);
            }
        }
    },

    /**
     * Update attendance status with enhanced validation and error handling
     * @param {import('../types/attendance').UpdateAttendanceStatusRequest} updateRequest - The update request data
     * @returns {Promise<boolean>}
     * @throws {Error} If the request fails or validation fails
     */
    updateAttendanceStatus: async (updateRequest) => {
        // Validate parameters
        if (!updateRequest) {
            throw new Error('Update request data is required');
        }
        
        if (!updateRequest.attendanceId) {
            throw new Error('Attendance ID is required');
        }
        
        if (!updateRequest.status) {
            throw new Error('Status is required');
        }
        
        // Validate status value
        const validStatuses = ['CHECKED_IN', 'CHECKED_OUT', 'EMERGENCY', 'NO_SHOW'];
        if (!validStatuses.includes(updateRequest.status)) {
            throw new Error(`Invalid status: ${updateRequest.status}. Must be one of: ${validStatuses.join(', ')}`);
        }
        
        // Trim notes if provided
        if (updateRequest.notes) {
            updateRequest.notes = updateRequest.notes.trim();
            
            // Validate notes length
            if (updateRequest.notes.length > 500) {
                throw new Error('Notes cannot exceed 500 characters');
            }
        }
        
        try {
            const response = await api.put(
                api.endpoints.ATTENDANCE_UPDATE_STATUS,
                updateRequest
            );
            
            if (!response || response.success === false) {
                throw new Error('Status update operation failed');
            }
            
            return response.success;
        } catch (error) {
            // Enhance error messages with context
            if (error.message && error.message.includes('404')) {
                throw new Error('Attendance record not found');
            } else if (error.message && error.message.includes('403')) {
                throw new Error('Permission denied to update attendance status');
            } else if (error.message && error.message.includes('network')) {
                throw new Error('Network error while updating attendance status');
            } else if (error.message && error.message.includes('timeout')) {
                throw new Error('Request timed out while updating attendance status');
            } else {
                throw new Error(`Failed to update attendance status: ${error.message || 'Unknown error'}`);
            }
        }
    },

    /**
     * Update attendance notes with enhanced validation and error handling
     * @param {import('../types/attendance').UpdateAttendanceNotesRequest} updateRequest - The update request data
     * @returns {Promise<boolean>}
     * @throws {Error} If the request fails or validation fails
     */
    updateAttendanceNotes: async (updateRequest) => {
        // Validate parameters
        if (!updateRequest) {
            throw new Error('Update request data is required');
        }
        
        if (!updateRequest.attendanceId) {
            throw new Error('Attendance ID is required');
        }
        
        if (updateRequest.notes === undefined || updateRequest.notes === null) {
            throw new Error('Notes field is required (can be empty string)');
        }
        
        // Trim notes
        updateRequest.notes = updateRequest.notes.trim();
        
        // Validate notes length
        if (updateRequest.notes.length > 500) {
            throw new Error('Notes cannot exceed 500 characters');
        }
        
        try {
            const response = await api.put(
                api.endpoints.ATTENDANCE_UPDATE_NOTES,
                updateRequest
            );
            
            if (!response || response.success === false) {
                throw new Error('Notes update operation failed');
            }
            
            return response.success;
        } catch (error) {
            // Enhance error messages with context
            if (error.message && error.message.includes('404')) {
                throw new Error('Attendance record not found');
            } else if (error.message && error.message.includes('403')) {
                throw new Error('Permission denied to update attendance notes');
            } else if (error.message && error.message.includes('network')) {
                throw new Error('Network error while updating attendance notes');
            } else if (error.message && error.message.includes('timeout')) {
                throw new Error('Request timed out while updating attendance notes');
            } else {
                throw new Error(`Failed to update attendance notes: ${error.message || 'Unknown error'}`);
            }
        }
    },

    /**
     * Check if a user is checked in to an event with enhanced error handling
     * @param {number} eventId - The ID of the event
     * @param {number} userId - The ID of the user
     * @returns {Promise<boolean>}
     * @throws {Error} If the request fails or validation fails
     */
    isUserCheckedInToEvent: async (eventId, userId) => {
        // Validate parameters
        if (!eventId) throw new Error('Event ID is required');
        if (!userId) throw new Error('User ID is required');
        
        try {
            const response = await api.get(
                api.endpoints.ATTENDANCE_EVENT_USER_STATUS(eventId, userId)
            );
            
            // Handle different response formats
            if (response.data) {
                if (typeof response.data === 'string') {
                    try {
                        return JSON.parse(response.data).isCheckedIn;
                    } catch (parseError) {
                        console.error('Error parsing response:', parseError);
                        return false;
                    }
                } else if (typeof response.data === 'object') {
                    return response.data.isCheckedIn === true;
                }
            }
            return false;
        } catch (error) {
            // Enhance error message with context
            if (error.message && error.message.includes('404')) {
                throw new Error(`User or event not found (IDs: ${userId}, ${eventId})`);
            } else if (error.message && error.message.includes('403')) {
                throw new Error('Permission denied to check attendance status');
            } else if (error.message && error.message.includes('network')) {
                throw new Error('Network error while checking attendance status');
            } else {
                throw new Error(`Failed to check if user is checked in: ${error.message || 'Unknown error'}`);
            }
        }
    },

    /**
     * Check if a user is checked in to a service with enhanced error handling
     * @param {number} serviceId - The ID of the service
     * @param {number} userId - The ID of the user
     * @returns {Promise<boolean>}
     * @throws {Error} If the request fails or validation fails
     */
    isUserCheckedInToService: async (serviceId, userId) => {
        // Validate parameters
        if (!serviceId) throw new Error('Service ID is required');
        if (!userId) throw new Error('User ID is required');
        
        try {
            const response = await api.get(
                api.endpoints.ATTENDANCE_SERVICE_USER_STATUS(serviceId, userId)
            );
            
            // Handle different response formats
            if (response.data) {
                if (typeof response.data === 'string') {
                    try {
                        return JSON.parse(response.data).isCheckedIn;
                    } catch (parseError) {
                        console.error('Error parsing response:', parseError);
                        return false;
                    }
                } else if (typeof response.data === 'object') {
                    return response.data.isCheckedIn === true;
                }
            }
            return false;
        } catch (error) {
            // Enhance error message with context
            if (error.message && error.message.includes('404')) {
                throw new Error(`User or service not found (IDs: ${userId}, ${serviceId})`);
            } else if (error.message && error.message.includes('403')) {
                throw new Error('Permission denied to check attendance status');
            } else if (error.message && error.message.includes('network')) {
                throw new Error('Network error while checking attendance status');
            } else {
                throw new Error(`Failed to check if user is checked in: ${error.message || 'Unknown error'}`);
            }
        }
    },

    /**
     * Check if a kid is checked in to a kids service with enhanced error handling
     * @param {number} kidsServiceId - The ID of the kids service
     * @param {number} kidId - The ID of the kid
     * @returns {Promise<boolean>}
     * @throws {Error} If the request fails or validation fails
     */
    isKidCheckedInToKidsService: async (kidsServiceId, kidId) => {
        // Validate parameters
        if (!kidsServiceId) throw new Error('Kids service ID is required');
        if (!kidId) throw new Error('Kid ID is required');
        
        try {
            const response = await api.get(
                api.endpoints.ATTENDANCE_KIDS_SERVICE_KID_STATUS(kidsServiceId, kidId)
            );
            
            // Handle different response formats
            if (response.data) {
                if (typeof response.data === 'string') {
                    try {
                        return JSON.parse(response.data).isCheckedIn;
                    } catch (parseError) {
                        console.error('Error parsing response:', parseError);
                        return false;
                    }
                } else if (typeof response.data === 'object') {
                    return response.data.isCheckedIn === true;
                }
            }
            return false;
        } catch (error) {
            // Enhance error message with context
            if (error.message && error.message.includes('404')) {
                throw new Error(`Kid or kids service not found (IDs: ${kidId}, ${kidsServiceId})`);
            } else if (error.message && error.message.includes('403')) {
                throw new Error('Permission denied to check attendance status');
            } else if (error.message && error.message.includes('network')) {
                throw new Error('Network error while checking attendance status');
            } else {
                throw new Error(`Failed to check if kid is checked in: ${error.message || 'Unknown error'}`);
            }
        }
    },

    /**
     * Get attendance records by event type and ID with pagination
     * @param {EventType} eventType - The type of event
     * @param {number} id - The ID of the event
     * @param {Object} options - Pagination and filtering options
     * @param {number} [options.page=1] - Page number
     * @param {number} [options.limit=10] - Items per page
     * @param {string} [options.status] - Filter by status
     * @param {string} [options.search] - Search query
     * @returns {Promise<{attendances: import('../types/attendance').AttendanceWithDetails[], total: number, page: number, limit: number}>}
     */
    getAttendanceByEventType: async (eventType, id, options = {}) => {
        try {
            switch (eventType) {
                case EventType.EVENT:
                    return attendanceService.getEventAttendance(id, options);
                case EventType.SERVICE:
                    return attendanceService.getServiceAttendance(id, options);
                case EventType.KIDS_SERVICE:
                    return attendanceService.getKidsServiceAttendance(id, options);
                default:
                    throw new Error(`Unsupported event type: ${eventType}`);
            }
        } catch (error) {
            console.error(`Error fetching attendance for ${eventType}:`, error);
            throw new Error(`Failed to fetch attendance records for ${eventType}. Please try again.`);
        }
    },

    /**
     * Get currently checked-in attendees by event type and ID
     * @param {EventType} eventType - The type of event
     * @param {number} id - The ID of the event
     * @returns {Promise<import('../types/attendance').AttendanceWithDetails[]>}
     */
    getCurrentlyCheckedInByEventType: async (eventType, id) => {
        switch (eventType) {
            case EventType.EVENT:
                return attendanceService.getCurrentlyCheckedInToEvent(id);
            case EventType.SERVICE:
                return attendanceService.getCurrentlyCheckedInToService(id);
            case EventType.KIDS_SERVICE:
                return attendanceService.getCurrentlyCheckedInToKidsService(id);
            default:
                throw new Error(`Unsupported event type: ${eventType}`);
        }
    },

    /**
     * Get attendance statistics by event type and ID
     * @param {EventType} eventType - The type of event
     * @param {number} id - The ID of the event
     * @returns {Promise<import('../types/attendance').AttendanceStats>}
     */
    getAttendanceStatsByEventType: async (eventType, id) => {
        switch (eventType) {
            case EventType.EVENT:
                return attendanceService.getEventAttendanceStats(id);
            case EventType.SERVICE:
                return attendanceService.getServiceAttendanceStats(id);
            case EventType.KIDS_SERVICE:
                return attendanceService.getKidsServiceAttendanceStats(id);
            default:
                throw new Error(`Unsupported event type: ${eventType}`);
        }
    },

    /**
     * Check in an attendee by event type with enhanced validation and error handling
     * @param {EventType} eventType - The type of event
     * @param {number} id - The ID of the event
     * @param {import('../types/attendance').CheckInRequest} checkInRequest - The check-in request data
     * @returns {Promise<import('../types/attendance').Attendance>}
     * @throws {Error} If the request fails or validation fails
     */
    checkInByEventType: async (eventType, id, checkInRequest) => {
        // Validate event type
        if (!eventType || !Object.values(EventType).includes(eventType)) {
            throw new Error(`Invalid event type: ${eventType}`);
        }
        
        // Validate ID
        if (!id) {
            throw new Error(`${eventType.toLowerCase()} ID is required`);
        }
        
        // Validate check-in request based on event type
        let validationOptions = {
            requireCheckedInBy: true,
            maxNotesLength: 500
        };
        
        if (eventType === EventType.EVENT || eventType === EventType.SERVICE) {
            validationOptions.requireUserId = true;
        } else if (eventType === EventType.KIDS_SERVICE) {
            validationOptions.requireKidId = true;
        }
        
        const validation = validateAttendanceRequest(checkInRequest, validationOptions);
        if (!validation.isValid) {
            throw new Error(validation.error);
        }
        
        // Trim notes if provided
        if (checkInRequest.notes) {
            checkInRequest.notes = checkInRequest.notes.trim();
        }
        
        try {
            let result;
            
            switch (eventType) {
                case EventType.EVENT:
                    result = await attendanceService.checkInUserToEvent(id, checkInRequest);
                    break;
                case EventType.SERVICE:
                    result = await attendanceService.checkInUserToService(id, checkInRequest);
                    break;
                case EventType.KIDS_SERVICE:
                    result = await attendanceService.checkInKidToKidsService(id, checkInRequest);
                    break;
                default:
                    throw new Error(`Unsupported event type: ${eventType}`);
            }
            
            return result;
        } catch (error) {
            // Enhance error messages with context
            if (error.message && error.message.includes('already checked in')) {
                throw error; // Pass through already formatted error
            } else if (error.message && error.message.includes('404')) {
                throw new Error(`${eventType === EventType.KIDS_SERVICE ? 'Kid' : 'User'} or ${eventType.toLowerCase()} not found`);
            } else if (error.message && error.message.includes('403')) {
                throw new Error('Permission denied to check in attendee');
            } else if (error.message && error.message.includes('network')) {
                throw new Error('Network error while checking in attendee');
            } else if (error.message && error.message.includes('timeout')) {
                throw new Error('Request timed out while checking in attendee');
            } else {
                throw new Error(`Failed to check in attendee: ${error.message || 'Unknown error'}`);
            }
        }
    }
};
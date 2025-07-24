const BASE_URL = '/api';

// API Endpoints
const ENDPOINTS = {
    // Auth
    LOGIN: '/auth/login',

    // Posts
    POSTS: '/posts',
    POST_BY_ID: (id) => `/posts/${id}`,
    POST_CREATE: '/posts/create',
    POST_UPDATE: '/posts/update',
    POST_DELETE: '/posts/delete',

    // Events
    EVENTS: '/events',
    EVENT_BY_ID: (id) => `/events/${id}`,
    EVENT_UPDATE: '/events/update',
    EVENT_DELETE: '/events/delete',

    // Users
    USERS: '/users',
    
    // Kids
    KIDS: '/kids',
    
    // Services
    SERVICES: '/services',
    SERVICE_BY_ID: (id) => `/services/${id}`,
    SERVICE_CREATE: '/services/create',
    SERVICE_UPDATE: '/services/update',
    SERVICE_DELETE: '/services/delete',
    
    // Kids Services
    KIDS_SERVICES: '/kids-services',
    KIDS_SERVICE_BY_ID: (id) => `/kids-services/${id}`,
    
    // Attendance
    ATTENDANCE_EVENT: (eventId) => `/attendance/event/${eventId}`,
    ATTENDANCE_SERVICE: (serviceId) => `/attendance/service/${serviceId}`,
    ATTENDANCE_KIDS_SERVICE: (kidsServiceId) => `/attendance/kids-service/${kidsServiceId}`,
    ATTENDANCE_EVENT_CURRENT: (eventId) => `/attendance/event/${eventId}/current`,
    ATTENDANCE_SERVICE_CURRENT: (serviceId) => `/attendance/service/${serviceId}/current`,
    ATTENDANCE_KIDS_SERVICE_CURRENT: (kidsServiceId) => `/attendance/kids-service/${kidsServiceId}/current`,
    ATTENDANCE_EVENT_STATS: (eventId) => `/attendance/event/${eventId}/stats`,
    ATTENDANCE_SERVICE_STATS: (serviceId) => `/attendance/service/${serviceId}/stats`,
    ATTENDANCE_KIDS_SERVICE_STATS: (kidsServiceId) => `/attendance/kids-service/${kidsServiceId}/stats`,
    ATTENDANCE_USER_HISTORY: (userId) => `/attendance/user/${userId}`,
    ATTENDANCE_KID_HISTORY: (kidId) => `/attendance/kid/${kidId}`,
    ATTENDANCE_EVENT_CHECK_IN: (eventId) => `/attendance/event/${eventId}/check-in`,
    ATTENDANCE_SERVICE_CHECK_IN: (serviceId) => `/attendance/service/${serviceId}/check-in`,
    ATTENDANCE_KIDS_SERVICE_CHECK_IN: (kidsServiceId) => `/attendance/kids-service/${kidsServiceId}/check-in`,
    ATTENDANCE_CHECK_OUT: '/attendance/check-out',
    ATTENDANCE_KID_CHECK_OUT: '/attendance/kid/check-out',
    ATTENDANCE_UPDATE_STATUS: '/attendance/status',
    ATTENDANCE_UPDATE_NOTES: '/attendance/notes',
    ATTENDANCE_EVENT_USER_STATUS: (eventId, userId) => `/attendance/event/${eventId}/user/${userId}/status`,
    ATTENDANCE_SERVICE_USER_STATUS: (serviceId, userId) => `/attendance/service/${serviceId}/user/${userId}/status`,
    ATTENDANCE_KIDS_SERVICE_KID_STATUS: (kidsServiceId, kidId) => `/attendance/kids-service/${kidsServiceId}/kid/${kidId}/status`,
    ATTENDANCE_RECENT: '/attendance/recent',
    ATTENDANCE_FREQUENT: '/attendance/frequent'
};

class ApiClient {
    constructor() {
        this.baseUrl = BASE_URL;
    }

    getAuthToken() {
        if (typeof window !== 'undefined') {
            return localStorage.getItem('authToken');
        }
        return null;
    }

    async request(path, options = {}) {
        const {
            method = 'GET',
            body = null,
            customFetch = typeof fetch !== 'undefined' ? fetch : null,
            isFormData = false,
            timeout = 15000 // Default timeout of 15 seconds
        } = options;

        if (!customFetch) {
            throw new Error('No fetch implementation available');
        }

        const token = this.getAuthToken();
        const headers = {};

        // Only set Content-Type for JSON, let browser set it for FormData
        if (!isFormData) {
            headers['Content-Type'] = 'application/json';
        }

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const fetchOptions = {
            method,
            headers,
        };

        if (body) {
            fetchOptions.body = isFormData ? body : JSON.stringify(body);
        }

        try {
            // Create a promise that rejects after the timeout
            const timeoutPromise = new Promise((_, reject) => {
                setTimeout(() => reject(new Error('Request timed out')), timeout);
            });

            // Create the fetch promise
            const fetchPromise = customFetch(`${this.baseUrl}${path}`, fetchOptions);

            // Race the fetch against the timeout
            const response = await Promise.race([fetchPromise, timeoutPromise]);

            if (!response.ok) {
                let errorData;
                try {
                    errorData = await response.json();
                } catch (e) {
                    errorData = { message: response.statusText };
                }
                
                // Enhance error messages based on status codes
                let errorMessage = errorData.message || `HTTP error! Status: ${response.status}`;
                
                switch (response.status) {
                    case 400:
                        errorMessage = errorData.message || 'Bad request: The server could not understand the request';
                        break;
                    case 401:
                        errorMessage = 'Authentication required: Please log in again';
                        break;
                    case 403:
                        errorMessage = 'Access denied: You do not have permission to perform this action';
                        break;
                    case 404:
                        errorMessage = 'Not found: The requested resource does not exist';
                        break;
                    case 409:
                        errorMessage = errorData.message || 'Conflict: The request conflicts with the current state';
                        break;
                    case 429:
                        errorMessage = 'Too many requests: Please try again later';
                        break;
                    case 500:
                    case 502:
                    case 503:
                    case 504:
                        errorMessage = 'Server error: The server encountered an error. Please try again later';
                        break;
                }
                
                throw new Error(errorMessage);
            }

            if (response.status === 204) {
                return null;
            }

            try {
                return await response.json();
            } catch (e) {
                return null;
            }
        } catch (error) {
            console.error(`API request error: ${method} ${path}`, error);
            
            // Enhance network error messages
            if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
                throw new Error('Network error: Unable to connect to the server. Please check your internet connection.');
            } else if (error.message === 'Request timed out') {
                throw new Error('The request timed out. The server might be experiencing high load or connectivity issues.');
            }
            
            throw error;
        }
    }
}

// Create a singleton instance
const client = new ApiClient();

// Export simplified API
export const api = {
    // Core HTTP methods
    get: (path, customFetch) => client.request(path, { method: 'GET', customFetch }),
    post: (path, data, customFetch) => client.request(path, { method: 'POST', body: data, customFetch }),
    postForm: (path, formData, customFetch) => client.request(path, {
        method: 'POST',
        body: formData,
        customFetch,
        isFormData: true
    }),
    put: (path, data, customFetch) => client.request(path, { method: 'PUT', body: data, customFetch }),
    delete: (path, data, customFetch) => client.request(path, { method: 'DELETE', body: data, customFetch }),

    // Endpoints for direct access
    endpoints: ENDPOINTS
};

// Export legacy function for backward compatibility
export async function apiFetch(path, options = {}) {
    return client.request(path, options);
}
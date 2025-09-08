const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '/api';

// API Endpoints
export const ENDPOINTS = {
    // Auth
    LOGIN: '/auth/login',

    // Posts
    POSTS: '/posts',
    POST_BY_ID: (id: string) => `/posts/${id}`,
    POST_CREATE: '/posts/create',
    POST_UPDATE: '/posts/update',
    POST_DELETE: '/posts/delete',

    // Events
    EVENTS: '/events',
    EVENT_BY_ID: (id: string) => `/events/${id}`,
    EVENT_UPDATE: '/events/update',
    EVENT_DELETE: '/events/delete',

    // Users
    USERS: '/users',
    
    // Kids
    KIDS: '/kids',
    
    // Services
    SERVICES: '/services',
    SERVICE_BY_ID: (id: string) => `/services/${id}`,
    SERVICE_CREATE: '/services/create',
    SERVICE_UPDATE: '/services/update',
    SERVICE_DELETE: '/services/delete',
    
    // Kids Services
    KIDS_SERVICES: '/kids-services',
    KIDS_SERVICE_BY_ID: (id: string) => `/kids-services/${id}`,
    
    // Attendance
    ATTENDANCE_EVENT: (eventId: string) => `/attendance/event/${eventId}`,
    ATTENDANCE_SERVICE: (serviceId: string) => `/attendance/service/${serviceId}`,
    ATTENDANCE_KIDS_SERVICE: (kidsServiceId: string) => `/attendance/kids-service/${kidsServiceId}`,
    ATTENDANCE_EVENT_CURRENT: (eventId: string) => `/attendance/event/${eventId}/current`,
    ATTENDANCE_SERVICE_CURRENT: (serviceId: string) => `/attendance/service/${serviceId}/current`,
    ATTENDANCE_KIDS_SERVICE_CURRENT: (kidsServiceId: string) => `/attendance/kids-service/${kidsServiceId}/current`,
    ATTENDANCE_EVENT_STATS: (eventId: string) => `/attendance/event/${eventId}/stats`,
    ATTENDANCE_SERVICE_STATS: (serviceId: string) => `/attendance/service/${serviceId}/stats`,
    ATTENDANCE_KIDS_SERVICE_STATS: (kidsServiceId: string) => `/attendance/kids-service/${kidsServiceId}/stats`,
    ATTENDANCE_USER_HISTORY: (userId: string) => `/attendance/user/${userId}`,
    ATTENDANCE_KID_HISTORY: (kidId: string) => `/attendance/kid/${kidId}`,
    ATTENDANCE_EVENT_CHECK_IN: (eventId: string) => `/attendance/event/${eventId}/check-in`,
    ATTENDANCE_SERVICE_CHECK_IN: (serviceId: string) => `/attendance/service/${serviceId}/check-in`,
    ATTENDANCE_KIDS_SERVICE_CHECK_IN: (kidsServiceId: string) => `/attendance/kids-service/${kidsServiceId}/check-in`,
    ATTENDANCE_CHECK_OUT: '/attendance/check-out',
    ATTENDANCE_KID_CHECK_OUT: '/attendance/kid/check-out',
    ATTENDANCE_UPDATE_STATUS: '/attendance/status',
    ATTENDANCE_UPDATE_NOTES: '/attendance/notes',
    ATTENDANCE_EVENT_USER_STATUS: (eventId: string, userId: string) => `/attendance/event/${eventId}/user/${userId}/status`,
    ATTENDANCE_SERVICE_USER_STATUS: (serviceId: string, userId: string) => `/attendance/service/${serviceId}/user/${userId}/status`,
    ATTENDANCE_KIDS_SERVICE_KID_STATUS: (kidsServiceId: string, kidId: string) => `/attendance/kids-service/${kidsServiceId}/kid/${kidId}/status`,
    ATTENDANCE_RECENT: '/attendance/recent',
    ATTENDANCE_FREQUENT: '/attendance/frequent'
};

interface RequestOptions {
    method?: string;
    body?: unknown;
    isFormData?: boolean;
    timeout?: number;
}

class ApiClient {
    private baseUrl: string;

    constructor() {
        this.baseUrl = BASE_URL;
    }

    private getAuthToken(): string | null {
        if (typeof window !== 'undefined') {
            return localStorage.getItem('authToken');
        }
        return null;
    }

    async request<T>(path: string, options: RequestOptions = {}): Promise<T | null> {
        const {
            method = 'GET',
            body = null,
            isFormData = false,
            timeout = 15000 // Default timeout of 15 seconds
        } = options;

        const token = this.getAuthToken();
        const headers: HeadersInit = {};

        if (!isFormData) {
            headers['Content-Type'] = 'application/json';
        }

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const fetchOptions: RequestInit = {
            method,
            headers,
        };

        if (body) {
            fetchOptions.body = isFormData ? body as BodyInit : JSON.stringify(body);
        }

        try {
            const controller = new AbortController();
            const id = setTimeout(() => controller.abort(), timeout);

            const response = await fetch(`${this.baseUrl}${path}`, { ...fetchOptions, signal: controller.signal });
            clearTimeout(id);

            if (!response.ok) {
                let errorData: { message?: string };
                try {
                    errorData = await response.json();
                } catch {
                    errorData = { message: response.statusText };
                }
                
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
            } catch {
                return null;
            }
        } catch (error: unknown) {
            console.error(`API request error: ${method} ${path}`, error);
            
            if (error instanceof Error) {
                if (error.name === 'AbortError') {
                    throw new Error('The request timed out. The server might be experiencing high load or connectivity issues.');
                } else if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
                    throw new Error('Network error: Unable to connect to the server. Please check your internet connection.');
                }
            }
            
            throw error;
        }
    }
}

const client = new ApiClient();

export const api = {
    get: <T>(path: string) => client.request<T>(path, { method: 'GET' }),
    post: <T>(path: string, data: unknown) => client.request<T>(path, { method: 'POST', body: data }),
    postForm: <T>(path: string, formData: FormData) => client.request<T>(path, {
        method: 'POST',
        body: formData,
        isFormData: true
    }),
    put: <T>(path: string, data: unknown) => client.request<T>(path, { method: 'PUT', body: data }),
    delete: <T>(path: string, data?: unknown) => client.request<T>(path, { method: 'DELETE', body: data }),

    // Attendance API functions
    attendance: {
        getByEventType: async (eventType: string) => {
            const endpoint = eventType === 'EVENT' ? '/attendance/events' : 
                           eventType === 'SERVICE' ? '/attendance/services' : 
                           '/attendance/kids-services';
            return client.request(endpoint, { method: 'GET' });
        },
        updateStatus: async (id: string, status: string) => {
            return client.request('/attendance/status', { 
                method: 'PUT', 
                body: { id, status } 
            });
        },
        updateNotes: async (id: string, notes: string) => {
            return client.request('/attendance/notes', { 
                method: 'PUT', 
                body: { id, notes } 
            });
        },
        bulkUpdateStatus: async (ids: string[], status: string) => {
            return client.request('/attendance/bulk-status', { 
                method: 'PUT', 
                body: { ids, status } 
            });
        },
        getStats: async () => {
            return client.request('/attendance/stats', { method: 'GET' });
        },
        getRecent: async (limit?: number) => {
            const query = limit ? `?limit=${limit}` : '';
            return client.request(`/attendance/recent${query}`, { method: 'GET' });
        },
        getByDateRange: async (startDate: string, endDate: string, eventType?: string) => {
            const params = new URLSearchParams({
                startDate,
                endDate,
                ...(eventType && { eventType })
            });
            return client.request(`/attendance/range?${params}`, { method: 'GET' });
        },
        exportData: async (filters: Record<string, string>) => {
            const params = new URLSearchParams(filters);
            return client.request(`/attendance/export?${params}`, { method: 'GET' });
        }
    }
};

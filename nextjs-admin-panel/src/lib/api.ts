const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '/api';

// API Endpoints - Aligned with Spring Boot backend
export const ENDPOINTS = {
    // Auth - /api/auth/*
    LOGIN: '/auth/login',
    SIGNUP: '/auth/signup',
    VERIFY: '/auth/verify',
    FORGOT_PASSWORD: '/auth/forgot-password',
    RESET_PASSWORD: '/auth/reset-password',
    GOOGLE_LOGIN: '/auth/google-login',
    FACEBOOK_LOGIN: '/auth/facebook-login',
    REFRESH: '/auth/refresh',
    LOGOUT: '/auth/logout',

    // Posts - /api/posts/*
    POSTS: '/posts',
    POST_BY_ID: (id: string) => `/posts/${id}`,
    POST_CREATE: '/posts',
    POST_UPDATE: (id: string) => `/posts/${id}`,
    POST_DELETE: (id: string) => `/posts/${id}`,
    POST_LIKE: (id: string) => `/posts/${id}/like`,
    POST_BY_AUTHOR: (authorId: string) => `/posts/author/${authorId}`,
    POST_SEARCH: '/posts/search',
    POST_COMMENTS: (id: string) => `/posts/${id}/comments`,
    POST_COMMENT_DELETE: (commentId: string) => `/posts/comments/${commentId}`,
    POST_STATS: '/posts/stats',

    // Events - /api/events/*
    EVENTS: '/events',
    EVENT_BY_ID: (id: string) => `/events/${id}`,
    EVENT_CREATE: '/events',
    EVENT_UPDATE: (id: string) => `/events/${id}`,
    EVENT_DELETE: (id: string) => `/events/${id}`,
    EVENT_UPCOMING: '/events/upcoming',
    EVENT_JOIN: (id: string) => `/events/${id}/join`,
    EVENT_LEAVE: (id: string) => `/events/${id}/leave`,
    EVENT_APPROVE: (eventId: string, userId: string) => `/events/${eventId}/approve/${userId}`,
    EVENT_STATUS: (id: string) => `/events/${id}/status`,
    EVENT_MY_EVENTS: '/events/my-events',
    EVENT_ATTENDING: '/events/attending',
    EVENT_WAITING_LIST: '/events/waiting-list',
    EVENT_SEARCH: '/events/search',

    // Profile/Users - /api/profile/*
    PROFILE: '/profile',
    PROFILE_BY_ID: (userId: string) => `/profile/${userId}`,
    PROFILE_UPDATE: '/profile',
    PROFILE_IMAGE: '/profile/image',
    PROFILE_ALL: '/profile/all',
    PROFILE_SEARCH: '/profile/search',
    PROFILE_ADMINS: '/profile/admins',
    PROFILE_ADMIN_STATUS: (userId: string) => `/profile/${userId}/admin-status`,
    PROFILE_DELETE: (userId: string) => `/profile/${userId}`,

    // Attendance - /api/attendance/*
    ATTENDANCE_CHECK_IN: '/attendance/check-in',
    ATTENDANCE_CHECK_OUT: '/attendance/check-out',
    ATTENDANCE_MY: '/attendance/my-attendance',
    ATTENDANCE_USER: (userId: string) => `/attendance/user/${userId}`,
    ATTENDANCE_EVENT: (eventId: string) => `/attendance/event/${eventId}`,
    ATTENDANCE_SERVICE: (serviceId: string) => `/attendance/service/${serviceId}`,
    ATTENDANCE_KIDS_SERVICE: (kidsServiceId: string) => `/attendance/kids-service/${kidsServiceId}`,
    ATTENDANCE_CURRENT: '/attendance/currently-checked-in',
    ATTENDANCE_STATS: '/attendance/stats',
    ATTENDANCE_FREQUENT: '/attendance/frequent-attendees',
    ATTENDANCE_BULK_CHECK_IN: '/attendance/bulk-check-in',
    ATTENDANCE_UPDATE_STATUS: (attendanceId: string) => `/attendance/${attendanceId}/status`,
    ATTENDANCE_BY_TYPE: (type: string) => `/attendance/by-type/${type}`,
    ATTENDANCE_BY_STATUS: (status: string) => `/attendance/by-status/${status}`,

    // Files - /api/files/*
    FILES: (subDirectory: string, fileName: string) => `/files/${subDirectory}/${fileName}`,
    FILE_ROOT: (fileName: string) => `/files/${fileName}`,
    FILE_INFO: (subDirectory: string, fileName: string) => `/files/${subDirectory}/${fileName}/info`,

    // Health - /api/health
    HEALTH: '/health'
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
            const token = localStorage.getItem('authToken');
            console.log('🔑 Retrieved token from localStorage:', token ? 'Token exists' : 'No token found');
            return token;
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
            console.log('🔐 Adding Authorization header with Bearer token');
        } else {
            console.log('⚠️ No token available, making unauthenticated request');
        }

        const fetchOptions: RequestInit = {
            method,
            headers,
        };

        if (body) {
            fetchOptions.body = isFormData ? body as BodyInit : JSON.stringify(body);
        }

        try {
            console.log(`🚀 API Request: ${method} ${this.baseUrl}${path}`, body ? { body } : '');

            const controller = new AbortController();
            const id = setTimeout(() => controller.abort(), timeout);

            const response = await fetch(`${this.baseUrl}${path}`, { ...fetchOptions, signal: controller.signal });
            clearTimeout(id);

            console.log(`📡 API Response: ${method} ${path} - Status: ${response.status}`);

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
                        // Only redirect if we're not already on the login page
                        if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
                            console.log('🚪 Redirecting to login due to 401 error');
                            localStorage.removeItem('authToken');
                            window.location.href = '/login';
                        }
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
            console.error(`🔴 API request error: ${method} ${path}`, error);

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
    putForm: <T>(path: string, formData: FormData) => client.request<T>(path, {
        method: 'PUT',
        body: formData,
        isFormData: true
    }),
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

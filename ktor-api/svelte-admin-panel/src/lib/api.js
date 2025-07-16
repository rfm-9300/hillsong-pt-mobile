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
    USERS: '/users'
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
            isFormData = false
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
            const response = await customFetch(`${this.baseUrl}${path}`, fetchOptions);

            if (!response.ok) {
                let errorData;
                try {
                    errorData = await response.json();
                } catch (e) {
                    errorData = { message: response.statusText };
                }
                throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
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
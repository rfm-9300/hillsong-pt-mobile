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

    // GET requests
    async get(path, customFetch) {
        return this.request(path, { method: 'GET', customFetch });
    }

    // POST requests
    async post(path, body, customFetch) {
        return this.request(path, { method: 'POST', body, customFetch });
    }

    // POST requests with FormData
    async postFormData(path, formData, customFetch) {
        return this.request(path, {
            method: 'POST',
            body: formData,
            customFetch,
            isFormData: true
        });
    }

    // PUT requests
    async put(path, body, customFetch) {
        return this.request(path, { method: 'PUT', body, customFetch });
    }

    // DELETE requests
    async delete(path, body, customFetch) {
        return this.request(path, { method: 'DELETE', body, customFetch });
    }

    // Auth methods
    async login(email, password, customFetch) {
        return this.post(ENDPOINTS.LOGIN, { email, password }, customFetch);
    }

    // Post methods
    async getPosts(customFetch) {
        return this.get(ENDPOINTS.POSTS, customFetch);
    }

    async getPost(id, customFetch) {
        return this.get(ENDPOINTS.POST_BY_ID(id), customFetch);
    }

    async createPost(formData, customFetch) {
        return this.postFormData(ENDPOINTS.POST_CREATE, formData, customFetch);
    }

    async updatePost(formData, customFetch) {
        return this.postFormData(ENDPOINTS.POST_UPDATE, formData, customFetch);
    }

    async deletePost(postId, customFetch) {
        return this.post(ENDPOINTS.POST_DELETE, { postId }, customFetch);
    }

    // Event methods
    async getEvents(customFetch) {
        return this.get(ENDPOINTS.EVENTS, customFetch);
    }

    async getEvent(id, customFetch) {
        return this.get(ENDPOINTS.EVENT_BY_ID(id), customFetch);
    }

    async createEvent(formData, customFetch) {
        return this.postFormData(ENDPOINTS.EVENTS, formData, customFetch);
    }

    async updateEvent(formData, customFetch) {
        return this.postFormData(ENDPOINTS.EVENT_UPDATE, formData, customFetch);
    }

    async deleteEvent(eventId, customFetch) {
        return this.post(ENDPOINTS.EVENT_DELETE, { eventId }, customFetch);
    }

    // User methods
    async getUsers(customFetch) {
        return this.get(ENDPOINTS.USERS, customFetch);
    }
}

// Export singleton instance
export const api = new ApiClient();

// Export legacy function for backward compatibility
export async function apiFetch(path, options = {}) {
    return api.request(path, options);
}

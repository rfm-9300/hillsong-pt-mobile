const BASE_URL = '/api';

function getAuthToken() {
    if (typeof window !== 'undefined') {
        return localStorage.getItem('authToken');
    }
    return null;
}

export async function apiFetch(path, options = {}) {
    const { 
        method = 'GET', 
        body = null, 
        customFetch = typeof fetch !== 'undefined' ? fetch : null 
    } = options;
    
    if (!customFetch) {
        throw new Error('No fetch implementation available');
    }

    const token = getAuthToken();
    const headers = {
        'Content-Type': 'application/json',
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const fetchOptions = {
        method,
        headers,
    };

    if (body) {
        fetchOptions.body = JSON.stringify(body);
    }

    try {
        const response = await customFetch(`${BASE_URL}${path}`, fetchOptions);

        if (!response.ok) {
            let errorData;
            try {
                errorData = await response.json();
            } catch (e) {
                errorData = { message: response.statusText };
            }
            throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
        }

        // Don't rely on content-length header which might not be accessible
        if (response.status === 204) {
            return null;
        }

        // Try to parse JSON, return null if response is empty
        try {
            return await response.json();
        } catch (e) {
            // If JSON parsing fails (likely empty response)
            return null;
        }
    } catch (error) {
        console.error(`API fetch error: ${method} ${path}`, error);
        throw error;
    }
}

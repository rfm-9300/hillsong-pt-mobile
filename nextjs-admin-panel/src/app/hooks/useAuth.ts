import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { api, ENDPOINTS } from '@/lib/api';
import { User, ApiResponse } from '@/lib/types';

interface UseAuthReturn {
    user: User | null;
    loading: boolean;
    logout: () => void;
}

export function useAuth(): UseAuthReturn {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    useEffect(() => {
        const fetchCurrentUser = async () => {
            try {
                const response = await api.get<ApiResponse<User>>(ENDPOINTS.PROFILE);
                if (response && response.success && response.data) {
                    setUser(response.data);
                }
            } catch (error) {
                console.error('Failed to fetch current user:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchCurrentUser();
    }, []);

    const logout = () => {
        if (typeof window !== 'undefined') {
            localStorage.removeItem('authToken');
            router.push('/login');
        }
    };

    return { user, loading, logout };
}

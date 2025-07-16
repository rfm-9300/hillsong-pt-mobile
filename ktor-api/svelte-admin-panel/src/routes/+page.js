import { redirect } from '@sveltejs/kit';
import { browser } from '$app/environment';

export function load() {
    if (browser) {
        const token = localStorage.getItem('authToken');
        if (token) {
            throw redirect(307, '/admin/dashboard');
        } else {
            throw redirect(307, '/login');
        }
    }
}
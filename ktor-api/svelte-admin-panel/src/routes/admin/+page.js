import { redirect } from '@sveltejs/kit';
import { browser } from '$app/environment';

/** @type {import('./$types').PageLoad} */
export async function load() {
    if (browser) {
        const token = localStorage.getItem('authToken');
        if (!token) {
            throw redirect(307, '/login');
        }
    }
}
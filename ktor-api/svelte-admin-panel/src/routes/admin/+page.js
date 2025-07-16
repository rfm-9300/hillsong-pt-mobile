import { redirect } from '@sveltejs/kit';

/** @type {import('./$types').PageLoad} */
export function load() {
    // Redirect from /admin to /admin/dashboard
    throw redirect(307, '/admin/dashboard');
}
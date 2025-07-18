import { redirect } from '@sveltejs/kit';

export function load({ locals }) {
    // Check if user is authenticated
    if (!locals.user) {
        throw redirect(302, '/login');
    }
    
    return {
        title: 'Attendance Management'
    };
}
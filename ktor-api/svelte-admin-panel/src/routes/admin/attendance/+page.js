import { redirect } from '@sveltejs/kit';
import { browser } from '$app/environment';

export function load({ depends }) {
    // Add dependency on auth
    depends('auth');
    
    // We'll handle authentication in the +page.svelte component
    // This allows the page to load and then redirect if needed
    
    return {
        title: 'Attendance Management'
    };
}
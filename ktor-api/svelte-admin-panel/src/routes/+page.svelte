<script>
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';

    onMount(() => {
        const token = localStorage.getItem('authToken');
        if (token) {
            goto('/admin');
        } else {
            goto('/login');
        }
    });
</script>
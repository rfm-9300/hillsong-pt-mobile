<script>
    import { fade } from 'svelte/transition';
    import StatCard from './StatCard.svelte';
    import QuickActions from './QuickActions.svelte';
    
    export let data; // SvelteKit provides the data from +page.js
    let stats = data.stats; // Access the stats from the load function
</script>

<div in:fade={{ duration: 300 }}>
    <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-800">Dashboard</h1>
        <p class="mt-2 text-gray-600">Welcome to your admin dashboard.</p>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <StatCard title="Total Posts" value={stats.posts.count} loading={stats.posts.loading} link="/admin/posts" color="blue" delay={100}>
            <svg slot="icon" xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
            </svg>
        </StatCard>
        
        <StatCard title="Total Events" value={stats.events.count} loading={stats.events.loading} link="/admin/events" color="purple" delay={200}>
            <svg slot="icon" xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
        </StatCard>
        
        <StatCard title="Total Users" value={stats.users.count} loading={stats.users.loading} link="/admin/users" color="green" delay={300}>
            <svg slot="icon" xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
        </StatCard>
    </div>
    
    <QuickActions />
</div>
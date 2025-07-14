<script>
    import { onMount } from 'svelte';
    import { fade, fly } from 'svelte/transition';
    
    let stats = {
        posts: { count: 0, loading: true },
        events: { count: 0, loading: true },
        users: { count: 0, loading: true }
    };
    
    onMount(async () => {
        try {
            const token = localStorage.getItem('authToken');
            
            // Fetch posts count
            const postsRes = await fetch('/api/posts', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (postsRes.ok) {
                const postsData = await postsRes.json();
                stats.posts.count = postsData.data?.postList?.length || 0;
            }
            stats.posts.loading = false;
            
            // Fetch events count
            const eventsRes = await fetch('/api/events', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (eventsRes.ok) {
                const eventsData = await eventsRes.json();
                stats.events.count = eventsData.data?.length || 0;
            }
            stats.events.loading = false;
            
            // Fetch users count
            const usersRes = await fetch('/api/users', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (usersRes.ok) {
                const usersData = await usersRes.json();
                stats.users.count = usersData.data?.users?.length || 0;
            }
            stats.users.loading = false;
            
        } catch (error) {
            console.error('Error fetching data:', error);
            stats.posts.loading = false;
            stats.events.loading = false;
            stats.users.loading = false;
        }
    });
</script>

<div in:fade={{ duration: 300 }}>
    <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-800">Dashboard</h1>
        <p class="mt-2 text-gray-600">Welcome to your admin dashboard.</p>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <!-- Posts Card -->
        <div 
            in:fly={{ y: 20, duration: 300, delay: 100 }}
            class="bg-white rounded-xl shadow-md p-6 border border-gray-100 hover:shadow-lg transition-shadow"
        >
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500 mb-1">Total Posts</p>
                    {#if stats.posts.loading}
                        <div class="h-8 w-16 bg-gray-200 animate-pulse rounded"></div>
                    {:else}
                        <h2 class="text-3xl font-bold text-gray-800">{stats.posts.count}</h2>
                    {/if}
                </div>
                <div class="p-3 bg-blue-50 rounded-lg">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
                    </svg>
                </div>
            </div>
            
            <div class="mt-4">
                <a href="/admin/posts" class="text-blue-600 hover:text-blue-800 flex items-center text-sm font-medium">
                    View all posts
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </a>
            </div>
        </div>
        
        <!-- Events Card -->
        <div 
            in:fly={{ y: 20, duration: 300, delay: 200 }}
            class="bg-white rounded-xl shadow-md p-6 border border-gray-100 hover:shadow-lg transition-shadow"
        >
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500 mb-1">Total Events</p>
                    {#if stats.events.loading}
                        <div class="h-8 w-16 bg-gray-200 animate-pulse rounded"></div>
                    {:else}
                        <h2 class="text-3xl font-bold text-gray-800">{stats.events.count}</h2>
                    {/if}
                </div>
                <div class="p-3 bg-purple-50 rounded-lg">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                </div>
            </div>
            
            <div class="mt-4">
                <a href="/admin/events" class="text-purple-600 hover:text-purple-800 flex items-center text-sm font-medium">
                    View all events
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </a>
            </div>
        </div>
        
        <!-- Users Card -->
        <div 
            in:fly={{ y: 20, duration: 300, delay: 300 }}
            class="bg-white rounded-xl shadow-md p-6 border border-gray-100 hover:shadow-lg transition-shadow"
        >
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500 mb-1">Total Users</p>
                    {#if stats.users.loading}
                        <div class="h-8 w-16 bg-gray-200 animate-pulse rounded"></div>
                    {:else}
                        <h2 class="text-3xl font-bold text-gray-800">{stats.users.count}</h2>
                    {/if}
                </div>
                <div class="p-3 bg-green-50 rounded-lg">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                    </svg>
                </div>
            </div>
            
            <div class="mt-4">
                <a href="/admin/users" class="text-green-600 hover:text-green-800 flex items-center text-sm font-medium">
                    View all users
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </a>
            </div>
        </div>
    </div>
    
    <div class="bg-white rounded-xl shadow-md p-6 border border-gray-100">
        <h2 class="text-xl font-bold text-gray-800 mb-4">Quick Actions</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <a href="/admin/posts/create" class="flex items-center p-4 bg-blue-50 hover:bg-blue-100 text-blue-700 rounded-lg transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
                Create New Post
            </a>
            
            <a href="/admin/events/create" class="flex items-center p-4 bg-purple-50 hover:bg-purple-100 text-purple-700 rounded-lg transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
                Create New Event
            </a>
            
            <a href="/admin/users" class="flex items-center p-4 bg-green-50 hover:bg-green-100 text-green-700 rounded-lg transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
                Add New User
            </a>
        </div>
    </div>
</div>
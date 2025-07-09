<script>
    import { fade, fly } from 'svelte/transition';
    export let data;
    
    let users = data.users;
    let searchTerm = '';
    let roleFilter = 'all';
    let verificationFilter = 'all';
    
    // Function to generate initials from name with null checks
    function getInitials(user) {
        if (!user.profile) {
            return 'U'; // Default for unknown user
        }
        
        const firstName = user.profile.firstName || '';
        const lastName = user.profile.lastName || '';
        
        if (!firstName && !lastName) return 'U';
        
        return firstName.charAt(0) + (lastName ? lastName.charAt(0) : '');
    }
    
    // Function to generate random pastel color based on name or email
    function getAvatarColor(user) {
        // Use email as fallback if profile is null or name is empty
        const nameToUse = user.profile ? 
            `${user.profile.firstName || ''} ${user.profile.lastName || ''}`.trim() : 
            user.email;
            
        // Generate a simple hash from the name
        let hash = 0;
        for (let i = 0; i < nameToUse.length; i++) {
            hash = nameToUse.charCodeAt(i) + ((hash << 5) - hash);
        }
        
        // Generate pastel color
        const hue = hash % 360;
        return `hsl(${hue}, 70%, 80%)`;
    }
    
    // Get display name with fallbacks
    function getDisplayName(user) {
        if (!user.profile) return "Unknown User";
        
        const firstName = user.profile.firstName || '';
        const lastName = user.profile.lastName || '';
        
        if (!firstName && !lastName) return user.email.split('@')[0] || "Unknown User";
        
        return `${firstName} ${lastName}`.trim();
    }
    
    // Check if user is admin
    function isAdmin(user) {
        return user.profile && user.profile.isAdmin;
    }
    
    // Filtered users based on search and filters with null checks
    $: filteredUsers = users.filter(user => {
        // Handle email search
        const email = (user.email || '').toLowerCase();
        
        // Handle name search with null check
        let fullName = '';
        if (user.profile) {
            const firstName = user.profile.firstName || '';
            const lastName = user.profile.lastName || '';
            fullName = `${firstName} ${lastName}`.toLowerCase();
        }
        
        const searchMatch = searchTerm.length === 0 || 
            fullName.includes(searchTerm.toLowerCase()) || 
            email.includes(searchTerm.toLowerCase());
            
        // Handle role filter with null check
        const roleMatch = roleFilter === 'all' || 
            (roleFilter === 'admin' && isAdmin(user)) || 
            (roleFilter === 'user' && !isAdmin(user));
            
        const verificationMatch = verificationFilter === 'all' || 
            (verificationFilter === 'verified' && user.verified) || 
            (verificationFilter === 'unverified' && !user.verified);
            
        return searchMatch && roleMatch && verificationMatch;
    });
</script>

<div class="p-8">
    <div class="flex items-center justify-between mb-8">
        <h1 class="text-3xl font-bold text-gray-800">Manage Users</h1>
        <button class="px-6 py-3 font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 shadow-md transition-all duration-200 flex items-center gap-2 cursor-pointer">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
            </svg>
            Add User
        </button>
    </div>
    
    <!-- Filters and Search -->
    <div class="bg-white rounded-xl shadow-md p-4 mb-6">
        <div class="flex flex-col md:flex-row md:items-center gap-4">
            <div class="flex-1">
                <div class="relative">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                        </svg>
                    </div>
                    <input
                        type="text"
                        placeholder="Search users by name or email..."
                        bind:value={searchTerm}
                        class="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
                    />
                </div>
            </div>
            
            <div class="flex gap-3">
                <select 
                    bind:value={roleFilter}
                    class="px-4 py-3 border border-gray-300 rounded-lg bg-white focus:ring-blue-500 focus:border-blue-500 cursor-pointer"
                >
                    <option value="all">All Roles</option>
                    <option value="admin">Admin</option>
                    <option value="user">User</option>
                </select>
                
                <select 
                    bind:value={verificationFilter}
                    class="px-4 py-3 border border-gray-300 rounded-lg bg-white focus:ring-blue-500 focus:border-blue-500 cursor-pointer"
                >
                    <option value="all">All Status</option>
                    <option value="verified">Verified</option>
                    <option value="unverified">Unverified</option>
                </select>
            </div>
        </div>
    </div>
    
    <!-- User count -->
    <div class="mb-4 text-gray-600">
        Showing {filteredUsers.length} of {users.length} users
    </div>
    
    <!-- User Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {#each filteredUsers as user, i (user.email)}
            <div 
                in:fly={{ y: 20, delay: i * 75, duration: 300 }} 
                class="bg-white rounded-xl overflow-hidden shadow-md hover:shadow-lg transition-all duration-200 border border-gray-100"
            >
                <div class="p-6">
                    <div class="flex items-center mb-4">
                        <div 
                            class="w-12 h-12 rounded-full flex items-center justify-center text-white font-medium"
                            style="background-color: {getAvatarColor(user)}"
                        >
                            {getInitials(user)}
                        </div>
                        <div class="ml-4">
                            <h3 class="text-lg font-semibold text-gray-800">{getDisplayName(user)}</h3>
                            <p class="text-sm text-gray-600">{user.email}</p>
                        </div>
                    </div>
                    
                    <div class="flex flex-wrap gap-2 mb-4">
                        <span class={`text-xs px-2 py-1 rounded-full ${isAdmin(user) ? 'bg-purple-100 text-purple-800' : 'bg-blue-100 text-blue-800'}`}>
                            {isAdmin(user) ? 'Admin' : 'User'}
                        </span>
                        
                        <span class={`text-xs px-2 py-1 rounded-full ${user.verified ? 'bg-green-100 text-green-800' : 'bg-amber-100 text-amber-800'}`}>
                            {user.verified ? 'Verified' : 'Unverified'}
                        </span>
                    </div>
                    
                    <div class="pt-4 border-t border-gray-100 flex justify-end">
                        <button class="text-gray-500 hover:text-blue-600 transition-colors cursor-pointer p-2">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                            </svg>
                        </button>
                        <button class="text-gray-500 hover:text-red-600 transition-colors cursor-pointer p-2 ml-2">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        {:else}
            <div class="col-span-full bg-gray-50 rounded-lg p-12 text-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                <h3 class="text-xl font-semibold text-gray-700 mb-2">No Users Found</h3>
                <p class="text-gray-500">Try adjusting your search or filter criteria</p>
            </div>
        {/each}
    </div>
</div>
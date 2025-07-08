<script>
    import { onMount } from 'svelte';
    import { fade, fly } from 'svelte/transition';

    let posts = [];
    let loading = true;
    let deleteConfirmation = { show: false, postId: null };

    onMount(async () => {
        try {
            const response = await fetch('/api/posts');
            if (response.ok) {
                const data = await response.json();
                posts = data.data.postList;
            }
        } catch (error) {
            console.error("Failed to load posts:", error);
        } finally {
            loading = false;
        }
    });

    async function deletePost(postId) {
        try {
            const token = localStorage.getItem('authToken');
            const response = await fetch('/api/posts/delete', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ postId })
            });

            if (response.ok) {
                posts = posts.filter(post => post.id !== postId);
            }
        } finally {
            deleteConfirmation = { show: false, postId: null };
        }
    }
    
    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
    
    function confirmDelete(postId) {
        deleteConfirmation = { show: true, postId };
    }
    
    // Function to build the complete image URL from the filename
    function getImageUrl(imagePath) {
        if (!imagePath) return null;
        return `http://localhost:8080/resources/uploads/images/${imagePath}`;
    }

    // Function to navigate to edit page
    function navigateToEdit(postId) {
        window.location.href = `/admin/posts/${postId}`;
    }
</script>

<div class="p-8">
    <div class="flex items-center justify-between mb-8">
        <h1 class="text-3xl font-bold text-gray-800">Manage Posts</h1>
        <a href="/admin/posts/create" 
           class="px-6 py-3 font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 shadow-md transition-all duration-200 flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
            </svg>
            Create Post
        </a>
    </div>

    {#if loading}
        <div class="flex justify-center items-center h-64">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-700"></div>
        </div>
    {:else if posts.length === 0}
        <div in:fade class="bg-gray-50 rounded-lg p-12 text-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
            <h3 class="text-xl font-semibold text-gray-700 mb-2">No Posts Yet</h3>
            <p class="text-gray-500">Create your first post to get started</p>
        </div>
    {:else}
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {#each posts as post, i (post.id)}
                <div 
                    in:fly={{ y: 20, delay: i * 75, duration: 300 }} 
                    class="relative bg-white rounded-xl overflow-hidden shadow-lg transition-all duration-200 hover:shadow-xl border border-gray-100 h-64 cursor-pointer hover:scale-[1.02]"
                    on:click={() => navigateToEdit(post.id)}
                    on:keydown={(e) => e.key === 'Enter' && navigateToEdit(post.id)}
                    tabindex="0"
                    role="button"
                    aria-label="Edit post: {post.title}"
                >
                    <!-- Background image with gradient overlay -->
                    <div class="absolute inset-0">
                        {#if post.headerImagePath}
                            <img 
                                src={getImageUrl(post.headerImagePath)} 
                                alt={post.title} 
                                class="w-full h-full object-cover"
                                on:error={(e) => e.target.src = '/images/placeholder.png'}
                            />
                        {:else}
                            <div class="w-full h-full bg-gradient-to-r from-blue-100 to-indigo-100 flex items-center justify-center">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                            </div>
                        {/if}
                        <!-- Gradient overlay for better text readability -->
                        <div class="absolute inset-0 bg-gradient-to-t from-black/80 to-black/20"></div>
                    </div>
                    
                    <!-- Post content on top of the background -->
                    <div class="relative h-full flex flex-col justify-between p-5 text-white z-10">
                        <div>
                            <h3 class="text-xl font-bold mb-2 text-white line-clamp-2">{post.title}</h3>
                            <p class="text-white/80 mb-4 line-clamp-2">{post.content}</p>
                        </div>
                        
                        <div class="mt-auto">
                            <div class="flex justify-between text-sm text-white/70 mb-4">
                                <div class="flex items-center">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                    </svg>
                                    {formatDate(post.date)}
                                </div>
                                <div class="flex items-center">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                                    </svg>
                                    {post.likes}
                                </div>
                            </div>
                            
                            <div class="flex justify-between pt-3 border-t border-white/20">
                                <button 
                                    class="text-white hover:text-blue-300 flex items-center transition-colors cursor-pointer"
                                    on:click|stopPropagation={() => navigateToEdit(post.id)}
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                    </svg>
                                    Edit
                                </button>
                                <button 
                                    on:click|stopPropagation={() => confirmDelete(post.id)} 
                                    class="text-white hover:text-red-300 flex items-center transition-colors cursor-pointer hover:scale-105"
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                    </svg>
                                    Delete
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            {/each}
        </div>
    {/if}
</div>

{#if deleteConfirmation.show}
    <div class="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50" transition:fade>
        <div class="bg-white rounded-lg p-6 max-w-sm mx-4 shadow-xl" in:fly={{ y: 20, duration: 200 }}>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Confirm Deletion</h3>
            <p class="text-gray-700 mb-6">Are you sure you want to delete this post? This action cannot be undone.</p>
            
            <div class="flex justify-end gap-3">
                <button 
                    on:click={() => deleteConfirmation = { show: false, postId: null }}
                    class="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors cursor-pointer"
                >
                    Cancel
                </button>
                <button 
                    on:click={() => deletePost(deleteConfirmation.postId)}
                    class="px-4 py-2 text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors cursor-pointer"
                >
                    Delete
                </button>
            </div>
        </div>
    </div>
{/if}

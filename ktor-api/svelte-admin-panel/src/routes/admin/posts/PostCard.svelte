<script>
    let { post, onEdit, onDelete } = $props();

    function getImageUrl(imagePath) {
        if (!imagePath) return null;
        return `http://localhost:8080/resources/uploads/images/${imagePath}`;
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
</script>

<div 
    class="relative bg-white rounded-xl overflow-hidden shadow-lg transition-all duration-200 hover:shadow-xl border border-gray-100 h-64 cursor-pointer hover:scale-[1.02]"
    onclick={onEdit}
    onkeydown={(e) => e.key === 'Enter' && onEdit()}
    tabindex="0"
    role="button"
    aria-label={`Edit post: ${post.title}`}
>
    <!-- Background image with gradient overlay -->
    <div class="absolute inset-0">
        {#if post.headerImagePath}
            <img 
                src={getImageUrl(post.headerImagePath)} 
                alt={post.title} 
                class="w-full h-full object-cover"
                onerror={(e) => e.target.src = 'http://localhost:8080/resources/uploads/images/eea87a90-ca30-4129-b3a9-c7265a1b4960.jpg'}
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
                    onclick={(e) => { e.stopPropagation(); onEdit(); }}
                >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                    Edit
                </button>
                <button 
                    onclick={(e) => { e.stopPropagation(); onDelete(); }}
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
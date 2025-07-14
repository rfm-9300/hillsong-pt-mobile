<script>
    import { fly, fade } from 'svelte/transition';
    import PostCard from './PostCard.svelte';

    let { posts, onEdit, onDelete } = $props();
</script>

{#if posts.length === 0}
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
            <div in:fly={{ y: 20, delay: i * 75, duration: 300 }}>
                <PostCard 
                    post={post} 
                    onEdit={() => onEdit(post.id)} 
                    onDelete={() => onDelete(post.id)} 
                />
            </div>
        {/each}
    </div>
{/if}
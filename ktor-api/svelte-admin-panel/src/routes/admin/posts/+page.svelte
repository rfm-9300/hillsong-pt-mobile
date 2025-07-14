<script>
    import { apiFetch } from '$lib/api';
    import PostsList from './PostsList.svelte';
    import DeleteConfirmation from './DeleteConfirmation.svelte';

    export let data;
    let posts = data.posts;
    let deleteConfirmation = { show: false, postId: null };

    async function deletePost(postId) {
        try {
            await apiFetch('/posts/delete', {
                method: 'POST',
                body: { postId },
            });
            posts = posts.filter(post => post.id !== postId);
        } catch (error) {
            console.error("Failed to delete post:", error);
            // Optionally, show an error message to the user
        } finally {
            deleteConfirmation = { show: false, postId: null };
        }
    }
    
    function confirmDelete(postId) {
        deleteConfirmation = { show: true, postId };
    }

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

    <PostsList 
        posts={posts} 
        onEdit={navigateToEdit} 
        onDelete={confirmDelete} 
    />
</div>

{#if deleteConfirmation.show}
    <DeleteConfirmation 
        onConfirm={() => deletePost(deleteConfirmation.postId)} 
        onCancel={() => deleteConfirmation = { show: false, postId: null }} 
    />
{/if}
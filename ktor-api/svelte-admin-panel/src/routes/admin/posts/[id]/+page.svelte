<script>
    import { onMount } from 'svelte';
    import { page } from '$app/stores';
    import { goto } from '$app/navigation';

    let post = null;
    let title = '';
    let content = '';

    onMount(async () => {
        const postId = $page.params.id;
        const response = await fetch(`/api/posts/${postId}`);
        if (response.ok) {
            const data = await response.json();
            post = data.data;
            title = post.title;
            content = post.content;
        }
    });

    async function updatePost() {
        const token = localStorage.getItem('authToken');
        const postId = $page.params.id;
        const response = await fetch('/api/posts/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ postId, title, content })
        });

        if (response.ok) {
            goto('/admin/posts');
        }
    }
</script>

<div class="p-8">
    <h1 class="text-2xl font-bold">Edit Post</h1>

    {#if post}
        <form on:submit|preventDefault={updatePost} class="mt-8 space-y-6">
            <div>
                <label for="title" class="text-sm font-medium">Title</label>
                <input type="text" id="title" bind:value={title} class="w-full px-3 py-2 mt-1 border rounded-md" required>
            </div>
            <div>
                <label for="content" class="text-sm font-medium">Content</label>
                <textarea id="content" bind:value={content} class="w-full px-3 py-2 mt-1 border rounded-md" required></textarea>
            </div>
            <button type="submit" class="w-full px-4 py-2 mt-4 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
                Update Post
            </button>
        </form>
    {/if}
</div>

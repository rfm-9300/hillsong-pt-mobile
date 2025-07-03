<script>
    import { onMount } from 'svelte';

    let posts = [];

    onMount(async () => {
        const response = await fetch('/api/posts');
        if (response.ok) {
            const data = await response.json();
            posts = data.data.postList;
        }
    });

    async function deletePost(postId) {
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
    }
</script>

<div class="p-8">
    <div class="flex items-center justify-between">
        <h1 class="text-2xl font-bold">Manage Posts</h1>
        <a href="/admin/posts/create" class="px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
            Create Post
        </a>
    </div>

    <div class="mt-8">
        <table class="w-full text-left bg-white rounded-lg shadow-md">
            <thead>
                <tr>
                    <th class="p-4">Title</th>
                    <th class="p-4">Content</th>
                    <th class="p-4">Actions</th>
                </tr>
            </thead>
            <tbody>
                {#each posts as post}
                    <tr class="border-t">
                        <td class="p-4">{post.title}</td>
                        <td class="p-4">{post.content}</td>
                        <td class="p-4">
                            <a href="/admin/posts/{post.id}" class="text-blue-600 hover:underline">Edit</a>
                            <button on:click={() => deletePost(post.id)} class="ml-4 text-red-600 hover:underline">Delete</button>
                        </td>
                    </tr>
                {/each}
            </tbody>
        </table>
    </div>
</div>

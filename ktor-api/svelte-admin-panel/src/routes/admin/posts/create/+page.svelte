<script>
    import { goto } from '$app/navigation';

    let title = '';
    let content = '';
    let image = null;
    let message = '';
    let isError = false;

    async function createPost() {
        const token = localStorage.getItem('authToken');
        const formData = new FormData();
        formData.append('title', title);
        formData.append('content', content);
        if (image) {
            formData.append('image', image);
        }

        const response = await fetch('/api/posts/create', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        const result = await response.json();

        if (response.ok) {
            isError = false;
            message = result.message;
            setTimeout(() => {
                goto('/admin/posts');
            }, 2000);
        } else {
            isError = true;
            message = result.message;
        }
    }

    function onFileSelected(e) {
        image = e.target.files[0];
    }
</script>

<div class="p-8">
    <h1 class="text-2xl font-bold">Create Post</h1>

    {#if message}
        <div class="p-4 mt-4 text-white" class:bg-green-500={!isError} class:bg-red-500={isError}>
            {message}
        </div>
    {/if}

    <form on:submit|preventDefault={createPost} class="mt-8 space-y-6">
        <div>
            <label for="title" class="text-sm font-medium">Title</label>
            <input type="text" id="title" bind:value={title} class="w-full px-3 py-2 mt-1 border rounded-md" required>
        </div>
        <div>
            <label for="content" class="text-sm font-medium">Content</label>
            <textarea id="content" bind:value={content} class="w-full px-3 py-2 mt-1 border rounded-md" required></textarea>
        </div>
        <div>
            <label for="image" class="text-sm font-medium">Image</label>
            <input type="file" id="image" on:change={onFileSelected} class="w-full px-3 py-2 mt-1 border rounded-md">
        </div>
        <button type="submit" class="w-full px-4 py-2 mt-4 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
            Create Post
        </button>
    </form>
</div>

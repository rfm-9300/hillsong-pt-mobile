<script>
    import { goto } from '$app/navigation';
    import { fade, fly } from 'svelte/transition';
    import Cropper from 'cropperjs';
    import 'cropperjs/dist/cropper.css';

    let title = '';
    let content = '';
    let image = null;
    let imagePreview = null;
    let message = '';
    let isError = false;
    let loading = false;
    let dragActive = false;
    let cropper;
    let imageElement;

    async function createPost() {
        loading = true;
        message = '';

        try {
            const token = localStorage.getItem('authToken');
            const formData = new FormData();
            formData.append('title', title);
            formData.append('content', content);

            if (cropper) {
                const canvas = cropper.getCroppedCanvas({
                    width: 400,
                    height: 200
                });
                canvas.toBlob(async (blob) => {
                    formData.append('image', blob, 'cropped-image.jpg');
                    await sendFormData(formData, token);
                }, 'image/jpeg');
            } else {
                await sendFormData(formData, token);
            }
        } catch (error) {
            isError = true;
            message = `Error: ${error.message}`;
            console.error(error);
            loading = false;
        }
    }

    async function sendFormData(formData, token) {
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
            message = result.message || 'Post created successfully!';
            setTimeout(() => {
                goto('/admin/posts');
            }, 2000);
        } else {
            isError = true;
            message = result.message || 'Failed to create post';
        }
        loading = false;
    }

    function onFileSelected(e) {
        const file = e.target.files[0];
        if (file) {
            createImagePreview(file);
        }
    }

    function createImagePreview(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            imagePreview = e.target.result;
            if (cropper) {
                cropper.destroy();
            }
            setTimeout(() => {
                if (imageElement) {
                    cropper = new Cropper(imageElement, {
                        aspectRatio: 2 / 1,
                        viewMode: 1,
                    });
                }
            }, 100);
        };
        reader.readAsDataURL(file);
    }

    function handleDragEnter(e) {
        e.preventDefault();
        dragActive = true;
    }

    function handleDragLeave(e) {
        e.preventDefault();
        dragActive = false;
    }

    function handleDrop(e) {
        e.preventDefault();
        dragActive = false;

        if (e.dataTransfer.files.length) {
            const file = e.dataTransfer.files[0];
            if (file.type.startsWith('image/')) {
                createImagePreview(file);
            }
        }
    }

    function goBack() {
        goto('/admin/posts');
    }

    function removeImage() {
        image = null;
        imagePreview = null;
        if (cropper) {
            cropper.destroy();
            cropper = null;
        }
        const fileInput = document.getElementById('image');
        if (fileInput) fileInput.value = '';
    }
</script>

<div class="p-8 w-full max-w-3xl mx-auto bg-gray-50 rounded-xl shadow-lg">
    <div class="mb-8">
        <button on:click={goBack} class="flex items-center text-gray-600 hover:text-blue-600 transition-colors cursor-pointer">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back to Posts
        </button>
    </div>

    <div class="flex items-center justify-between mb-6">
        <h1 class="text-3xl font-bold text-gray-800">Create New Post</h1>
    </div>

    {#if message}
    <div in:fly={{ y: -20, duration: 300 }} class="p-4 rounded-lg mb-6 flex items-center" class:bg-green-50={!isError} class:text-green-700={!isError} class:bg-red-50={isError} class:text-red-700={isError}>
        {#if !isError}
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        {:else}
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {/if}
        <p>{message}</p>
    </div>
    {/if}

    <div in:fly={{ y: 20, duration: 300 }} class="bg-white rounded-xl shadow-lg p-6 space-y-6">
        <form on:submit|preventDefault={createPost} class="space-y-6">
            <div>
                <label for="title" class="block text-sm font-medium text-gray-700 mb-1">Title</label>
                <input type="text" id="title" bind:value={title} class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" placeholder="Enter post title" required disabled={loading}>
            </div>

            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Featured Image</label>
                {#if imagePreview}
                <div class="relative rounded-lg overflow-hidden mb-3 bg-gray-100">
                    <img src={imagePreview} alt="Image preview" bind:this={imageElement} class="w-full">
                    <button type="button" on:click={removeImage} class="absolute top-2 right-2 bg-red-600 text-white rounded-full p-1 hover:bg-red-700 transition-colors cursor-pointer" title="Remove image">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>
                {:else}
                <div class="border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors" class:border-gray-300={!dragActive} class:border-blue-500={dragActive} class:bg-gray-50={!dragActive} class:bg-blue-50={dragActive} on:dragenter={handleDragEnter} on:dragover|preventDefault on:dragleave={handleDragLeave} on:drop={handleDrop} on:click={() => document.getElementById('image').click()}>
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <p class="mt-3 text-sm text-gray-600">Drag and drop image here, or click to select file</p>
                    <p class="mt-1 text-xs text-gray-500">PNG, JPG, GIF up to 5MB</p>
                </div>
                {/if}
                <input type="file" id="image" on:change={onFileSelected} accept="image/*" class="hidden">
            </div>

            <div>
                <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Content</label>
                <textarea id="content" bind:value={content} class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" rows="8" placeholder="Write your post content here..." required disabled={loading}></textarea>
            </div>

            <div class="flex items-center pt-4 border-t border-gray-200">
                <button type="button" on:click={goBack} class="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg mr-4 hover:bg-gray-200 transition-colors cursor-pointer" disabled={loading}>
                    Cancel
                </button>
                <button type="submit" class="flex items-center justify-center px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex-1 cursor-pointer" disabled={loading}>
                    {#if loading}
                    <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Creating Post...
                    {:else}
                    Create Post
                    {/if}
                </button>
            </div>
        </form>
    </div>
</div>
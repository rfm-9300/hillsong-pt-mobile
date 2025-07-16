<script>
    import { onMount } from 'svelte';
    import { page } from '$app/stores';
    import { goto } from '$app/navigation';
    import { fade, fly } from 'svelte/transition';
    import Cropper from 'cropperjs';
    import 'cropperjs/dist/cropper.css';
    import { api } from '$lib/api';

    export let data;
    let post = data.post;
    let title = post.title;
    let content = post.content;
    let headerImagePath = post.headerImagePath || '';
    let image = null;
    let imagePreview = getImageUrl(headerImagePath);
    let saving = false;
    let errorMessage = '';
    let successMessage = '';
    let cropper;
    let imageElement;

    function getImageUrl(imagePath) {
        if (!imagePath) return null;
        return `http://localhost:8080/resources/uploads/images/${imagePath}`;
    }

    async function updatePost() {
        saving = true;
        errorMessage = '';
        successMessage = '';

        try {
            const postId = $page.params.id;
            const formData = new FormData();
            formData.append('postId', postId);
            formData.append('title', title);
            formData.append('content', content);

            if (cropper && image) {
                const canvas = cropper.getCroppedCanvas({
                    width: 400,
                    height: 200
                });
                canvas.toBlob(async (blob) => {
                    formData.append('image', blob, 'cropped-image.jpg');
                    await sendFormData(formData);
                }, 'image/jpeg');
            } else {
                await sendFormData(formData);
            }
        } catch (error) {
            errorMessage = `Error updating post: ${error.message}`;
            console.error(error);
            saving = false;
        }
    }

    async function sendFormData(formData) {
        try {
            await api.updatePost(formData);
            successMessage = 'Post updated successfully!';
            setTimeout(() => goto('/admin/posts'), 1500);
        } catch (error) {
            errorMessage = error.message || 'Failed to update post';
        }
        saving = false;
    }

    function onFileSelected(e) {
        const file = e.target.files[0];
        if (file) {
            image = file;
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
    <div class="mb-8 w-[70%]">
        <button on:click={goBack} class="flex items-center text-gray-600 hover:text-blue-600 transition-colors cursor-pointer">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back to Posts
        </button>
    </div>

    <div class="flex items-center justify-between mb-6">
        <h1 class="text-3xl font-bold text-gray-800">Edit Post</h1>
    </div>

    {#if !post}
        <div in:fade class="bg-red-50 text-red-700 p-4 rounded-lg mb-6">
            <p>Post not found</p>
            <button on:click={goBack} class="mt-3 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                Return to Posts
            </button>
        </div>
    {:else}
        <div in:fly={{ y: 20, duration: 300 }} class="bg-white rounded-xl shadow-lg p-6 space-y-8">
            {#if successMessage}
                <div class="bg-green-50 text-green-700 p-4 rounded-lg mb-6 flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                    <p>{successMessage}</p>
                </div>
            {/if}

            {#if errorMessage}
                <div class="bg-red-50 text-red-700 p-4 rounded-lg mb-6">
                    <p>{errorMessage}</p>
                </div>
            {/if}

            <form on:submit|preventDefault={updatePost} class="space-y-6">
                <div>
                    <label for="title" class="block text-sm font-medium text-gray-700 mb-1">Title</label>
                    <input type="text" id="title" bind:value={title} class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" required disabled={saving}>
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">Featured Image</label>
                    <div class="bg-gray-50 rounded-lg overflow-hidden">
                        {#if imagePreview}
                            <div class="relative">
                                <img src={imagePreview} alt="Image preview" bind:this={imageElement} class="w-full">
                                <button type="button" on:click={removeImage} class="absolute top-2 right-2 bg-red-600 text-white rounded-full p-1 hover:bg-red-700 transition-colors cursor-pointer" title="Remove image">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            </div>
                        {:else}
                            <div class="border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors" on:click={() => document.getElementById('image').click()}>
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                                <p class="mt-3 text-sm text-gray-600">Click to select file</p>
                            </div>
                        {/if}
                    </div>
                    <input type="file" id="image" on:change={onFileSelected} accept="image/*" class="hidden">
                </div>

                <div>
                    <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Content</label>
                    <textarea id="content" bind:value={content} class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" rows="8" required disabled={saving}></textarea>
                </div>

                <div class="flex items-center pt-4 border-t border-gray-200">
                    <button type="button" on:click={goBack} class="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg mr-4 hover:bg-gray-200 transition-colors cursor-pointer hover:shadow-md" disabled={saving}>
                        Cancel
                    </button>
                    <button type="submit" class="flex items-center justify-center px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex-1 cursor-pointer hover:shadow-md" disabled={saving}>
                        {#if saving}
                            <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            Saving Changes...
                        {:else}
                            Update Post
                        {/if}
                    </button>
                </div>
            </form>
        </div>
    {/if}
</div>
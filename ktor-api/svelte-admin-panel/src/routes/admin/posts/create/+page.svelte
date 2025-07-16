<script>
    import { goto } from '$app/navigation';
    import Cropper from 'cropperjs';
    import 'cropperjs/dist/cropper.css';
    import { api } from '$lib/api';
    import FormContainer from '$lib/components/FormContainer.svelte';
    import Input from '$lib/components/Input.svelte';
    import Textarea from '$lib/components/Textarea.svelte';
    import Button from '$lib/components/Button.svelte';
    import Alert from '$lib/components/Alert.svelte';

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
                    await sendFormData(formData);
                }, 'image/jpeg');
            } else {
                await sendFormData(formData);
            }
        } catch (error) {
            isError = true;
            message = `Error: ${error.message}`;
            console.error(error);
            loading = false;
        }
    }

    async function sendFormData(formData) {
        try {
            const result = await api.postForm(api.endpoints.POST_CREATE, formData);
            isError = false;
            message = result.message || 'Post created successfully!';
            setTimeout(() => {
                goto('/admin/posts');
            }, 2000);
        } catch (error) {
            isError = true;
            message = error.message || 'Failed to create post';
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

<FormContainer 
    title="Create New Post" 
    subtitle="Write and publish a new blog post"
    backButton={true} 
    onBack={goBack}
    onSubmit={createPost}
>
    {#if message}
        <Alert 
            type={isError ? 'error' : 'success'} 
            message={message}
            dismissible={true}
            onDismiss={() => message = ''}
        />
    {/if}

    <Input 
        label="Title" 
        bind:value={title} 
        placeholder="Enter post title"
        required={true}
        disabled={loading}
        id="title"
    />

    <!-- Featured Image with Cropper -->
    <div class="space-y-2">
        <label class="block text-sm font-medium text-gray-700">Featured Image</label>
        
        {#if imagePreview}
            <div class="relative rounded-lg overflow-hidden bg-gray-100">
                <img src={imagePreview} alt="Post featured image preview" bind:this={imageElement} class="w-full">
                <Button 
                    variant="danger"
                    size="sm"
                    onclick={removeImage}
                    class="absolute top-2 right-2 !p-1 !rounded-full"
                    title="Remove image"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </Button>
            </div>
        {:else}
            <div 
                class="border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors hover:bg-gray-50"
                class:border-gray-300={!dragActive} 
                class:border-indigo-500={dragActive}
                class:bg-indigo-50={dragActive}
                on:dragenter={handleDragEnter}
                on:dragover|preventDefault
                on:dragleave={handleDragLeave}
                on:drop={handleDrop}
                on:click={() => document.getElementById('image').click()}
                role="button"
                tabindex="0"
            >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                <p class="mt-3 text-sm text-gray-600">Drag and drop image here, or click to select file</p>
                <p class="mt-1 text-xs text-gray-500">PNG, JPG, GIF up to 5MB (will be cropped to 2:1 ratio)</p>
            </div>
        {/if}
        
        <input 
            type="file" 
            id="image" 
            on:change={onFileSelected} 
            accept="image/*"
            class="hidden"
        >
    </div>
    
    <Textarea 
        label="Content" 
        bind:value={content} 
        placeholder="Write your post content here..."
        required={true}
        disabled={loading}
        rows={8}
        id="content"
    />
    
    <div class="flex items-center gap-3 pt-4 border-t border-gray-200">
        <Button 
            variant="secondary" 
            onclick={goBack} 
            disabled={loading}
        >
            Cancel
        </Button>
        <Button 
            type="submit" 
            loading={loading}
            disabled={loading}
            class="flex-1"
        >
            {loading ? 'Creating Post...' : 'Create Post'}
        </Button>
    </div>
</FormContainer>
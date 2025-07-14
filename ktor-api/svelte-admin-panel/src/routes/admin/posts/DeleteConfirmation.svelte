<script>
    import { fly, fade } from 'svelte/transition';

    let { onConfirm, onCancel } = $props();
</script>

<div class="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50" transition:fade>
    <div class="bg-white rounded-lg p-6 max-w-sm mx-4 shadow-xl" in:fly={{ y: 20, duration: 200 }}>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">Confirm Deletion</h3>
        <p class="text-gray-700 mb-6">Are you sure you want to delete this post? This action cannot be undone.</p>
        
        <div class="flex justify-end gap-3">
            <button 
                onclick={onCancel}
                class="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors cursor-pointer"
            >
                Cancel
            </button>
            <button 
                onclick={onConfirm}
                class="px-4 py-2 text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors cursor-pointer"
            >
                Delete
            </button>
        </div>
    </div>
</div>
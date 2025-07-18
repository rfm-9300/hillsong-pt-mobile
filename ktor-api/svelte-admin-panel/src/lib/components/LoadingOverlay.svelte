<script>
    // Props
    let { 
        show = false,
        fullScreen = false,
        message = 'Loading...',
        transparent = false,
        class: className = '',
        ...props
    } = $props();
</script>

{#if show}
    <div 
        class="
            {fullScreen ? 'fixed inset-0 z-50' : 'absolute inset-0 z-10'} 
            flex items-center justify-center 
            {transparent ? 'bg-white/70 dark:bg-gray-900/70' : 'bg-white/90 dark:bg-gray-900/90'}
            backdrop-blur-sm transition-opacity duration-300
            {className}
        "
        {...props}
    >
        <div class="flex flex-col items-center">
            <div class="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-indigo-500"></div>
            {#if message}
                <p class="mt-3 text-sm text-gray-600 dark:text-gray-300">{message}</p>
            {/if}
        </div>
    </div>
{/if}
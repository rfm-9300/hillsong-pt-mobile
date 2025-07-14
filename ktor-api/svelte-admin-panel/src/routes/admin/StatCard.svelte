<script>
    import { fly } from 'svelte/transition';

    let { title, value, loading, link, color, delay } = $props();

    const colorClasses = {
        blue: {
            bg: 'bg-blue-50',
            text: 'text-blue-500',
            hover: 'hover:text-blue-800'
        },
        purple: {
            bg: 'bg-purple-50',
            text: 'text-purple-500',
            hover: 'hover:text-purple-800'
        },
        green: {
            bg: 'bg-green-50',
            text: 'text-green-500',
            hover: 'hover:text-green-800'
        }
    };

    const selectedColor = colorClasses[color] || colorClasses.blue;
</script>

<a 
    href={link}
    class="block cursor-pointer"
    in:fly={{ y: 20, duration: 300, delay: delay }}
>
    <div class="bg-white rounded-xl shadow-md p-6 border border-gray-100 hover:shadow-lg transition-all duration-200 hover:scale-[1.02]">
        <div class="flex items-center justify-between">
            <div>
                <p class="text-sm font-medium text-gray-500 mb-1">{title}</p>
                {#if loading}
                    <div class="h-8 w-16 bg-gray-200 animate-pulse rounded"></div>
                {:else}
                    <h2 class="text-3xl font-bold text-gray-800">{value}</h2>
                {/if}
            </div>
            <div class={`p-3 ${selectedColor.bg} rounded-lg`}>
                <slot name="icon" />
            </div>
        </div>
        
        <div class="mt-4">
            <span class={`${selectedColor.hover} flex items-center text-sm font-medium ${selectedColor.text}`}>
                View all
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
            </span>
        </div>
    </div>
</a>
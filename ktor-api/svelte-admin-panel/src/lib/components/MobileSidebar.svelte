<script>
	import { fly, fade } from 'svelte/transition';
	import Navigation from './Navigation.svelte';

	let { isOpen, onClose } = $props();
</script>

{#if isOpen}
	<button
		type="button"
		transition:fade={{duration: 150}}
		class="md:hidden fixed inset-0 z-20 bg-black bg-opacity-50"
		aria-label="Close sidebar overlay"
		tabindex="0"
		onclick={onClose}
		onkeydown={(e) => { if (e.key === 'Enter' || e.key === ' ') { onClose(); } }}
		style="background: none; border: none; padding: 0; margin: 0;"
	></button>
	
	<aside 
		transition:fly={{x: -300, duration: 200}}
		class="md:hidden fixed inset-y-0 left-0 z-30 w-64 bg-white dark:bg-gray-800 shadow-xl"
	>
		<div class="flex flex-col h-full">
			<div class="flex items-center justify-between h-16 px-4 border-b border-gray-200 dark:border-gray-700 bg-blue-600 dark:bg-blue-700">
				<div class="text-xl font-bold text-white">Admin Panel</div>
				<button onclick={onClose} class="p-1 text-white" aria-label="Close sidebar">
					<svg class="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
					</svg>
				</button>
			</div>
			<Navigation onLinkClick={onClose} />
		</div>
	</aside>
{/if}
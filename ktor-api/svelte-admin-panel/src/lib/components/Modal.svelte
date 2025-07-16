<script>
	import { fade, fly } from 'svelte/transition';
	
	let {
		show = false,
		title = '',
		onClose = () => {},
		children,
		size = 'md'
	} = $props();

	const sizes = {
		sm: 'max-w-sm',
		md: 'max-w-md',
		lg: 'max-w-lg',
		xl: 'max-w-xl'
	};

	function handleBackdropClick(e) {
		if (e.target === e.currentTarget) {
			onClose();
		}
	}
</script>

{#if show}
	<div 
		class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4" 
		transition:fade={{ duration: 200 }}
		on:click={handleBackdropClick}
		role="dialog"
		aria-modal="true"
	>
		<div 
			class="bg-white rounded-xl shadow-xl w-full {sizes[size]} mx-4"
			in:fly={{ y: 20, duration: 200 }}
		>
			{#if title}
				<div class="px-6 py-4 border-b border-gray-200">
					<h3 class="text-lg font-semibold text-gray-900">{title}</h3>
				</div>
			{/if}
			
			<div class="p-6">
				{@render children()}
			</div>
		</div>
	</div>
{/if}
<script>
	let {
		title,
		subtitle = '',
		backButton = false,
		onBack = () => {},
		children
	} = $props();
</script>

<div class="mb-8">
	{#if backButton}
		<button 
			on:click={onBack} 
			class="flex items-center text-gray-600 hover:text-indigo-600 transition-colors mb-4 group"
		>
			<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 transition-transform group-hover:-translate-x-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
			</svg>
			Back
		</button>
	{/if}
	
	<div class="flex items-center justify-between">
		<div>
			<h1 class="text-3xl font-bold text-gray-900">{title}</h1>
			{#if subtitle}
				<p class="mt-2 text-gray-600">{subtitle}</p>
			{/if}
		</div>
		
		{#if children}
			<div class="flex items-center gap-3">
				{@render children()}
			</div>
		{/if}
	</div>
</div>
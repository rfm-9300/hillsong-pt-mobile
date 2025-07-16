<script>
	let {
		icon,
		title,
		description,
		actionText = '',
		onAction = () => {},
		children
	} = $props();
</script>

<div class="text-center py-12">
	<div class="mx-auto h-24 w-24 text-gray-400 mb-4">
		{@render icon()}
	</div>
	
	<h3 class="text-xl font-semibold text-gray-700 mb-2">{title}</h3>
	<p class="text-gray-500 mb-6 max-w-sm mx-auto">{description}</p>
	
	{#if actionText}
		<button 
			on:click={onAction}
			class="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors"
		>
			{actionText}
		</button>
	{/if}
	
	{#if children}
		<div class="mt-4">
			{@render children()}
		</div>
	{/if}
</div>
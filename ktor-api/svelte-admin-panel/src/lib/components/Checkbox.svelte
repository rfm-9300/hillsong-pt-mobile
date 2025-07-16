<script>
	let {
		checked = $bindable(false),
		disabled = false,
		label = '',
		id = '',
		class: className = '',
		...props
	} = $props();
</script>

<div class="flex items-center space-x-3">
	<input 
		type="checkbox" 
		{id}
		{disabled}
		bind:checked
		class="w-5 h-5 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500 cursor-pointer disabled:cursor-not-allowed {className}"
		{...props}
	>
	{#if label}
		<label for={id} class="text-sm font-medium text-gray-700 cursor-pointer">
			{label}
		</label>
	{/if}
</div>
<script>
	let {
		type = 'text',
		value = $bindable(''),
		placeholder = '',
		disabled = false,
		required = false,
		error = '',
		label = '',
		id = '',
		class: className = '',
		...props
	} = $props();

	const baseClasses = 'w-full px-4 py-3 border rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 disabled:cursor-not-allowed';
	const errorClasses = error ? 'border-red-300 focus:ring-red-500 focus:border-red-500' : 'border-gray-300';
</script>

<div class="space-y-1">
	{#if label}
		<label for={id} class="block text-sm font-medium text-gray-700">
			{label}
			{#if required}<span class="text-red-500">*</span>{/if}
		</label>
	{/if}
	
	<input
		{type}
		{id}
		{placeholder}
		{disabled}
		{required}
		bind:value
		class="{baseClasses} {errorClasses} {className}"
		{...props}
	/>
	
	{#if error}
		<p class="text-sm text-red-600">{error}</p>
	{/if}
</div>
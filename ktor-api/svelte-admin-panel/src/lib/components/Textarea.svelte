<script>
	let {
		value = $bindable(''),
		placeholder = '',
		disabled = false,
		required = false,
		error = '',
		label = '',
		id = '',
		rows = 4,
		maxLength = null,
		showCharCount = false,
		class: className = '',
		...props
	} = $props();

	const baseClasses = 'w-full px-4 py-3 border rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 disabled:cursor-not-allowed resize-vertical';
	const errorClasses = error ? 'border-red-300 focus:ring-red-500 focus:border-red-500' : 'border-gray-300';
	
	// Calculate character count and validation state
	$effect(() => {
		if (maxLength && value && value.length > maxLength) {
			if (!error) {
				error = `Maximum length is ${maxLength} characters`;
			}
		}
	});
</script>

<div class="space-y-1">
	{#if label}
		<label for={id} class="block text-sm font-medium text-gray-700">
			{label}
			{#if required}<span class="text-red-500">*</span>{/if}
		</label>
	{/if}
	
	<textarea
		{id}
		{placeholder}
		{disabled}
		{required}
		{rows}
		bind:value
		class="{baseClasses} {errorClasses} {className}"
		{...props}
	></textarea>
	
	{#if error}
		<p class="text-sm text-red-600">{error}</p>
	{/if}
	
	{#if showCharCount && maxLength}
		<div class="flex justify-end">
			<span class="text-xs text-gray-500 {value && value.length > maxLength ? 'text-red-500' : ''}">
				{value ? value.length : 0}/{maxLength}
			</span>
		</div>
	{/if}
</div>
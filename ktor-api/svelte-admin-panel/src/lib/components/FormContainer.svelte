<script>
	import Card from './Card.svelte';
	import PageHeader from './PageHeader.svelte';
	
	let {
		title,
		subtitle = '',
		backButton = false,
		onBack = () => {},
		onSubmit = () => {},
		children,
		maxWidth = 'max-w-3xl'
	} = $props();
</script>

<div class="w-full {maxWidth} mx-auto">
	<PageHeader {title} {subtitle} {backButton} {onBack} />
	
	<Card padding="p-8" shadow="shadow-lg">
		<form on:submit|preventDefault={onSubmit} class="space-y-6">
			{@render children()}
		</form>
	</Card>
</div>
<script>
	let {
		padding = 'p-6',
		shadow = 'shadow-sm',
		border = true,
		hover = false,
		children,
		class: className = '',
		...props
	} = $props();

	const baseClasses = 'bg-white rounded-xl transition-all duration-200';
	const borderClass = border ? 'border border-gray-200' : '';
	const hoverClass = hover ? 'hover:shadow-lg hover:scale-[1.01]' : '';
</script>

<div
	class="{baseClasses} {borderClass} {shadow} {hoverClass} {padding} {className}"
	{...props}
>
	{@render children()}
</div>
<script>
	let { onMenuClick } = $props();
</script>

<div class="md:hidden fixed top-0 left-0 right-0 z-20 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
	<div class="flex items-center justify-between h-16 px-4">
		<button
			on:click={onMenuClick}
			class="p-1 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-500"
		>
			<svg class="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
			</svg>
		</button>
		<div class="text-xl font-bold text-gray-800 dark:text-white">Admin Panel</div>
		<div class="w-6"></div> <!-- Empty div for balance -->
	</div>
</div>
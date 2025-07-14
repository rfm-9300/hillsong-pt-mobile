<script>
	import { fly } from 'svelte/transition';
	import Navigation from './Navigation.svelte';
</script>

<aside 
	class="hidden md:flex md:flex-col md:w-64 md:fixed md:inset-y-0 z-10"
	in:fly={{x: -100, duration: 200}}
>
	<div class="flex flex-col flex-grow min-h-0 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 shadow-lg">
		<div class="flex items-center justify-center h-16 px-4 bg-blue-600 dark:bg-blue-700">
			<span class="text-xl font-bold text-white">Admin Panel</span>
		</div>
		<div class="flex flex-col flex-1 overflow-y-auto">
			<Navigation />
		</div>
	</div>
</aside>
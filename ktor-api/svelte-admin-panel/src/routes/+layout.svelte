<script>
	import '../app.css';
	import { auth } from '$lib/authStore';
	import Sidebar from '../lib/components/Sidebar.svelte';
	import MobileHeader from '../lib/components/MobileHeader.svelte';
	import MobileSidebar from '../lib/components/MobileSidebar.svelte';

	let { children } = $props();
	let sidebarOpen = $state(false);

	function toggleSidebar() {
		sidebarOpen = !sidebarOpen;
	}
</script>

<div class="flex h-screen bg-gray-50">
	{#if $auth}
		<Sidebar />
		<MobileHeader onMenuClick={toggleSidebar} />
		<MobileSidebar isOpen={sidebarOpen} onClose={toggleSidebar} />

		<main class="flex-1 md:ml-64">
			<div class="flex flex-col w-full md:p-8 p-4 min-h-screen">
				{@render children()}
			</div>
		</main>
	{:else}
		<div class="flex items-center justify-center min-h-screen w-full bg-gradient-to-br from-indigo-50 to-white">
			{@render children()}
		</div>
	{/if}
</div>
<script>
	import { auth } from '$lib/authStore';
	import { page } from '$app/stores';

	let { onLinkClick = () => {} } = $props();
	
	const currentPath = $derived($page.url.pathname);
	
	// Track expanded state of navigation sections using separate state variable for better reactivity
	let attendanceExpanded = $state(false);

	function isActive(path) {
		if (path === '/admin/dashboard' && currentPath !== '/admin/dashboard') {
			return false;
		}
		return currentPath.startsWith(path);
	}
	
	// Debug function to log state changes
	function toggleAttendance() {
		console.log('Before toggle:', attendanceExpanded);
		attendanceExpanded = !attendanceExpanded;
		console.log('After toggle:', attendanceExpanded);
	}
	
	// Auto-expand sections based on current path
	$effect(() => {
		if (currentPath.startsWith('/admin/attendance')) {
			attendanceExpanded = true;
		}
	});

	function logout() {
		auth.logout();
		window.location.href = '/login';
	}
</script>

<nav class="flex-1 px-2 py-4 space-y-1">
	<a href="/admin/dashboard"
		class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {isActive('/admin/dashboard') ? 'bg-blue-50 text-blue-700 dark:bg-blue-900 dark:text-blue-200' : ''}"
		onclick={onLinkClick}
	>
		<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
		</svg>
		Dashboard
	</a>
	<a href="/admin/posts"
		class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {isActive('/admin/posts') ? 'bg-blue-50 text-blue-700 dark:bg-blue-900 dark:text-blue-200' : ''}"
		onclick={onLinkClick}
	>
		<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
		</svg>
		Posts
	</a>
	<a
		href="/admin/events"
		class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {isActive('/admin/events') ? 'bg-blue-50 text-blue-700 dark:bg-blue-900 dark:text-blue-200' : ''}"
		onclick={onLinkClick}
	>
		<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
		</svg>
		Events
	</a>
	<a href="/admin/users"
		class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {isActive('/admin/users') ? 'bg-blue-50 text-blue-700 dark:bg-blue-900 dark:text-blue-200' : ''}"
		onclick={onLinkClick}
	>
		<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
		</svg>
		Users
	</a>
	<!-- Attendance section with dropdown -->
	<div class="space-y-1">
		<button
			class="w-full flex items-center justify-between px-4 py-3 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {isActive('/admin/attendance') ? 'bg-blue-50 text-blue-700 dark:bg-blue-900 dark:text-blue-200' : ''}"
			onclick={toggleAttendance}
		>
			<div class="flex items-center">
				<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
				</svg>
				Attendance
			</div>
			<svg 
				xmlns="http://www.w3.org/2000/svg" 
				class="h-5 w-5 transform transition-transform {attendanceExpanded ? 'rotate-180' : ''}" 
				fill="none" 
				viewBox="0 0 24 24" 
				stroke="currentColor"
			>
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
			</svg>
		</button>
		
		{#if attendanceExpanded}
			<div class="pl-4 ml-6 space-y-1 border-l border-gray-200 dark:border-gray-700">
				<a 
					href="/admin/attendance" 
					class="flex items-center px-4 py-2 text-sm text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {currentPath === '/admin/attendance' ? 'text-blue-700 dark:text-blue-300' : ''}"
					onclick={onLinkClick}
				>
					Overview
				</a>
				<a 
					href="/admin/attendance/event" 
					class="flex items-center px-4 py-2 text-sm text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {currentPath.includes('/admin/attendance/event') ? 'text-blue-700 dark:text-blue-300' : ''}"
					onclick={onLinkClick}
				>
					Events
				</a>
				<a 
					href="/admin/attendance/service" 
					class="flex items-center px-4 py-2 text-sm text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {currentPath.includes('/admin/attendance/service') ? 'text-blue-700 dark:text-blue-300' : ''}"
					onclick={onLinkClick}
				>
					Services
				</a>
				<a 
					href="/admin/attendance/kids-service" 
					class="flex items-center px-4 py-2 text-sm text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {currentPath.includes('/admin/attendance/kids-service') ? 'text-blue-700 dark:text-blue-300' : ''}"
					onclick={onLinkClick}
				>
					Kids Services
				</a>
				<a 
					href="/admin/attendance/reports" 
					class="flex items-center px-4 py-2 text-sm text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors {currentPath.includes('/admin/attendance/reports') ? 'text-blue-700 dark:text-blue-300' : ''}"
					onclick={onLinkClick}
				>
					Reports
				</a>
			</div>
		{/if}
	</div>
	<button
		onclick={logout}
		class="w-full flex items-center px-4 py-3 mt-6 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-red-50 hover:text-red-700 dark:hover:bg-red-900 dark:hover:text-red-200 transition-colors"
	>
		<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
		</svg>
		Logout
	</button>
</nav>
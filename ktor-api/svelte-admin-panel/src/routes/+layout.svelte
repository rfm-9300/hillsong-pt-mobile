<script>
	import '../app.css';
	let { children } = $props();
</script>

<div class="flex flex-col min-h-screen bg-gray-100 dark:bg-gray-900">
	<header class="bg-white dark:bg-gray-800 shadow-md">
		<nav class="container mx-auto px-6 py-3">
			<div class="flex items-center justify-between">
				<div class="text-xl font-semibold text-gray-700 dark:text-white">
					<a href="/">Logo</a>
				</div>
				<div class="flex items-center">
					<ul class="flex items-center space-x-6">
						<li><a href="/" class="text-gray-700 dark:text-white hover:text-blue-500 dark:hover:text-blue-400">Home</a></li>
						<li><a href="/admin" class="text-gray-700 dark:text-white hover:text-blue-500 dark:hover:text-blue-400">Admin</a></li>
						<li><a href="/login" class="text-gray-700 dark:text-white hover:text-blue-500 dark:hover:text-blue-400">Login</a></li>
					</ul>
				</div>
			</div>
		</nav>
	</header>

	<main class="flex-grow container mx-auto px-6 py-8">
		{@render children()}
	</main>

	<footer class="bg-white dark:bg-gray-800 shadow-inner mt-auto">
		<div class="container mx-auto px-6 py-4 text-center text-gray-600 dark:text-gray-300">
			<p>&copy; 2025 Your Company. All rights reserved.</p>
		</div>
	</footer>
</div>
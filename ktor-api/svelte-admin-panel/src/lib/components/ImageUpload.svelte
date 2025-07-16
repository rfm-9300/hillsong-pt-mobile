<script>
	import Button from './Button.svelte';
	
	let {
		image = $bindable(null),
		imagePreview = $bindable(null),
		label = 'Image',
		accept = 'image/*',
		disabled = false,
		dragText = 'Drag and drop image here, or click to select file',
		supportText = 'PNG, JPG, GIF up to 5MB'
	} = $props();

	let dragActive = false;
	let fileInput;

	function onFileSelected(e) {
		const file = e.target.files[0];
		if (file) {
			image = file;
			createImagePreview(file);
		}
	}
	
	function createImagePreview(file) {
		const reader = new FileReader();
		reader.onload = (e) => {
			imagePreview = e.target.result;
		};
		reader.readAsDataURL(file);
	}
	
	function handleDragEnter(e) {
		e.preventDefault();
		dragActive = true;
	}
	
	function handleDragLeave(e) {
		e.preventDefault();
		dragActive = false;
	}
	
	function handleDrop(e) {
		e.preventDefault();
		dragActive = false;
		
		if (e.dataTransfer.files.length) {
			const file = e.dataTransfer.files[0];
			if (file.type.startsWith('image/')) {
				image = file;
				createImagePreview(file);
			}
		}
	}
	
	function removeImage() {
		image = null;
		imagePreview = null;
		if (fileInput) fileInput.value = '';
	}
</script>

<div class="space-y-2">
	<label class="block text-sm font-medium text-gray-700">{label}</label>
	
	{#if imagePreview}
		<div class="relative rounded-lg overflow-hidden bg-gray-100">
			<img src={imagePreview} alt="Image preview" class="w-full h-64 object-cover">
			<Button 
				variant="danger"
				size="sm"
				onclick={removeImage}
				class="absolute top-2 right-2 !p-1 !rounded-full"
				title="Remove image"
			>
				<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
				</svg>
			</Button>
		</div>
	{:else}
		<!-- Drag and Drop Zone -->
		<div 
			class="border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors hover:bg-gray-50"
			class:border-gray-300={!dragActive} 
			class:border-indigo-500={dragActive}
			class:bg-indigo-50={dragActive}
			class:opacity-50={disabled}
			class:cursor-not-allowed={disabled}
			on:dragenter={handleDragEnter}
			on:dragover|preventDefault
			on:dragleave={handleDragLeave}
			on:drop={handleDrop}
			on:click={() => !disabled && fileInput.click()}
			role="button"
			tabindex="0"
		>
			<svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
			</svg>
			<p class="mt-3 text-sm text-gray-600">{dragText}</p>
			<p class="mt-1 text-xs text-gray-500">{supportText}</p>
		</div>
	{/if}
	
	<input 
		bind:this={fileInput}
		type="file" 
		on:change={onFileSelected} 
		{accept}
		{disabled}
		class="hidden"
	>
</div>
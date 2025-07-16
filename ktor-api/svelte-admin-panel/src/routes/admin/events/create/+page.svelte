<script>
	import { goto } from '$app/navigation';
	import { api } from '$lib/api';
	import FormContainer from '$lib/components/FormContainer.svelte';
	import Input from '$lib/components/Input.svelte';
	import Textarea from '$lib/components/Textarea.svelte';
	import Button from '$lib/components/Button.svelte';
	import Alert from '$lib/components/Alert.svelte';
	import Checkbox from '$lib/components/Checkbox.svelte';
	import ImageUpload from '$lib/components/ImageUpload.svelte';

	let title = '';
	let description = '';
	let date = '';
	let location = '';
	let maxAttendees = 0;
	let needsApproval = false;
	let image = null;
	let imagePreview = null;
	let loading = false;
	let message = '';
	let isError = false;
	async function createEvent() {
		loading = true;
		message = '';

		try {
			const formData = new FormData();
			formData.append('title', title);
			formData.append('description', description);
			formData.append('date', date);
			formData.append('location', location);
			formData.append('maxAttendees', maxAttendees);
			formData.append('needsApproval', needsApproval);

			if (image) {
				formData.append('image', image);
			}

			const result = await api.postForm(api.endpoints.EVENTS, formData);

			isError = false;
			message = result.message || 'Event created successfully!';
			setTimeout(() => {
				goto('/admin/events');
			}, 2000);
		} catch (error) {
			isError = true;
			message = `Error: ${error.message}`;
			console.error(error);
		} finally {
			loading = false;
		}
	}

	function goBack() {
		goto('/admin/events');
	}
</script>

<FormContainer
	title="Create New Event"
	subtitle="Fill in the details to create a new event"
	backButton={true}
	onBack={goBack}
	onSubmit={createEvent}
>
	{#if message}
		<Alert
			type={isError ? 'error' : 'success'}
			{message}
			dismissible={true}
			onDismiss={() => (message = '')}
		/>
	{/if}

	<Input
		label="Title"
		bind:value={title}
		placeholder="Enter event title"
		required={true}
		disabled={loading}
		id="title"
	/>

	<Input
		type="datetime-local"
		label="Date & Time"
		bind:value={date}
		required={true}
		disabled={loading}
		id="date"
	/>

	<Input
		label="Location"
		bind:value={location}
		placeholder="Enter event location"
		required={true}
		disabled={loading}
		id="location"
	/>

	<Input
		type="number"
		label="Max Attendees"
		bind:value={maxAttendees}
		placeholder="Enter maximum number of attendees"
		required={true}
		disabled={loading}
		id="maxAttendees"
		min="0"
	/>

	<Checkbox
		bind:checked={needsApproval}
		label="Registration needs admin approval"
		disabled={loading}
		id="needsApproval"
	/>

	<ImageUpload bind:image bind:imagePreview label="Event Image" disabled={loading} />

	<Textarea
		label="Description"
		bind:value={description}
		placeholder="Write event description here..."
		required={true}
		disabled={loading}
		rows={6}
		id="description"
	/>

	<div class="flex items-center gap-3 pt-4 border-t border-gray-200">
		<Button variant="secondary" onclick={goBack} disabled={loading}>Cancel</Button>
		<Button type="submit" {loading} disabled={loading} class="flex-1">
			{loading ? 'Creating Event...' : 'Create Event'}
		</Button>
	</div>
</FormContainer>

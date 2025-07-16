# Unified Design System

## Overview
This document outlines the unified design system implemented across your admin panel to ensure consistency and maintainability.

## Color Scheme
- **Primary**: Indigo (indigo-600, indigo-700)
- **Secondary**: Gray (gray-100, gray-200)
- **Success**: Green (green-600, green-700)
- **Danger**: Red (red-600, red-700)
- **Warning**: Yellow (yellow-600, yellow-700)

## Components Created

### Core Components

#### Button (`src/lib/components/Button.svelte`)
```svelte
<Button variant="primary" size="md" loading={false}>Click me</Button>
```
**Variants**: `primary`, `secondary`, `danger`, `ghost`, `success`
**Sizes**: `sm`, `md`, `lg`

#### Input (`src/lib/components/Input.svelte`)
```svelte
<Input label="Email" type="email" bind:value={email} required={true} />
```

#### Textarea (`src/lib/components/Textarea.svelte`)
```svelte
<Textarea label="Description" bind:value={description} rows={6} />
```

#### Checkbox (`src/lib/components/Checkbox.svelte`)
```svelte
<Checkbox bind:checked={isActive} label="Active" />
```

### Layout Components

#### Card (`src/lib/components/Card.svelte`)
```svelte
<Card hover={true} padding="p-6">
    Content here
</Card>
```

#### PageHeader (`src/lib/components/PageHeader.svelte`)
```svelte
<PageHeader title="Page Title" subtitle="Description" backButton={true} onBack={goBack}>
    <Button>Action</Button>
</PageHeader>
```

#### FormContainer (`src/lib/components/FormContainer.svelte`)
```svelte
<FormContainer title="Create Item" onSubmit={handleSubmit}>
    <!-- Form fields here -->
</FormContainer>
```

### Specialized Components

#### Modal (`src/lib/components/Modal.svelte`)
```svelte
<Modal show={showModal} title="Confirm" onClose={closeModal}>
    Modal content
</Modal>
```

#### Alert (`src/lib/components/Alert.svelte`)
```svelte
<Alert type="success" message="Success!" dismissible={true} />
```

#### EmptyState (`src/lib/components/EmptyState.svelte`)
```svelte
<EmptyState title="No Items" description="Create your first item">
    {#snippet icon()}
        <svg>...</svg>
    {/snippet}
</EmptyState>
```

#### ImageUpload (`src/lib/components/ImageUpload.svelte`)
```svelte
<ImageUpload bind:image={image} bind:imagePreview={imagePreview} label="Upload Image" />
```

## Updated Pages

### Events
- ✅ `/admin/events` - Modern card grid layout
- ✅ `/admin/events/create` - Unified form design
- 🔄 `/admin/events/[id]` - Ready for update

### Posts
- ✅ `/admin/posts/create` - Unified form design with cropper
- 🔄 `/admin/posts` - Ready for update
- 🔄 `/admin/posts/[id]` - Ready for update

## Design Principles

1. **Consistency**: All buttons, inputs, and layouts follow the same design patterns
2. **Accessibility**: Proper ARIA labels, keyboard navigation, and focus states
3. **Responsiveness**: Mobile-first design with responsive breakpoints
4. **Modern**: Clean, minimal design with subtle shadows and smooth transitions
5. **Reusability**: Components are highly configurable and reusable

## Usage Guidelines

### Colors
- Use `indigo` as the primary brand color (replaces purple/blue inconsistencies)
- Use `gray` for neutral elements
- Use semantic colors (`red` for danger, `green` for success)

### Spacing
- Use Tailwind's spacing scale consistently
- Standard padding: `p-6` for cards, `p-4` for smaller elements
- Standard gaps: `gap-3` for button groups, `gap-6` for card grids

### Typography
- Page titles: `text-3xl font-bold text-gray-900`
- Section titles: `text-xl font-semibold text-gray-700`
- Body text: `text-gray-600`
- Labels: `text-sm font-medium text-gray-700`

### Shadows
- Cards: `shadow-sm` (default), `shadow-lg` (elevated)
- Hover states: `hover:shadow-lg`
- Modals: `shadow-xl`

## Next Steps

To complete the unified design system:

1. Update remaining pages (`/admin/posts`, `/admin/posts/[id]`, `/admin/events/[id]`)
2. Update dashboard components to use the new design system
3. Consider creating additional specialized components as needed
4. Update the sidebar and navigation to match the new design

## Benefits

- **Maintainability**: Changes to design can be made in one place
- **Consistency**: Users get a cohesive experience across all pages
- **Developer Experience**: Faster development with reusable components
- **Accessibility**: Built-in accessibility features across all components
- **Performance**: Smaller bundle size through component reuse
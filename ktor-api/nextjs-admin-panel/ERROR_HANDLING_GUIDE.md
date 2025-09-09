# Enhanced Error Handling & Loading States Guide

## Overview

This guide covers the comprehensive error handling and loading state management system implemented in the Next.js admin panel. The system provides consistent error processing, user-friendly feedback, and smooth loading experiences across the application.

## Architecture

### Core Components

1. **Error Handling Service** (`/lib/errorHandlingService.ts`)
   - Centralized error processing and transformation
   - User-friendly error message generation
   - Error logging and reporting
   - Retry logic determination

2. **Loading State Service** (`/lib/loadingStateService.ts`)
   - Global loading state management
   - Progress tracking capabilities
   - Timeout handling
   - Category-based loading organization

3. **Enhanced API Service** (`/lib/enhancedApiService.ts`)
   - Integrated error handling and loading states
   - Automatic retry mechanisms
   - Batch request support
   - File upload with progress tracking

4. **React Hooks** (`/hooks/useApiCall.ts`)
   - `useEnhancedApiCall` - Single API call management
   - `useEnhancedMultipleApiCalls` - Batch API call management
   - Automatic error context integration
   - Loading state subscription

5. **UI Components**
   - `ErrorBoundary` - React error boundary with retry
   - `GlobalLoadingIndicator` - Application-wide loading feedback
   - `LoadingOverlay` - Component-level loading states
   - `RetryButton` - Smart retry functionality
   - `Alert` - User feedback notifications

## Usage Examples

### Basic API Call with Error Handling

```typescript
import { useEnhancedApiCall } from '@/app/hooks/useApiCall';

const MyComponent = () => {
  const { data, loading, error, execute, retry } = useEnhancedApiCall(
    () => api.get('/api/data'),
    {
      loadingKey: 'my_data',
      context: 'Loading My Data',
      message: 'Fetching your data...',
      onSuccess: (data) => console.log('Success:', data),
    }
  );

  return (
    <div>
      <button onClick={() => execute()} disabled={loading}>
        Load Data
      </button>
      {loading && <p>Loading...</p>}
      {error && <RetryButton onRetry={retry} />}
      {data && <div>{JSON.stringify(data)}</div>}
    </div>
  );
};
```

### Batch API Calls

```typescript
const apiCalls = {
  posts: () => api.get('/api/posts'),
  users: () => api.get('/api/users'),
  events: () => api.get('/api/events'),
};

const { data, loading, errors, executeAll, retry } = useEnhancedMultipleApiCalls(
  apiCalls,
  {
    batchLoadingKey: 'dashboard_data',
    showProgress: true,
    failFast: false,
  }
);
```

### Direct API Service Usage

```typescript
import { enhancedApiService } from '@/lib/enhancedApiService';

const handleSubmit = async (formData) => {
  try {
    const result = await enhancedApiService.post('/api/submit', formData, {
      loadingKey: 'form_submit',
      message: 'Submitting form...',
      context: 'Form Submission',
      retries: 2,
    });
    console.log('Success:', result);
  } catch (error) {
    // Error is automatically handled and displayed to user
    console.error('Submission failed:', error);
  }
};
```

## Best Practices

1. **Always provide context** - Include meaningful context for error messages
2. **Use appropriate loading keys** - Unique keys for different operations
3. **Handle success feedback** - Show success messages for user actions
4. **Implement retry logic** - Allow users to retry failed operations
5. **Progress tracking** - Use progress indicators for long operations

## Configuration

### Error Context Provider

Wrap your app with the ErrorProvider to enable global error handling:

```typescript
<ErrorProvider>
  <YourApp />
</ErrorProvider>
```

### Global Loading Indicator

Add to your layout for application-wide loading feedback:

```typescript
<CompactGlobalLoadingIndicator />
```

This system provides a robust foundation for handling errors and loading states consistently across the entire application.
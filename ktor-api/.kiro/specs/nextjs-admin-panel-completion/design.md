# Design Document

## Overview

The Next.js admin panel will be a complete migration of the existing Svelte admin panel, maintaining the same functionality while leveraging Next.js 14+ features including the App Router, Server Components where appropriate, and modern React patterns. The design will focus on creating reusable components, proper state management, and a responsive user interface that matches the visual design of the Svelte version.

## Architecture

### Application Structure
```
nextjs-admin-panel/
├── src/
│   ├── app/
│   │   ├── admin/
│   │   │   ├── dashboard/
│   │   │   ├── posts/
│   │   │   │   ├── create/
│   │   │   │   └── [id]/
│   │   │   ├── events/
│   │   │   │   ├── create/
│   │   │   │   └── [id]/
│   │   │   ├── users/
│   │   │   └── attendance/
│   │   │       ├── event/
│   │   │       ├── service/
│   │   │       ├── kids-service/
│   │   │       └── reports/
│   │   ├── components/
│   │   │   ├── ui/
│   │   │   ├── forms/
│   │   │   └── attendance/
│   │   ├── context/
│   │   └── hooks/
│   └── lib/
│       ├── api.ts
│       ├── types.ts
│       └── utils.ts
```

### Technology Stack
- **Framework**: Next.js 14+ with App Router
- **Language**: TypeScript for type safety
- **Styling**: Tailwind CSS for consistent styling
- **State Management**: React Context API and custom hooks
- **HTTP Client**: Fetch API with custom wrapper
- **Form Handling**: React Hook Form with validation
- **UI Components**: Custom components built with Tailwind CSS

## Components and Interfaces

### Core UI Components

#### Button Component
```typescript
interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  disabled?: boolean;
  onClick?: () => void;
  children: React.ReactNode;
  className?: string;
}
```

#### Card Component
```typescript
interface CardProps {
  children: React.ReactNode;
  hover?: boolean;
  padding?: string;
  className?: string;
  onClick?: () => void;
}
```

#### Modal Component
```typescript
interface ModalProps {
  show: boolean;
  title: string;
  size?: 'sm' | 'md' | 'lg';
  onClose: () => void;
  children: React.ReactNode;
}
```

#### PageHeader Component
```typescript
interface PageHeaderProps {
  title: string;
  subtitle?: string;
  children?: React.ReactNode; // For action buttons
}
```

#### EmptyState Component
```typescript
interface EmptyStateProps {
  title: string;
  description: string;
  actionText?: string;
  onAction?: () => void;
  icon?: React.ReactNode;
}
```

### Form Components

#### Input Component
```typescript
interface InputProps {
  label?: string;
  type?: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  required?: boolean;
  disabled?: boolean;
}
```

#### Textarea Component
```typescript
interface TextareaProps {
  label?: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  rows?: number;
  required?: boolean;
}
```

#### ImageUpload Component
```typescript
interface ImageUploadProps {
  label?: string;
  value?: string;
  onChange: (file: File | null) => void;
  error?: string;
  accept?: string;
}
```

### Attendance Components

#### StatusBadge Component
```typescript
interface StatusBadgeProps {
  status: AttendanceStatus;
  eventType?: EventType;
}
```

#### AttendanceList Component
```typescript
interface AttendanceListProps {
  attendances: AttendanceRecord[];
  onStatusUpdate: (id: string, status: AttendanceStatus) => void;
  onNotesUpdate: (id: string, notes: string) => void;
}
```

## Data Models

### Post Model
```typescript
interface Post {
  id: string;
  title: string;
  description: string;
  content: string;
  imageUrl?: string;
  createdAt: string;
  updatedAt: string;
  authorId: string;
}
```

### Event Model
```typescript
interface Event {
  id: string;
  title: string;
  description: string;
  date: string;
  location: string;
  imageUrl?: string;
  createdAt: string;
  updatedAt: string;
}
```

### User Model
```typescript
interface User {
  id: string;
  email: string;
  verified: boolean;
  profile?: {
    firstName?: string;
    lastName?: string;
    isAdmin: boolean;
  };
}
```

### Attendance Models
```typescript
enum EventType {
  EVENT = 'EVENT',
  SERVICE = 'SERVICE',
  KIDS_SERVICE = 'KIDS_SERVICE'
}

enum AttendanceStatus {
  CHECKED_IN = 'CHECKED_IN',
  CHECKED_OUT = 'CHECKED_OUT',
  NO_SHOW = 'NO_SHOW',
  EMERGENCY = 'EMERGENCY'
}

interface AttendanceRecord {
  id: string;
  eventType: EventType;
  eventName: string;
  attendeeName: string;
  status: AttendanceStatus;
  timestamp: string;
  notes?: string;
}
```

## Error Handling

### Error Boundary Component
```typescript
interface ErrorBoundaryState {
  hasError: boolean;
  error?: Error;
}
```

### API Error Handling
- Implement consistent error response handling
- Display user-friendly error messages
- Provide retry mechanisms for failed requests
- Log errors for debugging purposes

### Form Validation
- Client-side validation using React Hook Form
- Server-side validation feedback
- Real-time validation feedback
- Clear error message display

## Testing Strategy

### Component Testing
- Unit tests for all UI components using Jest and React Testing Library
- Test component props, state changes, and user interactions
- Mock external dependencies and API calls
- Test accessibility features

### Integration Testing
- Test page-level functionality
- Test API integration
- Test form submissions and data flow
- Test navigation and routing

### End-to-End Testing
- Test complete user workflows
- Test authentication flows
- Test CRUD operations for all entities
- Test responsive design on different screen sizes

## Performance Considerations

### Code Splitting
- Implement dynamic imports for large components
- Split routes using Next.js automatic code splitting
- Lazy load non-critical components

### Image Optimization
- Use Next.js Image component for optimized images
- Implement proper image loading states
- Support multiple image formats

### Caching Strategy
- Implement proper caching for API responses
- Use React Query or SWR for data fetching and caching
- Cache static assets appropriately

### Bundle Optimization
- Tree shake unused code
- Minimize bundle size
- Optimize third-party dependencies

## Accessibility

### WCAG Compliance
- Ensure proper semantic HTML structure
- Implement proper ARIA labels and roles
- Provide keyboard navigation support
- Ensure sufficient color contrast

### Screen Reader Support
- Provide descriptive alt text for images
- Use proper heading hierarchy
- Implement focus management for modals and forms
- Provide status announcements for dynamic content

## Responsive Design

### Breakpoint Strategy
- Mobile-first approach using Tailwind CSS breakpoints
- Responsive grid layouts for card displays
- Adaptive navigation for mobile devices
- Touch-friendly interface elements

### Mobile Considerations
- Implement mobile-specific navigation patterns
- Optimize touch targets for mobile devices
- Ensure proper viewport configuration
- Test on various mobile devices and screen sizes

## Security Considerations

### Authentication
- Implement proper JWT token handling
- Secure token storage using httpOnly cookies
- Automatic token refresh mechanisms
- Proper logout functionality

### Authorization
- Role-based access control
- Route protection for admin-only pages
- API endpoint protection
- Proper error handling for unauthorized access

### Data Validation
- Client-side input validation
- Server-side validation
- XSS prevention
- CSRF protection

## Migration Strategy

### Phase 1: Core Infrastructure
- Set up component library
- Implement authentication system
- Create basic page layouts
- Set up API integration

### Phase 2: Content Management
- Implement posts management
- Implement events management
- Add CRUD operations
- Implement form handling

### Phase 3: User Management
- Implement user listing and filtering
- Add user management actions
- Implement search functionality
- Add user avatar generation

### Phase 4: Attendance System
- Implement attendance overview
- Add event-specific attendance
- Implement filtering and reporting
- Add attendance status management

### Phase 5: Polish and Optimization
- Add animations and transitions
- Implement loading states
- Add error handling
- Performance optimization
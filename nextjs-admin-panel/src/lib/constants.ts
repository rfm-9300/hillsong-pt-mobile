// Application Constants

export const APP_NAME = 'Admin Panel';
export const APP_VERSION = '1.0.0';

// API Configuration
export const API_TIMEOUT = 15000; // 15 seconds
export const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

// Pagination
export const DEFAULT_PAGE_SIZE = 10;
export const MAX_PAGE_SIZE = 100;

// Form Validation
export const MIN_PASSWORD_LENGTH = 8;
export const MAX_TITLE_LENGTH = 100;
export const MAX_DESCRIPTION_LENGTH = 500;
export const MAX_CONTENT_LENGTH = 10000;

// UI Constants
export const ANIMATION_DURATION = 200; // milliseconds
export const DEBOUNCE_DELAY = 300; // milliseconds

// Status Colors
export const STATUS_COLORS = {
  CHECKED_IN: 'bg-green-100 text-green-800',
  CHECKED_OUT: 'bg-blue-100 text-blue-800',
  NO_SHOW: 'bg-red-100 text-red-800',
  EMERGENCY: 'bg-orange-100 text-orange-800',
} as const;

// Button Variants
export const BUTTON_VARIANTS = {
  primary: 'bg-blue-600 hover:bg-blue-700 text-white',
  secondary: 'bg-gray-200 hover:bg-gray-300 text-gray-900',
  danger: 'bg-red-600 hover:bg-red-700 text-white',
  ghost: 'bg-transparent hover:bg-gray-100 text-gray-700',
} as const;

export const BUTTON_SIZES = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2 text-base',
  lg: 'px-6 py-3 text-lg',
} as const;

// Modal Sizes
export const MODAL_SIZES = {
  sm: 'max-w-md',
  md: 'max-w-lg',
  lg: 'max-w-2xl',
} as const;

// Event Types
export const EVENT_TYPE_LABELS = {
  EVENT: 'Event',
  SERVICE: 'Service',
  KIDS_SERVICE: 'Kids Service',
} as const;

// Attendance Status Labels
export const ATTENDANCE_STATUS_LABELS = {
  CHECKED_IN: 'Checked In',
  CHECKED_OUT: 'Checked Out',
  NO_SHOW: 'No Show',
  EMERGENCY: 'Emergency',
} as const;

// Navigation Items
export const NAVIGATION_ITEMS = [
  { href: '/admin/dashboard', label: 'Dashboard', icon: 'dashboard' },
  { href: '/admin/posts', label: 'Posts', icon: 'posts' },
  { href: '/admin/events', label: 'Events', icon: 'events' },
  { href: '/admin/users', label: 'Users', icon: 'users' },
  { href: '/admin/attendance', label: 'Attendance', icon: 'attendance' },
] as const;

// Error Messages
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network error: Unable to connect to the server. Please check your internet connection.',
  TIMEOUT_ERROR: 'The request timed out. The server might be experiencing high load or connectivity issues.',
  UNAUTHORIZED: 'Authentication required: Please log in again.',
  FORBIDDEN: 'Access denied: You do not have permission to perform this action.',
  NOT_FOUND: 'Not found: The requested resource does not exist.',
  SERVER_ERROR: 'Server error: The server encountered an error. Please try again later.',
  VALIDATION_ERROR: 'Please check your input and try again.',
  GENERIC_ERROR: 'An unexpected error occurred. Please try again.',
} as const;

// Success Messages
export const SUCCESS_MESSAGES = {
  POST_CREATED: 'Post created successfully!',
  POST_UPDATED: 'Post updated successfully!',
  POST_DELETED: 'Post deleted successfully!',
  EVENT_CREATED: 'Event created successfully!',
  EVENT_UPDATED: 'Event updated successfully!',
  EVENT_DELETED: 'Event deleted successfully!',
  USER_UPDATED: 'User updated successfully!',
  USER_DELETED: 'User deleted successfully!',
  ATTENDANCE_UPDATED: 'Attendance updated successfully!',
} as const;
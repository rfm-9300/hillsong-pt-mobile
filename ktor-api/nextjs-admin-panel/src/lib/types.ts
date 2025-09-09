// Core Data Models

export interface Post {
  id: string;
  title: string;
  description: string;
  content: string;
  imageUrl?: string;
  createdAt: string;
  updatedAt: string;
  authorId: string;
}

export interface Event {
  id: string;
  title: string;
  description: string;
  date: string;
  location: string;
  imageUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface User {
  id: string;
  email: string;
  verified: boolean;
  profile?: {
    firstName?: string;
    lastName?: string;
    isAdmin: boolean;
  };
}

// Attendance Models
export enum EventType {
  EVENT = 'EVENT',
  SERVICE = 'SERVICE',
  KIDS_SERVICE = 'KIDS_SERVICE'
}

export enum AttendanceStatus {
  CHECKED_IN = 'CHECKED_IN',
  CHECKED_OUT = 'CHECKED_OUT',
  NO_SHOW = 'NO_SHOW',
  EMERGENCY = 'EMERGENCY'
}

export interface AttendanceRecord {
  id: string;
  eventType: EventType;
  eventName: string;
  attendeeName: string;
  status: AttendanceStatus;
  timestamp: string;
  notes?: string;
}

// Component Props Interfaces

export interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  disabled?: boolean;
  onClick?: (e?: React.MouseEvent<HTMLButtonElement>) => void;
  children: React.ReactNode;
  className?: string;
  type?: 'button' | 'submit' | 'reset';
}

export interface CardProps {
  children: React.ReactNode;
  hover?: boolean;
  padding?: string;
  className?: string;
  onClick?: () => void;
}

export interface ModalProps {
  show: boolean;
  title: string;
  size?: 'sm' | 'md' | 'lg';
  onClose: () => void;
  children: React.ReactNode;
}

export interface PageHeaderProps {
  title: string;
  subtitle?: string;
  children?: React.ReactNode; // For action buttons
}

export interface EmptyStateProps {
  title: string;
  description: string;
  actionText?: string;
  onAction?: () => void;
  icon?: React.ReactNode;
}

export interface InputProps {
  label?: string;
  type?: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  required?: boolean;
  disabled?: boolean;
  className?: string;
}

export interface TextareaProps {
  label?: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  rows?: number;
  required?: boolean;
  className?: string;
  disabled?: boolean;
}

export interface CheckboxProps {
  label?: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
  error?: string;
  disabled?: boolean;
  className?: string;
}

export interface ImageUploadProps {
  label?: string;
  value?: string;
  onChange: (file: File | null) => void;
  error?: string;
  accept?: string;
  className?: string;
  disabled?: boolean;
}

export interface FormContainerProps {
  children: React.ReactNode;
  onSubmit?: (e: React.FormEvent) => void;
  className?: string;
}

export interface LoadingOverlayProps {
  show: boolean;
  message?: string;
}

export interface AlertProps {
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  onClose?: () => void;
  className?: string;
}

export interface StatusBadgeProps {
  status: AttendanceStatus;
  eventType?: EventType;
  className?: string;
}

export interface AttendanceListProps {
  attendances: AttendanceRecord[];
  onStatusUpdate: (id: string, status: AttendanceStatus) => void;
  onNotesUpdate: (id: string, notes: string) => void;
  updatingStatus?: boolean;
  updatingNotes?: boolean;
}

export interface StatusUpdateInterfaceProps {
  selectedIds: string[];
  onBulkStatusUpdate: (ids: string[], status: AttendanceStatus) => void;
  onClearSelection: () => void;
  eventType: EventType;
  bulkUpdating?: boolean;
}

// Error Handling
export interface ErrorBoundaryState {
  hasError: boolean;
  error?: Error;
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

// Dashboard Statistics
export interface DashboardStats {
  totalPosts: number;
  totalEvents: number;
  totalUsers: number;
  totalAttendees: number;
  recentActivity: AttendanceRecord[];
}
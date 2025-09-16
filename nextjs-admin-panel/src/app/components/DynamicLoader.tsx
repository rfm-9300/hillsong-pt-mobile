'use client';

import dynamic from 'next/dynamic';
import { LoadingOverlay } from './ui';

// Dynamic imports for heavy components
export const DynamicAttendanceList = dynamic(
  () => import('./attendance/AttendanceList'),
  {
    loading: () => <LoadingOverlay show={true} />,
    ssr: false,
  }
);

export const DynamicStatusUpdateInterface = dynamic(
  () => import('./attendance/StatusUpdateInterface'),
  {
    loading: () => <LoadingOverlay show={true} />,
    ssr: false,
  }
);

export const DynamicPostsList = dynamic(
  () => import('./PostsList'),
  {
    loading: () => <LoadingOverlay show={true} />,
    ssr: false,
  }
);

export const DynamicEventsList = dynamic(
  () => import('./EventsList'),
  {
    loading: () => <LoadingOverlay show={true} />,
    ssr: false,
  }
);

export const DynamicUsersList = dynamic(
  () => import('./UsersList'),
  {
    loading: () => <LoadingOverlay show={true} />,
    ssr: false,
  }
);

// Dynamic imports for form components can be added here when forms are created
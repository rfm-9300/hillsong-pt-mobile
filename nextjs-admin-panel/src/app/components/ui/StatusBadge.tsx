'use client';

import React from 'react';
import { StatusBadgeProps } from '@/lib/types';
import { cn } from '@/lib/utils';
import { STATUS_COLORS, ATTENDANCE_STATUS_LABELS, EVENT_TYPE_LABELS } from '@/lib/constants';

const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  eventType,
  className,
}) => {
  const statusColor = STATUS_COLORS[status];
  const statusLabel = ATTENDANCE_STATUS_LABELS[status];

  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        statusColor,
        className
      )}
    >
      {statusLabel}
      {eventType && (
        <span className="ml-1 opacity-75">
          ({EVENT_TYPE_LABELS[eventType]})
        </span>
      )}
    </span>
  );
};

// User role badge component
export const RoleBadge: React.FC<{ isAdmin: boolean; className?: string }> = ({
  isAdmin,
  className,
}) => {
  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        isAdmin
          ? 'bg-purple-100 text-purple-800'
          : 'bg-gray-100 text-gray-800',
        className
      )}
    >
      {isAdmin ? 'Admin' : 'User'}
    </span>
  );
};

// Verification status badge component
export const VerificationBadge: React.FC<{ verified: boolean; className?: string }> = ({
  verified,
  className,
}) => {
  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        verified
          ? 'bg-green-100 text-green-800'
          : 'bg-yellow-100 text-yellow-800',
        className
      )}
    >
      {verified ? 'Verified' : 'Unverified'}
    </span>
  );
};

export default StatusBadge;
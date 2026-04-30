'use client';

import React from 'react';
import { AttendanceStatus, EventType, StatusBadgeProps } from '@/lib/types';
import { ATTENDANCE_STATUS_LABELS, EVENT_TYPE_LABELS } from '@/lib/constants';
import Badge, { BadgeColor } from './Badge';

const statusColors: Record<AttendanceStatus, BadgeColor> = {
  [AttendanceStatus.CHECKED_IN]: 'green',
  [AttendanceStatus.CHECKED_OUT]: 'neutral',
  [AttendanceStatus.NO_SHOW]: 'red',
  [AttendanceStatus.EMERGENCY]: 'red',
};

const StatusBadge: React.FC<StatusBadgeProps> = ({ status, eventType, className }) => (
  <Badge color={statusColors[status] || 'neutral'} className={className}>
    {ATTENDANCE_STATUS_LABELS[status] || status}
    {eventType && <span className="opacity-75">({EVENT_TYPE_LABELS[eventType as EventType] || eventType})</span>}
  </Badge>
);

export const RoleBadge: React.FC<{ isAdmin: boolean; className?: string }> = ({ isAdmin, className }) => (
  <Badge color={isAdmin ? 'amber' : 'neutral'} className={className}>
    {isAdmin ? 'Admin' : 'User'}
  </Badge>
);

export const VerificationBadge: React.FC<{ verified: boolean; className?: string }> = ({ verified, className }) => (
  <Badge color={verified ? 'green' : 'yellow'} className={className}>
    {verified ? 'Verified' : 'Unverified'}
  </Badge>
);

export default StatusBadge;

'use client';

import React from 'react';
import { AttendanceStatus, EventType } from '@/lib/types';
import { Button } from '@/app/components/ui';
import { cn } from '@/lib/utils';

interface CheckInOutProps {
  currentStatus: AttendanceStatus;
  onStatusChange: (status: AttendanceStatus) => void;
  eventType: EventType;
  className?: string;
  disabled?: boolean;
}

export const CheckInOut: React.FC<CheckInOutProps> = ({
  currentStatus,
  onStatusChange,
  eventType,
  className,
  disabled = false,
}) => {
  const statusOptions = [
    {
      status: AttendanceStatus.CHECKED_IN,
      label: 'Check In',
      variant: 'primary' as const,
      icon: '✓',
    },
    {
      status: AttendanceStatus.CHECKED_OUT,
      label: 'Check Out',
      variant: 'secondary' as const,
      icon: '→',
    },
    {
      status: AttendanceStatus.NO_SHOW,
      label: 'No Show',
      variant: 'danger' as const,
      icon: '✗',
    },
    {
      status: AttendanceStatus.EMERGENCY,
      label: 'Emergency',
      variant: 'danger' as const,
      icon: '⚠',
    },
  ];

  // For kids services, we might want to show different options or labels
  const getStatusLabel = (status: AttendanceStatus) => {
    if (eventType === EventType.KIDS_SERVICE) {
      switch (status) {
        case AttendanceStatus.CHECKED_IN:
          return 'Arrived';
        case AttendanceStatus.CHECKED_OUT:
          return 'Picked Up';
        case AttendanceStatus.NO_SHOW:
          return 'Absent';
        case AttendanceStatus.EMERGENCY:
          return 'Emergency';
        default:
          return 'Unknown';
      }
    }
    
    return statusOptions.find(option => option.status === status)?.label || 'Unknown';
  };

  const handleStatusChange = (status: AttendanceStatus) => {
    if (disabled || status === currentStatus) return;
    onStatusChange(status);
  };

  return (
    <div className={cn('space-y-2', className)}>
      <div className="grid grid-cols-2 gap-2">
        {statusOptions.map((option) => {
          const isActive = currentStatus === option.status;
          const isDisabled = disabled;
          
          return (
            <Button
              key={option.status}
              variant={isActive ? option.variant : 'ghost'}
              size="sm"
              onClick={() => handleStatusChange(option.status)}
              disabled={isDisabled}
              className={cn(
                'flex items-center justify-center space-x-2 transition-all',
                isActive && 'ring-2 ring-offset-1',
                isActive && option.variant === 'primary' && 'ring-blue-300',
                isActive && option.variant === 'secondary' && 'ring-gray-300',
                isActive && option.variant === 'danger' && 'ring-red-300',
                !isActive && 'hover:bg-gray-100'
              )}
            >
              <span className="text-sm">{option.icon}</span>
              <span className="text-sm">
                {eventType === EventType.KIDS_SERVICE 
                  ? getStatusLabel(option.status)
                  : option.label
                }
              </span>
            </Button>
          );
        })}
      </div>
      
      {/* Current Status Display */}
      <div className="text-center">
        <span className="text-xs text-gray-500">
          Current: {getStatusLabel(currentStatus)}
        </span>
      </div>
    </div>
  );
};

export default CheckInOut;
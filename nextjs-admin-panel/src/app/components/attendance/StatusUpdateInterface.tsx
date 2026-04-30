'use client';

import React, { useState } from 'react';
import { AttendanceStatus, EventType } from '@/lib/types';
import { Button, Modal, Card } from '@/app/components/ui';
import { CheckInOut } from './CheckInOut';
import { cn } from '@/lib/utils';

interface StatusUpdateInterfaceProps {
  selectedIds: string[];
  onBulkStatusUpdate: (ids: string[], status: AttendanceStatus) => Promise<void>;
  onClearSelection: () => void;
  eventType?: EventType;
  className?: string;
  bulkUpdating?: boolean;
}

const StatusUpdateInterface: React.FC<StatusUpdateInterfaceProps> = ({
  selectedIds,
  onBulkStatusUpdate,
  onClearSelection,
  eventType = EventType.EVENT,
  className,
  bulkUpdating = false,
}) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState<AttendanceStatus>(AttendanceStatus.CHECKED_IN);


  const handleOpenModal = () => {
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedStatus(AttendanceStatus.CHECKED_IN);
  };

  const handleBulkUpdate = async () => {
    if (selectedIds.length === 0) return;

    try {
      await onBulkStatusUpdate(selectedIds, selectedStatus);
      handleCloseModal();
      onClearSelection();
    } catch (error) {
      console.error('Failed to update attendance status:', error);
    }
  };

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
    
    switch (status) {
      case AttendanceStatus.CHECKED_IN:
        return 'Checked In';
      case AttendanceStatus.CHECKED_OUT:
        return 'Checked Out';
      case AttendanceStatus.NO_SHOW:
        return 'No Show';
      case AttendanceStatus.EMERGENCY:
        return 'Emergency';
      default:
        return 'Unknown';
    }
  };

  if (selectedIds.length === 0) {
    return null;
  }

  return (
    <>
      {/* Floating Action Bar */}
      <div className={cn(
        'fixed bottom-0 left-0 right-0 z-50 border-t border-gray-200 bg-white p-3 pb-safe shadow-lg sm:bottom-6 sm:left-1/2 sm:right-auto sm:-translate-x-1/2 sm:transform sm:rounded-lg sm:border sm:p-4',
        'flex flex-col gap-3 sm:flex-row sm:items-center sm:space-x-4',
        className
      )}>
        <div className="flex items-center space-x-2">
          <span className="text-sm font-medium text-gray-700">
            {selectedIds.length} selected
          </span>
          <div className="h-4 w-px bg-gray-300" />
        </div>

        <div className="flex flex-wrap items-center gap-2 sm:space-x-2">
          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenModal}
          >
            Update Status
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={onClearSelection}
          >
            Clear Selection
          </Button>
        </div>
      </div>

      {/* Bulk Update Modal */}
      <Modal
        show={isModalOpen}
        title="Bulk Status Update"
        size="md"
        onClose={handleCloseModal}
      >
        <div className="space-y-6">
          <div>
            <p className="text-sm text-gray-600 mb-4">
              You are about to update the status for {selectedIds.length} attendance record{selectedIds.length !== 1 ? 's' : ''}.
            </p>
            
            <Card className="bg-gray-50">
              <div className="p-4">
                <h4 className="text-sm font-medium text-gray-700 mb-3">
                  Select New Status
                </h4>
                <CheckInOut
                  currentStatus={selectedStatus}
                  onStatusChange={setSelectedStatus}
                  eventType={eventType}
                  disabled={bulkUpdating}
                />
              </div>
            </Card>
          </div>

          <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <span className="text-yellow-400">⚠</span>
              </div>
              <div className="ml-3">
                <h3 className="text-sm font-medium text-yellow-800">
                  Confirmation Required
                </h3>
                <div className="mt-2 text-sm text-yellow-700">
                  <p>
                    This will change the status of all selected records to &quot;{getStatusLabel(selectedStatus)}&quot;. 
                    This action cannot be undone.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap items-center justify-end gap-3">
            <Button
              variant="ghost"
              onClick={handleCloseModal}
              disabled={bulkUpdating}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleBulkUpdate}
              loading={bulkUpdating}
              disabled={bulkUpdating}
            >
              {bulkUpdating ? 'Updating...' : `Update ${selectedIds.length} Record${selectedIds.length !== 1 ? 's' : ''}`}
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
};

export default StatusUpdateInterface;

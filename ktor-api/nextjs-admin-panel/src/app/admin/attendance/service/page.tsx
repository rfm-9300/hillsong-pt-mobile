'use client';

import React, { useState, useEffect } from 'react';
import { AttendanceRecord, AttendanceStatus, EventType } from '@/lib/types';
import { 
  PageHeader, 
  Card, 
  Button, 
  EmptyState, 
  LoadingOverlay,
  Alert
} from '@/app/components/ui';
import { 
  AttendanceList, 
  StatusUpdateInterface 
} from '@/app/components/attendance';
import { useApiCall } from '@/app/hooks';
import { api } from '@/lib/api';

const ServiceAttendancePage: React.FC = () => {
  const [attendanceRecords, setAttendanceRecords] = useState<AttendanceRecord[]>([]);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [filterStatus, setFilterStatus] = useState<AttendanceStatus | 'all'>('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const { 
    execute: fetchAttendance, 
    loading: loadingAttendance 
  } = useApiCall(api.attendance.getByEventType);

  const { 
    execute: updateAttendanceStatus, 
    loading: updatingStatus 
  } = useApiCall(api.attendance.updateStatus);

  const { 
    execute: updateAttendanceNotes, 
    loading: updatingNotes 
  } = useApiCall(api.attendance.updateNotes);

  const { 
    execute: bulkUpdateStatus, 
    loading: bulkUpdating 
  } = useApiCall(api.attendance.bulkUpdateStatus);

  useEffect(() => {
    loadAttendanceData();
  }, []);

  const loadAttendanceData = async () => {
    try {
      const response = await fetchAttendance(EventType.SERVICE);
      if (response.success) {
        setAttendanceRecords(response.data);
      }
    } catch (error) {
      console.error('Failed to load attendance data:', error);
      setAlert({ type: 'error', message: 'Failed to load attendance data' });
    }
  };

  const handleStatusUpdate = async (id: string, status: AttendanceStatus) => {
    try {
      const response = await updateAttendanceStatus(id, status);
      if (response.success) {
        setAttendanceRecords(prev => 
          prev.map(record => 
            record.id === id ? { ...record, status } : record
          )
        );
        setAlert({ type: 'success', message: 'Attendance status updated successfully' });
      }
    } catch (error) {
      console.error('Failed to update status:', error);
      setAlert({ type: 'error', message: 'Failed to update attendance status' });
    }
  };

  const handleNotesUpdate = async (id: string, notes: string) => {
    try {
      const response = await updateAttendanceNotes(id, notes);
      if (response.success) {
        setAttendanceRecords(prev => 
          prev.map(record => 
            record.id === id ? { ...record, notes } : record
          )
        );
        setAlert({ type: 'success', message: 'Notes updated successfully' });
      }
    } catch (error) {
      console.error('Failed to update notes:', error);
      setAlert({ type: 'error', message: 'Failed to update notes' });
    }
  };

  const handleBulkStatusUpdate = async (ids: string[], status: AttendanceStatus) => {
    try {
      const response = await bulkUpdateStatus(ids, status);
      if (response.success) {
        setAttendanceRecords(prev => 
          prev.map(record => 
            ids.includes(record.id) ? { ...record, status } : record
          )
        );
        setAlert({ type: 'success', message: `Updated ${ids.length} attendance records` });
      }
    } catch (error) {
      console.error('Failed to bulk update status:', error);
      setAlert({ type: 'error', message: 'Failed to update attendance records' });
    }
  };

  const handleClearSelection = () => {
    setSelectedIds([]);
  };

  const filteredRecords = attendanceRecords.filter(record => {
    const matchesStatus = filterStatus === 'all' || record.status === filterStatus;
    const matchesSearch = searchTerm === '' || 
      record.attendeeName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      record.eventName.toLowerCase().includes(searchTerm.toLowerCase());
    
    return matchesStatus && matchesSearch;
  });

  const statusCounts = {
    total: attendanceRecords.length,
    checkedIn: attendanceRecords.filter(r => r.status === AttendanceStatus.CHECKED_IN).length,
    checkedOut: attendanceRecords.filter(r => r.status === AttendanceStatus.CHECKED_OUT).length,
    noShow: attendanceRecords.filter(r => r.status === AttendanceStatus.NO_SHOW).length,
    emergency: attendanceRecords.filter(r => r.status === AttendanceStatus.EMERGENCY).length,
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Service Attendance"
        subtitle="Manage attendance for regular services"
      >
        <Button
          variant="primary"
          onClick={loadAttendanceData}
          loading={loadingAttendance}
        >
          Refresh
        </Button>
      </PageHeader>

      {alert && (
        <Alert
          type={alert.type}
          message={alert.message}
          onClose={() => setAlert(null)}
        />
      )}

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <Card className="p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-gray-900">{statusCounts.total}</div>
            <div className="text-sm text-gray-500">Total Records</div>
          </div>
        </Card>
        <Card className="p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">{statusCounts.checkedIn}</div>
            <div className="text-sm text-gray-500">Checked In</div>
          </div>
        </Card>
        <Card className="p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">{statusCounts.checkedOut}</div>
            <div className="text-sm text-gray-500">Checked Out</div>
          </div>
        </Card>
        <Card className="p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-red-600">{statusCounts.noShow}</div>
            <div className="text-sm text-gray-500">No Show</div>
          </div>
        </Card>
        <Card className="p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-orange-600">{statusCounts.emergency}</div>
            <div className="text-sm text-gray-500">Emergency</div>
          </div>
        </Card>
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1">
            <input
              type="text"
              placeholder="Search by attendee or service name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <div>
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value as AttendanceStatus | 'all')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="all">All Status</option>
              <option value={AttendanceStatus.CHECKED_IN}>Checked In</option>
              <option value={AttendanceStatus.CHECKED_OUT}>Checked Out</option>
              <option value={AttendanceStatus.NO_SHOW}>No Show</option>
              <option value={AttendanceStatus.EMERGENCY}>Emergency</option>
            </select>
          </div>
        </div>
      </Card>

      {/* Attendance List */}
      <div className="relative">
        <LoadingOverlay show={loadingAttendance} />
        
        {filteredRecords.length === 0 ? (
          <EmptyState
            title="No attendance records found"
            description={
              searchTerm || filterStatus !== 'all'
                ? "No records match your current filters"
                : "No service attendance records have been created yet"
            }
            actionText={searchTerm || filterStatus !== 'all' ? "Clear Filters" : undefined}
            onAction={
              searchTerm || filterStatus !== 'all'
                ? () => {
                    setSearchTerm('');
                    setFilterStatus('all');
                  }
                : undefined
            }
          />
        ) : (
          <AttendanceList
            attendances={filteredRecords}
            onStatusUpdate={handleStatusUpdate}
            onNotesUpdate={handleNotesUpdate}
          />
        )}
      </div>

      {/* Bulk Update Interface */}
      <StatusUpdateInterface
        selectedIds={selectedIds}
        onBulkStatusUpdate={handleBulkStatusUpdate}
        onClearSelection={handleClearSelection}
        eventType={EventType.SERVICE}
      />
    </div>
  );
};

export default ServiceAttendancePage;
'use client';

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { AttendanceRecord, AttendanceStatus, EventType } from '@/lib/types';

type ApiResponse<T = unknown> = { success: boolean; data?: T };
import {
  Card,
  Button,
  EmptyState,
  LoadingOverlay,
  Alert,
  NavigationHeader
} from '@/app/components/ui';
import { Input } from '@/app/components/forms';
import { api } from '@/lib/api';
import { cn } from '@/lib/utils';

interface AttendanceFilters {
  startDate: string;
  endDate: string;
  eventType: EventType | 'all';
  status: AttendanceStatus | 'all';
  searchTerm: string;
}

interface AttendanceStats {
  totalRecords: number;
  byStatus: Record<AttendanceStatus, number>;
  byEventType: Record<EventType, number>;
  byDate: Record<string, number>;
}

const AttendanceReportsPage: React.FC = () => {
  const [filters, setFilters] = useState<AttendanceFilters>({
    startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 30 days ago
    endDate: new Date().toISOString().split('T')[0], // today
    eventType: 'all',
    status: 'all',
    searchTerm: '',
  });

  const [attendanceData, setAttendanceData] = useState<AttendanceRecord[]>([]);
  const [stats, setStats] = useState<AttendanceStats | null>(null);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [loadingData, setLoadingData] = useState(false);
  const [exporting, setExporting] = useState(false);
  const hasExecutedRef = useRef(false);

  const loadAttendanceData = useCallback(async () => {
    setLoadingData(true);
    try {
      const eventTypeParam = filters.eventType === 'all' ? undefined : filters.eventType;
      const response = await api.attendance.getByDateRange(
        filters.startDate,
        filters.endDate,
        eventTypeParam
      ) as ApiResponse<AttendanceRecord[]>;

      if (response && response.success && response.data) {
        setAttendanceData(response.data);
        calculateStats(response.data);
      }
    } catch (error) {
      console.error('Failed to load attendance data:', error);
      setAlert({ type: 'error', message: 'Failed to load attendance data' });
    } finally {
      setLoadingData(false);
    }
  }, [filters.startDate, filters.endDate, filters.eventType]);

  // Load data only once on mount
  useEffect(() => {
    if (!hasExecutedRef.current) {
      hasExecutedRef.current = true;
      loadAttendanceData();
    }
  }, [loadAttendanceData]);

  const calculateStats = (data: AttendanceRecord[]) => {
    const stats: AttendanceStats = {
      totalRecords: data.length,
      byStatus: {
        [AttendanceStatus.CHECKED_IN]: 0,
        [AttendanceStatus.CHECKED_OUT]: 0,
        [AttendanceStatus.NO_SHOW]: 0,
        [AttendanceStatus.EMERGENCY]: 0,
      },
      byEventType: {
        [EventType.EVENT]: 0,
        [EventType.SERVICE]: 0,
        [EventType.KIDS_SERVICE]: 0,
      },
      byDate: {},
    };

    data.forEach(record => {
      // Count by status
      stats.byStatus[record.status]++;
      
      // Count by event type
      stats.byEventType[record.eventType]++;
      
      // Count by date
      const date = new Date(record.timestamp).toISOString().split('T')[0];
      stats.byDate[date] = (stats.byDate[date] || 0) + 1;
    });

    setStats(stats);
  };

  const handleFilterChange = (key: keyof AttendanceFilters, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      const exportFilters = {
        startDate: filters.startDate,
        endDate: filters.endDate,
        ...(filters.eventType !== 'all' && { eventType: filters.eventType }),
        ...(filters.status !== 'all' && { status: filters.status }),
        ...(filters.searchTerm && { search: filters.searchTerm }),
      };

      const response = await api.attendance.exportData(exportFilters) as ApiResponse<string>;

      if (response && response.success && response.data) {
        // Create and download the file
        const blob = new Blob([response.data], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `attendance-report-${filters.startDate}-to-${filters.endDate}.csv`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

        setAlert({ type: 'success', message: 'Report exported successfully' });
      }
    } catch (error) {
      console.error('Failed to export data:', error);
      setAlert({ type: 'error', message: 'Failed to export report' });
    } finally {
      setExporting(false);
    }
  };

  const filteredData = attendanceData.filter(record => {
    const matchesStatus = filters.status === 'all' || record.status === filters.status;
    const matchesSearch = !filters.searchTerm || 
      record.attendeeName.toLowerCase().includes(filters.searchTerm.toLowerCase()) ||
      record.eventName.toLowerCase().includes(filters.searchTerm.toLowerCase());
    
    return matchesStatus && matchesSearch;
  });

  const getStatusLabel = (status: AttendanceStatus) => {
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

  const getEventTypeLabel = (eventType: EventType) => {
    switch (eventType) {
      case EventType.EVENT:
        return 'Events';
      case EventType.SERVICE:
        return 'Services';
      case EventType.KIDS_SERVICE:
        return 'Kids Services';
      default:
        return 'Unknown';
    }
  };

  return (
    <div className="space-y-6">
      <NavigationHeader
        title="Attendance Reports"
        subtitle="Generate and export attendance reports with filtering capabilities"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Attendance', href: '/admin/attendance' },
          { label: 'Reports & Analytics', current: true },
        ]}
      >
        <Button
          variant="primary"
          onClick={handleExport}
          loading={exporting}
          disabled={filteredData.length === 0}
        >
          Export Report
        </Button>
      </NavigationHeader>

      {alert && (
        <Alert
          type={alert.type}
          message={alert.message}
          onClose={() => setAlert(null)}
        />
      )}

      {/* Filters */}
      <Card className="p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Report Filters</h3>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div>
            <Input
              label="Start Date"
              type="date"
              value={filters.startDate}
              onChange={(value) => handleFilterChange('startDate', value)}
            />
          </div>
          
          <div>
            <Input
              label="End Date"
              type="date"
              value={filters.endDate}
              onChange={(value) => handleFilterChange('endDate', value)}
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Event Type
            </label>
            <select
              value={filters.eventType}
              onChange={(e) => handleFilterChange('eventType', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="all">All Types</option>
              <option value={EventType.EVENT}>Events</option>
              <option value={EventType.SERVICE}>Services</option>
              <option value={EventType.KIDS_SERVICE}>Kids Services</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Status
            </label>
            <select
              value={filters.status}
              onChange={(e) => handleFilterChange('status', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="all">All Status</option>
              <option value={AttendanceStatus.CHECKED_IN}>Checked In</option>
              <option value={AttendanceStatus.CHECKED_OUT}>Checked Out</option>
              <option value={AttendanceStatus.NO_SHOW}>No Show</option>
              <option value={AttendanceStatus.EMERGENCY}>Emergency</option>
            </select>
          </div>
          
          <div className="md:col-span-2">
            <Input
              label="Search"
              placeholder="Search by attendee or event name..."
              value={filters.searchTerm}
              onChange={(value) => handleFilterChange('searchTerm', value)}
            />
          </div>
        </div>

        <div className="mt-4 flex justify-end">
          <Button
            variant="secondary"
            onClick={loadAttendanceData}
            loading={loadingData}
          >
            Apply Filters
          </Button>
        </div>
      </Card>

      {/* Statistics Overview */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Status Statistics */}
          <Card className="p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">By Status</h3>
            <div className="space-y-3">
              {Object.entries(stats.byStatus).map(([status, count]) => (
                <div key={status} className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">
                    {getStatusLabel(status as AttendanceStatus)}
                  </span>
                  <span className="font-medium">{count}</span>
                </div>
              ))}
            </div>
          </Card>

          {/* Event Type Statistics */}
          <Card className="p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">By Event Type</h3>
            <div className="space-y-3">
              {Object.entries(stats.byEventType).map(([eventType, count]) => (
                <div key={eventType} className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">
                    {getEventTypeLabel(eventType as EventType)}
                  </span>
                  <span className="font-medium">{count}</span>
                </div>
              ))}
            </div>
          </Card>

          {/* Total Records */}
          <Card className="p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Summary</h3>
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-600">Total Records</span>
                <span className="font-medium text-2xl text-blue-600">{stats.totalRecords}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-600">Filtered Results</span>
                <span className="font-medium text-lg">{filteredData.length}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-600">Date Range</span>
                <span className="text-sm font-medium">
                  {Object.keys(stats.byDate).length} days
                </span>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* Data Preview */}
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-medium text-gray-900">Data Preview</h3>
          <span className="text-sm text-gray-500">
            Showing {filteredData.length} of {attendanceData.length} records
          </span>
        </div>

        <div className="relative">
          <LoadingOverlay show={loadingData} />
          
          {filteredData.length === 0 ? (
            <EmptyState
              title="No data found"
              description="No attendance records match your current filters"
              actionText="Clear Filters"
              onAction={() => {
                setFilters({
                  startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
                  endDate: new Date().toISOString().split('T')[0],
                  eventType: 'all',
                  status: 'all',
                  searchTerm: '',
                });
              }}
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Attendee
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Event
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Type
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Date
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredData.slice(0, 50).map((record) => (
                    <tr key={record.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {record.attendeeName}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {record.eventName}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {getEventTypeLabel(record.eventType)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={cn(
                          'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                          record.status === AttendanceStatus.CHECKED_IN && 'bg-green-100 text-green-800',
                          record.status === AttendanceStatus.CHECKED_OUT && 'bg-blue-100 text-blue-800',
                          record.status === AttendanceStatus.NO_SHOW && 'bg-red-100 text-red-800',
                          record.status === AttendanceStatus.EMERGENCY && 'bg-orange-100 text-orange-800'
                        )}>
                          {getStatusLabel(record.status)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {new Date(record.timestamp).toLocaleDateString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              
              {filteredData.length > 50 && (
                <div className="px-6 py-3 bg-gray-50 text-center text-sm text-gray-500">
                  Showing first 50 records. Export to see all {filteredData.length} records.
                </div>
              )}
            </div>
          )}
        </div>
      </Card>
    </div>
  );
};

export default AttendanceReportsPage;
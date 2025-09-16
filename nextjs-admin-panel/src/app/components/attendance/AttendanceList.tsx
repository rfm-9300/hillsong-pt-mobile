'use client';

import React, { useState } from 'react';
import { AttendanceListProps } from '@/lib/types';
import { Card, StatusBadge, Button } from '@/app/components/ui';
import { NotesEditor } from './NotesEditor';
import { CheckInOut } from './CheckInOut';
import { cn } from '@/lib/utils';

const AttendanceList: React.FC<AttendanceListProps> = ({
  attendances,
  onStatusUpdate,
  onNotesUpdate,
}) => {
  const [editingNotes, setEditingNotes] = useState<string | null>(null);
  const [expandedCard, setExpandedCard] = useState<string | null>(null);

  const handleNotesEdit = (id: string) => {
    setEditingNotes(id);
  };

  const handleNotesSave = (id: string, notes: string) => {
    onNotesUpdate(id, notes);
    setEditingNotes(null);
  };

  const handleNotesCancel = () => {
    setEditingNotes(null);
  };

  const toggleCardExpansion = (id: string) => {
    setExpandedCard(expandedCard === id ? null : id);
  };

  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };

  if (attendances.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        <p>No attendance records found.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {attendances.map((attendance) => {
        const isExpanded = expandedCard === attendance.id;
        const isEditingNotes = editingNotes === attendance.id;

        return (
          <Card
            key={attendance.id}
            className={cn(
              'transition-all duration-200',
              isExpanded && 'ring-2 ring-blue-200'
            )}
          >
            <div className="p-4">
              {/* Header Row */}
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center space-x-3">
                  <h3 className="font-medium text-gray-900">
                    {attendance.attendeeName}
                  </h3>
                  <StatusBadge 
                    status={attendance.status} 
                    eventType={attendance.eventType}
                  />
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => toggleCardExpansion(attendance.id)}
                  className="text-gray-500 hover:text-gray-700"
                >
                  {isExpanded ? 'Collapse' : 'Expand'}
                </Button>
              </div>

              {/* Event Info */}
              <div className="flex items-center justify-between text-sm text-gray-600 mb-3">
                <span className="font-medium">{attendance.eventName}</span>
                <span>{formatTimestamp(attendance.timestamp)}</span>
              </div>

              {/* Expanded Content */}
              {isExpanded && (
                <div className="border-t pt-4 space-y-4">
                  {/* Status Update Section */}
                  <div>
                    <h4 className="text-sm font-medium text-gray-700 mb-2">
                      Update Status
                    </h4>
                    <CheckInOut
                      currentStatus={attendance.status}
                      onStatusChange={(status) => onStatusUpdate(attendance.id, status)}
                      eventType={attendance.eventType}
                    />
                  </div>

                  {/* Notes Section */}
                  <div>
                    <div className="flex items-center justify-between mb-2">
                      <h4 className="text-sm font-medium text-gray-700">
                        Notes
                      </h4>
                      {!isEditingNotes && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleNotesEdit(attendance.id)}
                        >
                          {attendance.notes ? 'Edit' : 'Add Note'}
                        </Button>
                      )}
                    </div>
                    
                    {isEditingNotes ? (
                      <NotesEditor
                        initialNotes={attendance.notes || ''}
                        onSave={(notes) => handleNotesSave(attendance.id, notes)}
                        onCancel={handleNotesCancel}
                      />
                    ) : (
                      <div className="text-sm text-gray-600 bg-gray-50 rounded-md p-3 min-h-[60px]">
                        {attendance.notes || 'No notes added'}
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </Card>
        );
      })}
    </div>
  );
};

export default AttendanceList;
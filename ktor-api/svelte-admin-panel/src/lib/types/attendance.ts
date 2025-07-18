// Attendance data models for the admin panel

export enum EventType {
    EVENT = 'EVENT',
    SERVICE = 'SERVICE',
    KIDS_SERVICE = 'KIDS_SERVICE'
}

export enum AttendanceStatus {
    CHECKED_IN = 'CHECKED_IN',
    CHECKED_OUT = 'CHECKED_OUT',
    EMERGENCY = 'EMERGENCY',
    NO_SHOW = 'NO_SHOW'
}

export interface Attendance {
    id: number;
    eventType: EventType;
    eventId: number;
    userId?: number;
    kidId?: number;
    checkedInBy: number;
    checkInTime: string;
    checkOutTime?: string;
    checkedOutBy?: number;
    status: AttendanceStatus;
    notes: string;
    createdAt: string;
}

export interface AttendanceWithDetails {
    attendance: Attendance;
    attendeeName: string;
    eventName: string;
    checkedInByName: string;
    checkedOutByName?: string;
}

export interface AttendanceStats {
    totalAttendees: number;
    currentlyCheckedIn: number;
    checkedOut: number;
    noShows: number;
    emergencies: number;
}

// Request models
export interface CheckInRequest {
    userId?: number;
    kidId?: number;
    checkedInBy: number;
    notes?: string;
}

export interface CheckOutRequest {
    attendanceId: number;
    checkedOutBy: number;
    notes?: string;
}

export interface UpdateAttendanceStatusRequest {
    attendanceId: number;
    status: AttendanceStatus;
    notes?: string;
}

export interface UpdateAttendanceNotesRequest {
    attendanceId: number;
    notes: string;
}

// Response models
export interface AttendanceResponse {
    attendance: Attendance;
}

export interface AttendanceListResponse {
    attendances: AttendanceWithDetails[];
}

export interface AttendanceStatsResponse {
    stats: AttendanceStats;
}
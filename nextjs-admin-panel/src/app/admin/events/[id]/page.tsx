'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Image from 'next/image';
import { NavigationHeader, Alert, StatCard, Card, Button, DeleteConfirmationModal } from '@/app/components/ui';
import Badge from '@/app/components/ui/Badge';
import LoadingSkeleton from '@/app/components/ui/LoadingSkeleton';
import { api, ENDPOINTS } from '@/lib/api';
import { Event, EventAttendeeListResponse, EventAttendeeRow } from '@/lib/types';
import { getImageUrl } from '@/lib/utils';
import { EditIcon, TrashIcon, EventsIcon, UsersIcon } from '@/app/components/icons/Icons';

type Tab = 'attendees' | 'waitlist' | 'checkin';

export default function EventDetailPage() {
  const router = useRouter();
  const params = useParams();
  const eventId = params.id as string;

  const [event, setEvent] = useState<Event | null>(null);
  const [attendeeData, setAttendeeData] = useState<EventAttendeeListResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [activeTab, setActiveTab] = useState<Tab>('attendees');
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  // Check-in panel state
  const [tokenInput, setTokenInput] = useState('');
  const [checkingIn, setCheckingIn] = useState(false);

  // Attendee action state
  const [removingUserId, setRemovingUserId] = useState<string | null>(null);
  const [approvingUserId, setApprovingUserId] = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    try {
      const [eventRes, attendeesRes] = await Promise.all([
        api.get<{ data: Event }>(ENDPOINTS.EVENT_BY_ID(eventId)),
        api.get<{ data: EventAttendeeListResponse }>(ENDPOINTS.EVENT_ATTENDEES(eventId))
      ]);
      if (eventRes?.data) setEvent(eventRes.data);
      if (attendeesRes?.data) setAttendeeData(attendeesRes.data);
    } catch (error) {
      setAlert({ type: 'error', message: error instanceof Error ? error.message : 'Failed to load event' });
    } finally {
      setLoading(false);
    }
  }, [eventId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await api.delete(ENDPOINTS.EVENT_DELETE(eventId));
      router.push('/admin/events');
    } catch (error) {
      setAlert({ type: 'error', message: error instanceof Error ? error.message : 'Failed to delete event' });
      setShowDeleteModal(false);
    } finally {
      setDeleting(false);
    }
  };

  const handleCheckIn = async (userId?: string) => {
    const token = userId ? `__userId:${userId}` : tokenInput.trim();
    if (!token) return;

    setCheckingIn(true);
    setAlert(null);
    try {
      await api.post(ENDPOINTS.ATTENDANCE_CHECK_IN_BY_TOKEN, {
        qrToken: token,
        attendanceType: 'EVENT',
        eventId
      });
      setAlert({ type: 'success', message: 'User checked in successfully' });
      setTokenInput('');
      await fetchData();
    } catch (error) {
      setAlert({ type: 'error', message: error instanceof Error ? error.message : 'Check-in failed' });
    } finally {
      setCheckingIn(false);
    }
  };

  const handleCheckInByUserId = async (userId: string) => {
    setCheckingIn(true);
    setAlert(null);
    try {
      await api.post(ENDPOINTS.ATTENDANCE_BULK_CHECK_IN, {
        userIds: [userId],
        attendanceType: 'EVENT',
        eventId
      });
      setAlert({ type: 'success', message: 'User checked in successfully' });
      await fetchData();
    } catch (error) {
      setAlert({ type: 'error', message: error instanceof Error ? error.message : 'Check-in failed' });
    } finally {
      setCheckingIn(false);
    }
  };

  const handleRemove = async (userId: string) => {
    setRemovingUserId(userId);
    try {
      await api.delete(ENDPOINTS.EVENT_REMOVE_ATTENDEE(eventId, userId));
      setAlert({ type: 'success', message: 'Attendee removed' });
      await fetchData();
    } catch (error) {
      setAlert({ type: 'error', message: error instanceof Error ? error.message : 'Failed to remove attendee' });
    } finally {
      setRemovingUserId(null);
    }
  };

  const handleApprove = async (userId: string) => {
    setApprovingUserId(userId);
    try {
      await api.post(ENDPOINTS.EVENT_APPROVE(eventId, userId), {});
      setAlert({ type: 'success', message: 'User approved and added to event' });
      await fetchData();
    } catch (error) {
      setAlert({ type: 'error', message: error instanceof Error ? error.message : 'Failed to approve user' });
    } finally {
      setApprovingUserId(null);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <LoadingSkeleton height={32} width="40%" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[...Array(4)].map((_, i) => <LoadingSkeleton key={i} height={80} />)}
        </div>
        <LoadingSkeleton height={300} />
      </div>
    );
  }

  if (!event) return null;

  const eventDate = new Date(event.date);
  const imageUrl = event.headerImagePath ? getImageUrl(event.headerImagePath) : null;
  const waitlistSize = attendeeData?.waitingList.length ?? 0;

  return (
    <div className="space-y-6">
      <NavigationHeader
        title={event.title}
        showBackButton
        backButtonText="Back to Events"
        backButtonHref="/admin/events"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Events', href: '/admin/events' },
          { label: event.title, current: true },
        ]}
      >
        <Button size="sm" variant="outline" icon={<EditIcon />} onClick={() => router.push(`/admin/events/${eventId}/edit`)}>Edit</Button>
        <Button size="sm" variant="danger" icon={<TrashIcon />} onClick={() => setShowDeleteModal(true)}>Delete</Button>
      </NavigationHeader>

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      {/* Event hero */}
      <Card className="overflow-hidden p-0">
        <div className="relative h-[160px] bg-[linear-gradient(135deg,#1e3a5f,#2563EB)] sm:h-[200px]">
          {imageUrl && (
            <Image src={imageUrl} alt={event.title} fill className="object-cover opacity-60" sizes="100vw" />
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
          <div className="absolute bottom-3 left-3 right-3 text-white sm:bottom-4 sm:left-4 sm:right-auto">
            <p className="flex items-center gap-1.5 whitespace-normal text-[12px] opacity-80 sm:text-sm">
              <EventsIcon size={14} />
              {eventDate.toLocaleDateString('pt-PT', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
              {' · '}
              {eventDate.toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit' })}
            </p>
            <p className="mt-0.5 text-[12px] opacity-70 sm:text-sm">{event.location}</p>
            <p className="text-xs opacity-60 mt-0.5">Organised by {event.organizerName}</p>
          </div>
          <div className="absolute right-3 top-3 flex flex-col items-end gap-1 sm:right-4 sm:top-4 sm:flex-row sm:gap-2">
            {event.needsApproval && <Badge color="yellow">Approval required</Badge>}
            {event.isAtCapacity && <Badge color="red">Full</Badge>}
          </div>
        </div>
        <div className="p-4">
          <p className="text-sm text-[var(--color-text-sub)] leading-relaxed">{event.description}</p>
        </div>
      </Card>

      {/* Stats row */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard title="Registered" value={`${event.attendeeCount}/${event.maxAttendees}`} color="blue" icon={<UsersIcon size={16} />} />
        <StatCard title="Checked in" value={attendeeData?.checkedInCount ?? 0} color="green" icon={<UsersIcon size={16} />} />
        <StatCard title="Waitlist" value={waitlistSize} color="amber" icon={<UsersIcon size={16} />} />
        <StatCard title="Available" value={event.availableSpots} color={event.availableSpots === 0 ? 'red' : 'green'} icon={<UsersIcon size={16} />} />
      </div>

      {/* Tabs */}
      <div>
        <div className="-mx-4 overflow-x-auto px-4 sm:mx-0 sm:px-0">
        <div className="flex min-w-max gap-0 border-b border-[var(--color-border)]">
          {(['attendees', 'waitlist', 'checkin'] as Tab[]).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`-mb-px cursor-pointer whitespace-nowrap border-b-2 px-4 py-2.5 text-[13px] font-medium transition-colors ${
                activeTab === tab
                  ? 'border-[var(--color-accent)] text-[var(--color-accent)]'
                  : 'border-transparent text-[var(--color-text-sub)] hover:text-[var(--color-text)]'
              }`}
            >
              {tab === 'attendees' && `Attendees (${event.attendeeCount})`}
              {tab === 'waitlist' && `Waitlist (${waitlistSize})`}
              {tab === 'checkin' && 'Check-in'}
            </button>
          ))}
        </div>
        </div>

        <div className="pt-4">
          {activeTab === 'attendees' && (
            <AttendeeTable
              rows={attendeeData?.attendees ?? []}
              onCheckIn={handleCheckInByUserId}
              onRemove={handleRemove}
              removingUserId={removingUserId}
              checkingIn={checkingIn}
              emptyMessage="No registered attendees yet"
              showCheckIn
            />
          )}

          {activeTab === 'waitlist' && (
            <AttendeeTable
              rows={attendeeData?.waitingList ?? []}
              onApprove={handleApprove}
              onRemove={handleRemove}
              approvingUserId={approvingUserId}
              removingUserId={removingUserId}
              emptyMessage="Waitlist is empty"
              showApprove
            />
          )}

          {activeTab === 'checkin' && (
            <Card className="max-w-md p-4 sm:p-6">
              <h3 className="text-[14px] font-semibold text-[var(--color-text)] mb-4">Manual Token Check-in</h3>
              <p className="text-[12px] text-[var(--color-text-sub)] mb-4">
                Enter a user&apos;s QR token to check them in. The token is displayed in their mobile app profile.
              </p>
              <div className="flex flex-col gap-2 sm:flex-row">
                <input
                  type="text"
                  value={tokenInput}
                  onChange={e => setTokenInput(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleCheckIn()}
                  placeholder="Paste QR token here…"
                  className="min-h-[40px] flex-1 rounded-lg border border-[var(--color-border)] px-3 py-2 text-[16px] focus:border-[var(--color-accent)] focus:outline-none sm:text-[13px]"
                />
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => handleCheckIn()}
                  loading={checkingIn}
                  disabled={!tokenInput.trim() || checkingIn}
                >
                  Check in
                </Button>
              </div>
            </Card>
          )}
        </div>
      </div>

      <DeleteConfirmationModal
        show={showDeleteModal}
        title="Delete Event"
        message={`Are you sure you want to delete "${event.title}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteModal(false)}
        loading={deleting}
      />
    </div>
  );
}

interface AttendeeTableProps {
  rows: EventAttendeeRow[];
  onCheckIn?: (userId: string) => void;
  onApprove?: (userId: string) => void;
  onRemove?: (userId: string) => void;
  removingUserId?: string | null;
  approvingUserId?: string | null;
  checkingIn?: boolean;
  emptyMessage: string;
  showCheckIn?: boolean;
  showApprove?: boolean;
}

function AttendeeTable({
  rows,
  onCheckIn,
  onApprove,
  onRemove,
  removingUserId,
  approvingUserId,
  checkingIn,
  emptyMessage,
  showCheckIn,
  showApprove,
}: AttendeeTableProps) {
  if (rows.length === 0) {
    return (
      <div className="flex items-center justify-center py-12 text-[13px] text-[var(--color-text-sub)]">
        {emptyMessage}
      </div>
    );
  }

  return (
    <>
    <div className="space-y-2 md:hidden">
      {rows.map(row => (
        <div key={row.userId} className="rounded-[10px] border border-[var(--color-border)] bg-[var(--color-surface)] p-3">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[var(--color-accent-sub)] text-[11px] font-semibold text-[var(--color-accent)]">
              {row.fullName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()}
            </div>
            <div className="min-w-0">
              <p className="truncate font-medium text-[var(--color-text)]">{row.fullName}</p>
              <p className="truncate text-[12px] text-[var(--color-text-sub)]">{row.email}</p>
            </div>
          </div>
          <div className="mt-3">
            {row.isCheckedIn ? (
              <Badge color="green">
                Checked in{row.checkInTime ? ` · ${new Date(row.checkInTime).toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit' })}` : ''}
              </Badge>
            ) : (
              <Badge color="neutral">Registered</Badge>
            )}
          </div>
          <div className="mt-3 flex flex-wrap gap-2 border-t border-[var(--color-border)] pt-3">
            {showCheckIn && !row.isCheckedIn && onCheckIn && (
              <Button size="xs" variant="outline" onClick={() => onCheckIn(row.userId)} loading={checkingIn}>
                Check in
              </Button>
            )}
            {showApprove && onApprove && (
              <Button size="xs" variant="primary" onClick={() => onApprove(row.userId)} loading={approvingUserId === row.userId}>
                Approve
              </Button>
            )}
            {onRemove && (
              <Button size="xs" variant="danger" onClick={() => onRemove(row.userId)} loading={removingUserId === row.userId}>
                Remove
              </Button>
            )}
          </div>
        </div>
      ))}
    </div>
    <div className="hidden overflow-x-auto rounded-lg border border-[var(--color-border)] md:block">
      <table className="w-full text-[13px]">
        <thead>
          <tr className="border-b border-[var(--color-border)] bg-[var(--color-surface-alt)]">
            <th className="px-4 py-3 text-left font-medium text-[var(--color-text-sub)]">Name</th>
            <th className="px-4 py-3 text-left font-medium text-[var(--color-text-sub)]">Email</th>
            <th className="px-4 py-3 text-left font-medium text-[var(--color-text-sub)]">Status</th>
            <th className="px-4 py-3 text-right font-medium text-[var(--color-text-sub)]">Actions</th>
          </tr>
        </thead>
        <tbody>
          {rows.map(row => (
            <tr key={row.userId} className="border-b border-[var(--color-border)] last:border-0">
              <td className="px-4 py-3">
                <div className="flex items-center gap-2.5">
                  <div className="h-8 w-8 shrink-0 rounded-full bg-[var(--color-accent-sub)] flex items-center justify-center text-[11px] font-semibold text-[var(--color-accent)]">
                    {row.fullName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()}
                  </div>
                  <span className="font-medium text-[var(--color-text)]">{row.fullName}</span>
                </div>
              </td>
              <td className="px-4 py-3 text-[var(--color-text-sub)]">{row.email}</td>
              <td className="px-4 py-3">
                {row.isCheckedIn ? (
                  <Badge color="green">
                    Checked in{row.checkInTime ? ` · ${new Date(row.checkInTime).toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit' })}` : ''}
                  </Badge>
                ) : (
                  <Badge color="neutral">Registered</Badge>
                )}
              </td>
              <td className="px-4 py-3">
                <div className="flex justify-end gap-2">
                  {showCheckIn && !row.isCheckedIn && onCheckIn && (
                    <Button
                      size="xs"
                      variant="outline"
                      onClick={() => onCheckIn(row.userId)}
                      loading={checkingIn}
                    >
                      Check in
                    </Button>
                  )}
                  {showApprove && onApprove && (
                    <Button
                      size="xs"
                      variant="primary"
                      onClick={() => onApprove(row.userId)}
                      loading={approvingUserId === row.userId}
                    >
                      Approve
                    </Button>
                  )}
                  {onRemove && (
                    <Button
                      size="xs"
                      variant="danger"
                      onClick={() => onRemove(row.userId)}
                      loading={removingUserId === row.userId}
                    >
                      Remove
                    </Button>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
    </>
  );
}

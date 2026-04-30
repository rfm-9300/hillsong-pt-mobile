'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { Badge, Button, Card, DeleteConfirmationModal } from './ui';
import { Event } from '@/lib/types';
import { getImageUrl } from '@/lib/utils';
import { EditIcon, EventsIcon, TrashIcon, ChevronRIcon } from './icons/Icons';
import { api, ENDPOINTS } from '@/lib/api';

interface EventCardProps {
  event: Event;
  onDelete?: (eventId: string) => void;
}

const gradients = [
  'bg-[linear-gradient(135deg,#1e3a5f,#2563EB)]',
  'bg-[linear-gradient(135deg,#14532d,#16A34A)]',
  'bg-[linear-gradient(135deg,#4c1d95,#7c3aed)]',
  'bg-[linear-gradient(135deg,#7c2d12,#D97706)]',
];

export default function EventCard({ event, onDelete }: EventCardProps) {
  const router = useRouter();
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const eventDate = new Date(event.date);
  const isUpcoming = eventDate > new Date();
  const percent = event.maxAttendees ? Math.min(100, Math.round((event.attendeeCount / event.maxAttendees) * 100)) : 0;
  const imagePath = event.headerImagePath || event.imageUrl;
  const imageUrl = imagePath ? getImageUrl(imagePath) : '';
  const gradient = gradients[Number(event.id) % gradients.length];

  const handleView = () => router.push(`/admin/events/${event.id}`);
  const handleEdit = () => router.push(`/admin/events/${event.id}/edit`);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await api.delete(ENDPOINTS.EVENT_DELETE(event.id.toString()));
      onDelete?.(event.id.toString());
      setShowDeleteModal(false);
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <Card className="overflow-hidden">
        <div className={`relative h-[120px] ${gradient}`}>
          {imageUrl && (
            <Image src={imageUrl} alt={event.title} fill className="object-cover opacity-60" sizes="(max-width: 1024px) 100vw, 50vw" />
          )}
          <div className="absolute left-3 top-3">
            <Badge color={isUpcoming ? 'green' : 'neutral'}>{isUpcoming ? 'Upcoming' : 'Past'}</Badge>
          </div>
          {event.needsApproval && (
            <div className="absolute right-3 top-3">
              <Badge color="yellow">Approval required</Badge>
            </div>
          )}
          <div className="absolute bottom-3 right-3 min-w-12 rounded-[8px] bg-white px-2 py-1 text-center shadow-sm">
            <div className="text-[10px] font-semibold uppercase text-[var(--color-text-sub)]">{eventDate.toLocaleDateString('en-GB', { month: 'short' })}</div>
            <div className="font-display text-[18px] leading-none text-[var(--color-text)]">{eventDate.getDate()}</div>
          </div>
        </div>

        <div className="p-[14px_16px]">
          <h3 className="mb-1 line-clamp-1 text-[14px] font-bold text-[var(--color-text)]">{event.title}</h3>
          <div className="mb-3 flex items-center gap-1.5 text-[12px] text-[var(--color-text-sub)]">
            <EventsIcon size={14} />
            <span className="truncate">{formatEventDate(eventDate)} · {event.location}</span>
          </div>
          <div className="mb-3">
            <div className="mb-1 flex items-center justify-between text-[11px] text-[var(--color-text-sub)]">
              <span>Attendance</span>
              <span>{event.attendeeCount || 0}/{event.maxAttendees || 0}</span>
            </div>
            <div className="h-1 rounded-full bg-[var(--color-surface-alt)]">
              <div className={`h-1 rounded-full ${percent > 80 ? 'bg-[var(--color-danger)]' : 'bg-[var(--color-accent)]'}`} style={{ width: `${percent}%` }} />
            </div>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button size="xs" variant="ghost" icon={<ChevronRIcon size={12} />} onClick={handleView}>View</Button>
            <Button size="xs" variant="outline" icon={<EditIcon />} onClick={handleEdit}>Edit</Button>
            <Button size="xs" variant="danger" icon={<TrashIcon />} onClick={() => setShowDeleteModal(true)}>Delete</Button>
          </div>
        </div>
      </Card>

      <DeleteConfirmationModal
        show={showDeleteModal}
        title="Delete Event"
        message={`Are you sure you want to delete "${event.title}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteModal(false)}
        loading={deleting}
      />
    </>
  );
}

function formatEventDate(date: Date) {
  return date.toLocaleDateString('pt-PT', { weekday: 'short', day: 'numeric', month: 'short' });
}

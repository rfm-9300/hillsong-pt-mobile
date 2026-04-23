'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Badge, Button, Card, DeleteConfirmationModal } from './ui';
import { api, ENDPOINTS } from '@/lib/api';
import { formatGroupDay, formatGroupFrequency, formatMinistry } from '@/lib/groups';
import { GroupSummary } from '@/lib/types';
import { truncateText } from '@/lib/utils';
import { EditIcon, TrashIcon } from './icons/Icons';

interface GroupCardProps {
  group: GroupSummary;
  onDelete?: (groupId: string) => void;
}

export default function GroupCard({ group, onDelete }: GroupCardProps) {
  const router = useRouter();
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const handleEdit = () => router.push(`/admin/groups/${group.id}`);
  const atCapacity = Boolean(group.maxMembers && group.currentMembers >= group.maxMembers);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await api.delete(ENDPOINTS.ADMIN_GROUP_DELETE(group.id));
      onDelete?.(group.id);
      setShowDeleteModal(false);
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <Card className="p-[16px_18px]">
        <div className="grid grid-cols-[1fr_200px_180px_120px_100px] items-center gap-4">
          <div className="min-w-0">
            <div className="mb-1 flex flex-wrap items-center gap-2">
              <button type="button" onClick={handleEdit} className="truncate text-left text-[14px] font-bold text-[var(--color-text)] hover:text-[var(--color-accent)]">
                {group.name}
              </button>
              <Badge color={group.isActive ? 'green' : 'neutral'} size="xs">{group.isActive ? 'Active' : 'Hidden'}</Badge>
              <Badge color="blue" size="xs">{formatMinistry(group.ministry)}</Badge>
            </div>
            <p className="line-clamp-2 text-[12px] leading-[1.45] text-[var(--color-text-sub)]">{truncateText(group.description, 140)}</p>
          </div>

          <InfoBlock label="Leader">
            <div className="truncate text-[13px] font-medium text-[var(--color-text)]">{group.leaderName}</div>
            <div className="truncate text-[11px] text-[var(--color-text-sub)]">{group.city}</div>
          </InfoBlock>

          <InfoBlock label="Schedule">
            <div className="truncate text-[13px] font-medium text-[var(--color-text)]">{formatGroupDay(group.meetingDay)} · {group.meetingTime}</div>
            <div className="truncate text-[11px] text-[var(--color-text-sub)]">{formatGroupFrequency(group.frequency)}</div>
          </InfoBlock>

          <InfoBlock label="Members">
            <div>
              <span className={`font-display text-[20px] leading-none ${atCapacity ? 'text-[var(--color-danger)]' : 'text-[var(--color-text)]'}`}>{group.currentMembers}</span>
              {group.maxMembers && <span className="ml-1 text-[11px] text-[var(--color-text-muted)]">/ {group.maxMembers}</span>}
            </div>
          </InfoBlock>

          <div className="flex flex-col gap-1.5">
            <Button size="xs" variant="ghost" icon={<EditIcon />} onClick={handleEdit}>Edit</Button>
            <Button size="xs" variant="danger" icon={<TrashIcon />} onClick={() => setShowDeleteModal(true)}>Delete</Button>
          </div>
        </div>
      </Card>

      <DeleteConfirmationModal
        show={showDeleteModal}
        title="Delete Group"
        message={`Are you sure you want to hide "${group.name}" from the mobile app?`}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteModal(false)}
        loading={deleting}
      />
    </>
  );
}

function InfoBlock({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="min-w-0">
      <div className="mb-1 text-[11px] font-semibold uppercase tracking-[0.5px] text-[var(--color-text-muted)]">{label}</div>
      {children}
    </div>
  );
}

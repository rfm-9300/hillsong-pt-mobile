'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Badge, Button, Card, DeleteConfirmationModal } from './ui';
import { api, ENDPOINTS } from '@/lib/api';
import { formatMinistryType } from '@/lib/ministries';
import { MinistrySummary } from '@/lib/types';
import { truncateText } from '@/lib/utils';
import { EditIcon, TrashIcon } from './icons/Icons';

interface MinistryCardProps {
  ministry: MinistrySummary;
  onDelete?: (ministryId: string) => void;
}

export default function MinistryCard({ ministry, onDelete }: MinistryCardProps) {
  const router = useRouter();
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const handleEdit = () => router.push(`/admin/ministries/${ministry.id}`);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await api.delete(ENDPOINTS.ADMIN_MINISTRY_DELETE(ministry.id));
      onDelete?.(ministry.id);
      setShowDeleteModal(false);
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <Card className="p-4 sm:p-[16px_18px]">
        <div className="grid grid-cols-1 items-start gap-4 md:grid-cols-[1fr_180px_140px_120px_100px] md:items-center">
          <div className="min-w-0">
            <div className="mb-1 flex flex-wrap items-center gap-2">
              <button type="button" onClick={handleEdit} className="cursor-pointer truncate text-left text-[14px] font-bold text-[var(--color-text)] hover:text-[var(--color-accent)]">
                {ministry.name}
              </button>
              <Badge color={ministry.isActive ? 'green' : 'neutral'} size="xs">{ministry.isActive ? 'Active' : 'Hidden'}</Badge>
              <Badge color="blue" size="xs">{formatMinistryType(ministry.type)}</Badge>
              {ministry.isJoinable && <Badge color="amber" size="xs">Joinable</Badge>}
            </div>
            <p className="line-clamp-2 text-[12px] leading-[1.45] text-[var(--color-text-sub)]">{truncateText(ministry.description, 140)}</p>
          </div>

          <InfoBlock label="Location">
            <div className="truncate text-[13px] font-medium text-[var(--color-text)]">{ministry.city ?? '—'}</div>
          </InfoBlock>

          <InfoBlock label="Leaders">
            <div>
              <span className="font-display text-[20px] leading-none text-[var(--color-text)]">{ministry.leaderCount}</span>
            </div>
          </InfoBlock>

          <InfoBlock label="Members">
            <div>
              <span className="font-display text-[20px] leading-none text-[var(--color-text)]">{ministry.memberCount}</span>
            </div>
          </InfoBlock>

          <div className="flex flex-wrap gap-1.5 md:flex-col">
            <Button size="xs" variant="ghost" icon={<EditIcon />} onClick={handleEdit}>Edit</Button>
            <Button size="xs" variant="danger" icon={<TrashIcon />} onClick={() => setShowDeleteModal(true)}>Delete</Button>
          </div>
        </div>
      </Card>

      <DeleteConfirmationModal
        show={showDeleteModal}
        title="Delete Ministry"
        message={`Are you sure you want to deactivate "${ministry.name}"? Members and history will be preserved.`}
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

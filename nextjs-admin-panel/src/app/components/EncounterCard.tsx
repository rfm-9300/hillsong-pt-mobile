'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Badge, Button, Card, DeleteConfirmationModal } from './ui';
import { Encounter } from '@/lib/types';
import { getImageUrl } from '@/lib/utils';
import { api, ENDPOINTS } from '@/lib/api';
import { EditIcon, ImageIcon, TrashIcon } from './icons/Icons';

interface EncounterCardProps {
  encounter: Encounter;
  onDelete?: (encounterId: number) => void;
}

export default function EncounterCard({ encounter, onDelete }: EncounterCardProps) {
  const router = useRouter();
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const handleEdit = () => router.push(`/admin/encounters/${encounter.id}`);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await api.delete(ENDPOINTS.ENCOUNTER_DELETE(encounter.id.toString()));
      onDelete?.(encounter.id);
      setShowDeleteModal(false);
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <Card className="overflow-hidden">
        <div className="flex min-h-[120px] flex-col sm:flex-row">
          <div className="flex h-[140px] w-full shrink-0 items-center justify-center bg-[var(--color-surface-alt)] text-[var(--color-text-muted)] sm:h-auto sm:w-[120px]">
            {encounter.imagePath ? (
              <img src={getImageUrl(encounter.imagePath)} alt={encounter.title} className="h-full w-full object-cover" />
            ) : (
              <ImageIcon />
            )}
          </div>
          <div className="flex flex-1 flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between sm:p-[16px_18px]">
            <div className="min-w-0">
              <h3 className="mb-1 truncate text-[14px] font-bold text-[var(--color-text)]">{encounter.title}</h3>
              <p className="mb-2 line-clamp-2 max-w-[620px] text-[12px] leading-[1.45] text-[var(--color-text-sub)]">{encounter.description}</p>
              <div className="flex flex-wrap gap-1.5">
                <Badge color="neutral">{formatDate(encounter.date)}</Badge>
                {encounter.location && <Badge color="neutral">{encounter.location}</Badge>}
              </div>
            </div>
            <div className="flex shrink-0 flex-wrap items-center gap-1.5">
              <Button size="xs" variant="ghost" icon={<EditIcon />} onClick={handleEdit}>Edit</Button>
              <Button size="xs" variant="danger" icon={<TrashIcon />} onClick={() => setShowDeleteModal(true)}>Delete</Button>
            </div>
          </div>
        </div>
      </Card>

      <DeleteConfirmationModal
        show={showDeleteModal}
        title="Delete Encounter"
        message={`Are you sure you want to delete "${encounter.title}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteModal(false)}
        loading={deleting}
      />
    </>
  );
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

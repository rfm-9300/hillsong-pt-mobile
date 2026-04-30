'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { YouTubeVideo } from '@/lib/types';
import { api } from '@/lib/api';
import { Alert, Badge, Button, Card, DeleteConfirmationModal, EmptyState, PageHeader, TableHeader, TableRow } from '@/app/components/ui';
import { EditIcon, EyeIcon, EyeOffIcon, ImageIcon, PlusIcon, TrashIcon } from '@/app/components/icons/Icons';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

const cols = [
  { label: 'Thumbnail', width: '100px' },
  { label: 'Title', width: '1fr' },
  { label: 'Status', width: '100px' },
  { label: 'Order', width: '80px' },
  { label: 'Added', width: '120px' },
  { label: '', width: '150px' },
];

export default function VideosPage() {
  const router = useRouter();
  const [videos, setVideos] = useState<YouTubeVideo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [videoToDelete, setVideoToDelete] = useState<YouTubeVideo | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [toggling, setToggling] = useState<string | null>(null);

  const fetchVideos = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await api.youtubeVideos.getAll() as ApiResponse<YouTubeVideo[]>;
      setVideos(res?.data ?? []);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load videos');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchVideos(); }, []);

  const handleToggleActive = async (video: YouTubeVideo) => {
    try {
      setToggling(video.id);
      await api.youtubeVideos.toggleActive(video.id);
      await fetchVideos();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to toggle status');
    } finally {
      setToggling(null);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!videoToDelete) return;
    try {
      setDeleting(true);
      await api.youtubeVideos.delete(videoToDelete.id);
      await fetchVideos();
      setVideoToDelete(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to delete video');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Videos"
        subtitle="Manage YouTube videos shown in Latest Messages"
        breadcrumbs={['Admin', 'Videos']}
        actions={<Button size="sm" icon={<PlusIcon />} onClick={() => router.push('/admin/videos/create')}>Add Video</Button>}
      />

      {error && <Alert type="error" message={error} onClose={() => setError(null)} />}

      {loading ? (
        <Card>
          {[1, 2, 3].map((i) => <div key={i} className="mx-3 my-3 h-12 rounded skeleton-shimmer" />)}
        </Card>
      ) : videos.length === 0 ? (
        <Card>
          <EmptyState title="No videos yet" description="Add YouTube videos to display in the mobile app." actionText="Add First Video" onAction={() => router.push('/admin/videos/create')} icon={<ImageIcon />} />
        </Card>
      ) : (
        <Card className="overflow-hidden">
          <TableHeader cols={cols} />
          {videos.sort((a, b) => a.displayOrder - b.displayOrder).map((video, index) => {
            const thumb = getThumbnail(video);
            return (
              <TableRow
                key={video.id}
                cols={cols}
                last={index === videos.length - 1}
                cells={[
                  <div key="thumb" className="h-[42px] w-[72px] overflow-hidden rounded-[5px] bg-[var(--color-surface-alt)]">
                    {thumb ? <img src={thumb} alt={video.title} className="h-full w-full object-cover" /> : <div className="flex h-full items-center justify-center text-[var(--color-text-muted)]"><ImageIcon /></div>}
                  </div>,
                  <button key="title" type="button" className="cursor-pointer truncate text-left font-semibold text-[var(--color-text)] hover:text-[var(--color-accent)]" onClick={() => router.push(`/admin/videos/${video.id}`)}>{video.title}</button>,
                  <Badge key="status" color={video.active ? 'green' : 'neutral'}>{video.active ? 'Active' : 'Hidden'}</Badge>,
                  <span key="order">#{video.displayOrder}</span>,
                  <span key="date">{formatDate(video.createdAt)}</span>,
                ]}
                actions={
                  <div className="flex flex-wrap items-center gap-1.5">
                    <Button size="xs" variant="ghost" icon={video.active ? <EyeOffIcon /> : <EyeIcon />} loading={toggling === video.id} onClick={() => handleToggleActive(video)}>
                      {video.active ? 'Hide' : 'Show'}
                    </Button>
                    <Button size="xs" variant="ghost" icon={<EditIcon />} onClick={() => router.push(`/admin/videos/${video.id}`)}>Edit</Button>
                    <Button size="xs" variant="danger" icon={<TrashIcon />} onClick={() => setVideoToDelete(video)}>Del</Button>
                  </div>
                }
              />
            );
          })}
        </Card>
      )}

      <DeleteConfirmationModal
        show={!!videoToDelete}
        title="Delete Video"
        message={`Are you sure you want to delete "${videoToDelete?.title}"? This action cannot be undone.`}
        onConfirm={handleDeleteConfirm}
        onCancel={() => setVideoToDelete(null)}
        loading={deleting}
      />
    </div>
  );
}

function getThumbnail(video: YouTubeVideo) {
  const match = video.videoUrl.match(/(?:v=|youtu\.be\/|embed\/)([^&?\s]{11})/);
  return match ? `https://img.youtube.com/vi/${match[1]}/mqdefault.jpg` : video.thumbnailUrl;
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : '-';
}

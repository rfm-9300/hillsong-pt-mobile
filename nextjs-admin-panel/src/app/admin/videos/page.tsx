'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { YouTubeVideo } from '@/lib/types';
import { api } from '@/lib/api';
import { Button, NavigationHeader, Card, DeleteConfirmationModal } from '@/app/components/ui';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export default function VideosPage() {
  const router = useRouter();
  const [videos, setVideos] = useState<YouTubeVideo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [videoToDelete, setVideoToDelete] = useState<YouTubeVideo | null>(null);
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
      alert(e instanceof Error ? e.message : 'Failed to toggle status');
    } finally {
      setToggling(null);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!videoToDelete) return;
    try {
      setDeletingId(videoToDelete.id);
      await api.youtubeVideos.delete(videoToDelete.id);
      await fetchVideos();
      setShowDeleteModal(false);
      setVideoToDelete(null);
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to delete video');
    } finally {
      setDeletingId(null);
    }
  };

  const getYouTubeId = (url: string) => {
    const match = url.match(/(?:v=|youtu\.be\/|embed\/)([^&?\s]{11})/);
    return match ? match[1] : null;
  };

  return (
    <div className="space-y-6">
      <NavigationHeader
        title="Videos Management"
        subtitle="Manage the YouTube videos shown in the mobile app's 'Latest Messages' section"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Videos', current: true },
        ]}
      >
        <Button variant="primary" onClick={() => router.push('/admin/videos/create')}>
          + Add Video
        </Button>
      </NavigationHeader>

      {error && (
        <Card className="p-4 border-red-200 bg-red-50">
          <p className="text-red-700 text-sm">{error}</p>
          <Button variant="secondary" size="sm" onClick={fetchVideos} className="mt-2">
            Retry
          </Button>
        </Card>
      )}

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map(i => (
            <Card key={i} className="animate-pulse">
              <div className="h-48 bg-gray-200 rounded-t-lg" />
              <div className="p-4 space-y-2">
                <div className="h-4 bg-gray-200 rounded w-3/4" />
                <div className="h-3 bg-gray-200 rounded w-1/2" />
              </div>
            </Card>
          ))}
        </div>
      ) : videos.length === 0 ? (
        <Card className="p-12 text-center">
          <div className="text-6xl mb-4">🎬</div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No videos yet</h3>
          <p className="text-gray-500 mb-6">Add YouTube videos to display in the mobile app.</p>
          <Button variant="primary" onClick={() => router.push('/admin/videos/create')}>
            Add First Video
          </Button>
        </Card>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {videos
            .sort((a, b) => a.displayOrder - b.displayOrder)
            .map((video) => {
              const ytId = getYouTubeId(video.videoUrl);
              const thumb = ytId
                ? `https://img.youtube.com/vi/${ytId}/mqdefault.jpg`
                : video.thumbnailUrl;

              return (
                <Card key={video.id} hover className="flex flex-col">
                  {/* Thumbnail */}
                  <div className="relative h-44 bg-gray-900 rounded-t-lg overflow-hidden">
                    <img
                      src={thumb}
                      alt={video.title}
                      className="w-full h-full object-cover opacity-90"
                    />
                    {/* Play overlay */}
                    <div className="absolute inset-0 flex items-center justify-center">
                      <div className="w-12 h-12 bg-red-600 rounded-full flex items-center justify-center shadow-lg">
                        <svg className="w-5 h-5 text-white ml-1" fill="currentColor" viewBox="0 0 24 24">
                          <path d="M8 5v14l11-7z" />
                        </svg>
                      </div>
                    </div>
                    {/* Order badge */}
                    <div className="absolute top-2 left-2 bg-black/70 text-white text-xs px-2 py-0.5 rounded-full font-mono">
                      #{video.displayOrder}
                    </div>
                    {/* Status badge */}
                    <div className="absolute top-2 right-2">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                        video.active ? 'bg-green-500 text-white' : 'bg-gray-500 text-white'
                      }`}>
                        {video.active ? 'Active' : 'Hidden'}
                      </span>
                    </div>
                  </div>

                  {/* Content */}
                  <div className="p-4 flex-1 flex flex-col">
                    <h3 className="font-bold text-gray-900 line-clamp-2 mb-1">{video.title}</h3>
                    {video.description && (
                      <p className="text-sm text-gray-500 line-clamp-2 mb-3 flex-1">{video.description}</p>
                    )}
                    <a
                      href={video.videoUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-xs text-blue-500 hover:underline truncate mb-4 block"
                    >
                      {video.videoUrl}
                    </a>

                    {/* Actions */}
                    <div className="flex gap-2 pt-3 border-t border-gray-100">
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => router.push(`/admin/videos/${video.id}`)}
                        className="flex-1"
                      >
                        Edit
                      </Button>
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => handleToggleActive(video)}
                        className="flex-1"
                      >
                        {toggling === video.id ? '...' : video.active ? 'Hide' : 'Show'}
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => { setVideoToDelete(video); setShowDeleteModal(true); }}
                      >
                        🗑
                      </Button>
                    </div>
                  </div>
                </Card>
              );
            })}
        </div>
      )}

      <DeleteConfirmationModal
        show={showDeleteModal}
        title="Delete Video"
        message={`Are you sure you want to delete "${videoToDelete?.title}"? This action cannot be undone.`}
        onConfirm={handleDeleteConfirm}
        onCancel={() => { setShowDeleteModal(false); setVideoToDelete(null); }}
        loading={!!deletingId}
      />
    </div>
  );
}

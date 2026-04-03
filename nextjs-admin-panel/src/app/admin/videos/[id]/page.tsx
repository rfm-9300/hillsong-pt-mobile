'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { YouTubeVideo } from '@/lib/types';
import { api } from '@/lib/api';
import { Button, NavigationHeader, Card } from '@/app/components/ui';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

interface FormData {
  title: string;
  description: string;
  videoUrl: string;
  thumbnailUrl: string;
  displayOrder: number;
  active: boolean;
}

function getYouTubeId(url: string) {
  const match = url.match(/(?:v=|youtu\.be\/|embed\/)([^&?\s]{11})/);
  return match ? match[1] : null;
}

function autoThumbnail(url: string) {
  const id = getYouTubeId(url);
  return id ? `https://img.youtube.com/vi/${id}/mqdefault.jpg` : '';
}

export default function EditVideoPage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const [form, setForm] = useState<FormData>({
    title: '',
    description: '',
    videoUrl: '',
    thumbnailUrl: '',
    displayOrder: 0,
    active: true,
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetch = async () => {
      try {
        setLoading(true);
        const res = await api.youtubeVideos.getById(id) as ApiResponse<YouTubeVideo>;
        const v = res?.data;
        if (v) {
          setForm({
            title: v.title,
            description: v.description ?? '',
            videoUrl: v.videoUrl,
            thumbnailUrl: v.thumbnailUrl,
            displayOrder: v.displayOrder,
            active: v.active,
          });
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load video');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [id]);

  const handleChange = (field: keyof FormData, value: string | number | boolean) => {
    setForm(prev => {
      const updated = { ...prev, [field]: value };
      if (field === 'videoUrl' && typeof value === 'string') {
        const thumb = autoThumbnail(value);
        if (thumb) updated.thumbnailUrl = thumb;
      }
      return updated;
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.title.trim() || !form.videoUrl.trim()) {
      setError('Title and Video URL are required.');
      return;
    }

    try {
      setSaving(true);
      setError(null);
      await api.youtubeVideos.update(id, form);
      router.push('/admin/videos');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to update video');
    } finally {
      setSaving(false);
    }
  };

  const previewThumb = form.thumbnailUrl || autoThumbnail(form.videoUrl);

  if (loading) {
    return (
      <div className="space-y-6">
        <NavigationHeader
          title="Edit Video"
          breadcrumbs={[
            { label: 'Dashboard', href: '/admin/dashboard' },
            { label: 'Videos', href: '/admin/videos' },
            { label: 'Edit', current: true },
          ]}
        />
        <Card className="p-6 animate-pulse">
          <div className="space-y-4">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="h-10 bg-gray-200 rounded" />
            ))}
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <NavigationHeader
        title="Edit Video"
        subtitle="Update this YouTube video's details"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Videos', href: '/admin/videos' },
          { label: 'Edit', current: true },
        ]}
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Form */}
        <div className="lg:col-span-2">
          <Card className="p-6">
            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Title <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={form.title}
                  onChange={e => handleChange('title', e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  YouTube URL <span className="text-red-500">*</span>
                </label>
                <input
                  type="url"
                  value={form.videoUrl}
                  onChange={e => handleChange('videoUrl', e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Thumbnail URL
                </label>
                <input
                  type="url"
                  value={form.thumbnailUrl}
                  onChange={e => handleChange('thumbnailUrl', e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description
                </label>
                <textarea
                  value={form.description}
                  onChange={e => handleChange('description', e.target.value)}
                  rows={3}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Display Order
                  </label>
                  <input
                    type="number"
                    min={0}
                    value={form.displayOrder}
                    onChange={e => handleChange('displayOrder', parseInt(e.target.value) || 0)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                  <p className="text-xs text-gray-400 mt-1">Lower numbers appear first.</p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
                  <div className="flex items-center gap-3 mt-2">
                    <button
                      type="button"
                      onClick={() => handleChange('active', !form.active)}
                      className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                        form.active ? 'bg-blue-600' : 'bg-gray-300'
                      }`}
                    >
                      <span
                        className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform shadow ${
                          form.active ? 'translate-x-6' : 'translate-x-1'
                        }`}
                      />
                    </button>
                    <span className="text-sm text-gray-700">
                      {form.active ? 'Active (visible in app)' : 'Hidden'}
                    </span>
                  </div>
                </div>
              </div>

              <div className="flex gap-3 pt-4 border-t border-gray-200">
                <Button type="submit" variant="primary" loading={saving}>
                  {saving ? 'Saving...' : 'Save Changes'}
                </Button>
                <Button type="button" variant="secondary" onClick={() => router.push('/admin/videos')}>
                  Cancel
                </Button>
              </div>
            </form>
          </Card>
        </div>

        {/* Preview */}
        <div>
          <Card className="p-4">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">Preview</h3>
            <div className="bg-gray-900 rounded-lg overflow-hidden">
              {previewThumb ? (
                <div className="relative h-40">
                  <img src={previewThumb} alt="Thumbnail preview" className="w-full h-full object-cover" />
                  <div className="absolute inset-0 flex items-center justify-center">
                    <div className="w-10 h-10 bg-red-600 rounded-full flex items-center justify-center">
                      <svg className="w-4 h-4 text-white ml-0.5" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M8 5v14l11-7z" />
                      </svg>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="h-40 flex items-center justify-center text-gray-500 text-sm">
                  Thumbnail preview
                </div>
              )}
            </div>
            {form.title && (
              <div className="mt-3">
                <p className="font-semibold text-gray-900 text-sm line-clamp-2">{form.title}</p>
                {form.description && (
                  <p className="text-xs text-gray-500 mt-1 line-clamp-2">{form.description}</p>
                )}
              </div>
            )}
            <div className="mt-3 flex gap-2 flex-wrap">
              <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                form.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'
              }`}>
                {form.active ? '● Active' : '○ Hidden'}
              </span>
              <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-700">
                Order: #{form.displayOrder}
              </span>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}

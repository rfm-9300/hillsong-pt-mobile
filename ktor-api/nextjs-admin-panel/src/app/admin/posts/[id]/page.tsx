'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { api, ENDPOINTS } from '@/lib/api';
import { Post } from '@/lib/types';
import { FormContainer, Input, Textarea, ImageUpload } from '@/app/components/forms';
import { Button, Alert, LoadingOverlay, NavigationHeader } from '@/app/components/ui';

export default function EditPostPage() {
  const router = useRouter();
  const params = useParams();
  const postId = params.id as string;

  const [post, setPost] = useState<Post | null>(null);
  const [formData, setFormData] = useState({
    title: '',
    content: '',
  });
  const [image, setImage] = useState<File | null>(null);
  const [currentImageUrl, setCurrentImageUrl] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  useEffect(() => {
    const fetchPost = async () => {
      try {
        const fetchedPost = await api.get<Post>(ENDPOINTS.POST_BY_ID(postId));
        if (fetchedPost) {
          setPost(fetchedPost);
          setFormData({
            title: fetchedPost.title,
            content: fetchedPost.content,
          });
          if (fetchedPost.imageUrl) {
            setCurrentImageUrl(fetchedPost.imageUrl);
          }
        }
      } catch (error) {
        setAlert({
          type: 'error',
          message: 'Failed to load post'
        });
      } finally {
        setLoading(false);
      }
    };

    if (postId) {
      fetchPost();
    }
  }, [postId]);

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleImageChange = (file: File | null) => {
    setImage(file);
    if (file) {
      setCurrentImageUrl(''); // Clear current image URL when new image is selected
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setAlert(null);

    try {
      const formDataToSend = new FormData();
      formDataToSend.append('postId', postId);
      formDataToSend.append('title', formData.title);
      formDataToSend.append('content', formData.content);
      
      if (image) {
        formDataToSend.append('image', image);
      }

      await api.postForm(ENDPOINTS.POST_UPDATE, formDataToSend);
      
      setAlert({
        type: 'success',
        message: 'Post updated successfully!'
      });

      // Redirect after a short delay
      setTimeout(() => {
        router.push('/admin/posts');
      }, 1500);
    } catch (error) {
      setAlert({
        type: 'error',
        message: error instanceof Error ? error.message : 'Failed to update post'
      });
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    router.push('/admin/posts');
  };

  if (loading) {
    return <LoadingOverlay show={true} message="Loading post..." />;
  }

  if (!post) {
    return (
      <div className="p-8 w-full max-w-3xl mx-auto">
        <div className="bg-red-50 text-red-700 p-4 rounded-lg">
          <p>Post not found</p>
          <Button
            variant="danger"
            onClick={handleCancel}
            className="mt-3"
          >
            Return to Posts
          </Button>
        </div>
      </div>
    );
  }

  const displayImageUrl = image ? URL.createObjectURL(image) : currentImageUrl;

  return (
    <div className="p-8 w-full max-w-3xl mx-auto">
      <NavigationHeader
        title="Edit Post"
        subtitle={post ? `Editing: ${post.title}` : 'Loading post...'}
        showBackButton={true}
        backButtonText="Back to Posts"
        backButtonHref="/admin/posts"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Posts', href: '/admin/posts' },
          { label: 'Edit Post', current: true },
        ]}
      />

      <div className="bg-white rounded-xl shadow-lg p-6">

        {alert && (
          <Alert
            type={alert.type}
            message={alert.message}
            onClose={() => setAlert(null)}
            className="mb-6"
          />
        )}

        <FormContainer onSubmit={handleSubmit}>
          <Input
            label="Title"
            value={formData.title}
            onChange={(value) => handleInputChange('title', value)}
            placeholder="Enter post title"
            required
            disabled={saving}
          />

          <ImageUpload
            label="Featured Image"
            value={displayImageUrl}
            onChange={handleImageChange}
            accept="image/*"
            disabled={saving}
          />

          <Textarea
            label="Content"
            value={formData.content}
            onChange={(value) => handleInputChange('content', value)}
            placeholder="Write your post content here..."
            rows={8}
            required
            disabled={saving}
          />

          <div className="flex items-center gap-3 pt-4 border-t border-gray-200">
            <Button
              variant="secondary"
              onClick={handleCancel}
              disabled={saving}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              loading={saving}
              disabled={saving}
              className="flex-1"
            >
              {saving ? 'Saving Changes...' : 'Update Post'}
            </Button>
          </div>
        </FormContainer>
      </div>
    </div>
  );
}
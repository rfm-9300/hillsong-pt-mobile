'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { api, ENDPOINTS } from '@/lib/api';
import { FormContainer, Input, Textarea, ImageUpload } from '@/app/components/forms';
import { Button, Alert } from '@/app/components/ui';

export default function CreatePostPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    title: '',
    content: '',
  });
  const [image, setImage] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleImageChange = (file: File | null) => {
    setImage(file);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setAlert(null);

    try {
      const formDataToSend = new FormData();
      formDataToSend.append('title', formData.title);
      formDataToSend.append('content', formData.content);
      
      if (image) {
        formDataToSend.append('image', image);
      }

      const result = await api.postForm(ENDPOINTS.POST_CREATE, formDataToSend);
      
      setAlert({
        type: 'success',
        message: 'Post created successfully!'
      });

      // Redirect after a short delay
      setTimeout(() => {
        router.push('/admin/posts');
      }, 2000);
    } catch (error) {
      setAlert({
        type: 'error',
        message: error instanceof Error ? error.message : 'Failed to create post'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.push('/admin/posts');
  };

  return (
    <div className="p-8 w-full max-w-3xl mx-auto">
      <div className="mb-8">
        <button 
          onClick={handleCancel}
          className="flex items-center text-gray-600 hover:text-blue-600 transition-colors"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back to Posts
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-lg p-6">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-800">Create New Post</h1>
          <p className="text-gray-600 mt-2">Write and publish a new blog post</p>
        </div>

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
            disabled={loading}
          />

          <ImageUpload
            label="Featured Image"
            value={image ? URL.createObjectURL(image) : undefined}
            onChange={handleImageChange}
            accept="image/*"
            disabled={loading}
          />

          <Textarea
            label="Content"
            value={formData.content}
            onChange={(value) => handleInputChange('content', value)}
            placeholder="Write your post content here..."
            rows={8}
            required
            disabled={loading}
          />

          <div className="flex items-center gap-3 pt-4 border-t border-gray-200">
            <Button
              variant="secondary"
              onClick={handleCancel}
              disabled={loading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              loading={loading}
              disabled={loading}
              className="flex-1"
            >
              {loading ? 'Creating Post...' : 'Create Post'}
            </Button>
          </div>
        </FormContainer>
      </div>
    </div>
  );
}
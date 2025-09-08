'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { api, ENDPOINTS } from '@/lib/api';
import { Post } from '@/lib/types';
import { PageHeader, Button, Alert, DeleteConfirmationModal } from '@/app/components/ui';
import PostsList from '@/app/components/PostsList';

export default function PostsPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleteConfirmation, setDeleteConfirmation] = useState<{
    show: boolean;
    postId: string | null;
    deleting: boolean;
  }>({
    show: false,
    postId: null,
    deleting: false
  });
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  useEffect(() => {
    fetchPosts();
  }, []);

  const fetchPosts = async () => {
    try {
      setLoading(true);
      const fetchedPosts = await api.get<Post[]>(ENDPOINTS.POSTS);
      setPosts(fetchedPosts || []);
    } catch (error) {
      setAlert({
        type: 'error',
        message: 'Failed to load posts'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePost = () => {
    router.push('/admin/posts/create');
  };

  const handleEditPost = (postId: string) => {
    router.push(`/admin/posts/${postId}`);
  };

  const handleDeletePost = (postId: string) => {
    setDeleteConfirmation({
      show: true,
      postId,
      deleting: false
    });
  };

  const confirmDelete = async () => {
    if (!deleteConfirmation.postId) return;

    setDeleteConfirmation(prev => ({ ...prev, deleting: true }));

    try {
      await api.post(ENDPOINTS.POST_DELETE, { postId: deleteConfirmation.postId });
      setPosts(prev => prev.filter(post => post.id !== deleteConfirmation.postId));
      setAlert({
        type: 'success',
        message: 'Post deleted successfully'
      });
    } catch (error) {
      setAlert({
        type: 'error',
        message: error instanceof Error ? error.message : 'Failed to delete post'
      });
    } finally {
      setDeleteConfirmation({
        show: false,
        postId: null,
        deleting: false
      });
    }
  };

  const cancelDelete = () => {
    setDeleteConfirmation({
      show: false,
      postId: null,
      deleting: false
    });
  };

  return (
    <div className="p-8">
      <PageHeader
        title="Manage Posts"
        subtitle="Create, edit, and manage your blog posts"
      >
        <Button
          onClick={handleCreatePost}
          className="flex items-center gap-2"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clipRule="evenodd" />
          </svg>
          Create Post
        </Button>
      </PageHeader>

      {alert && (
        <Alert
          type={alert.type}
          message={alert.message}
          onClose={() => setAlert(null)}
          className="mb-6"
        />
      )}

      <PostsList
        posts={posts}
        onEdit={handleEditPost}
        onDelete={handleDeletePost}
        loading={loading}
      />

      <DeleteConfirmationModal
        show={deleteConfirmation.show}
        title="Delete Post"
        message="Are you sure you want to delete this post? This action cannot be undone."
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        loading={deleteConfirmation.deleting}
      />
    </div>
  );
}
import { Post } from '@/lib/types';
import { EmptyState } from './ui';
import PostCard from './PostCard';

interface PostsListProps {
  posts: Post[];
  onEdit: (postId: string) => void;
  onDelete: (postId: string) => void;
  loading?: boolean;
}

export default function PostsList({ posts, onEdit, onDelete, loading = false }: PostsListProps) {
  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="bg-gray-200 rounded-xl h-64 animate-pulse"></div>
        ))}
      </div>
    );
  }

  if (posts.length === 0) {
    return (
      <EmptyState
        title="No Posts Yet"
        description="Create your first post to get started"
        actionText="Create Post"
        onAction={() => window.location.href = '/admin/posts/create'}
        icon={
          <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
        }
      />
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {posts.map((post, i) => (
        <div 
          key={post.id}
          className="animate-in fade-in slide-in-from-bottom-4"
          style={{ animationDelay: `${i * 75}ms`, animationDuration: '300ms' }}
        >
          <PostCard 
            post={post} 
            onEdit={() => onEdit(post.id)} 
            onDelete={() => onDelete(post.id)} 
          />
        </div>
      ))}
    </div>
  );
}
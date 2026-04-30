import Image from 'next/image';
import { Post } from '@/lib/types';
import { getImageUrl } from '@/lib/utils';
import { Button, Card, EmptyState, TableHeader, TableRow } from './ui';
import { EditIcon, ImageIcon, TrashIcon } from './icons/Icons';

interface PostsListProps {
  posts: Post[];
  onEdit: (postId: string) => void;
  onDelete: (postId: string) => void;
  loading?: boolean;
}

const cols = [
  { label: 'Image', width: '120px' },
  { label: 'Title', width: '1fr' },
  { label: 'Date', width: '140px' },
  { label: 'Author', width: '120px' },
  { label: '', width: '100px' },
];

export default function PostsList({ posts, onEdit, onDelete, loading = false }: PostsListProps) {
  if (loading) {
    return (
      <Card>
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="mx-3 my-3 h-12 rounded skeleton-shimmer" />
        ))}
      </Card>
    );
  }

  if (posts.length === 0) {
    return (
      <Card>
        <EmptyState
          title="No posts yet"
          description="Create your first post to get started."
          actionText="Create Post"
          onAction={() => window.location.href = '/admin/posts/create'}
          icon={<ImageIcon />}
        />
      </Card>
    );
  }

  return (
    <Card className="overflow-hidden">
      <TableHeader cols={cols} />
      {posts.map((post, index) => {
        const imagePath = post.headerImagePath || post.imageUrl;
        const imageUrl = imagePath ? getImageUrl(imagePath) : '';
        return (
          <TableRow
            key={post.id}
            cols={cols}
            last={index === posts.length - 1}
            cells={[
              <div key="image" className="flex h-[46px] w-20 items-center justify-center overflow-hidden rounded-[6px] bg-[var(--color-surface-alt)] text-[var(--color-text-muted)]">
                {imageUrl ? (
                  <Image src={imageUrl} alt={post.title} width={80} height={46} className="h-full w-full object-cover" />
                ) : (
                  <ImageIcon />
                )}
              </div>,
              <button key="title" type="button" onClick={() => onEdit(post.id)} className="block cursor-pointer truncate text-left font-semibold text-[var(--color-text)] hover:text-[var(--color-accent)]">
                {post.title}
              </button>,
              <span key="date">{formatDate(post.date || post.createdAt)}</span>,
              <span key="author" className="truncate">{post.author?.fullName || 'Admin'}</span>,
            ]}
            actions={
              <div className="flex flex-wrap items-center gap-1.5">
                <Button size="xs" variant="ghost" icon={<EditIcon />} onClick={() => onEdit(post.id)}>Edit</Button>
                <Button size="xs" variant="danger" icon={<TrashIcon />} onClick={() => onDelete(post.id)}>Del</Button>
              </div>
            }
          />
        );
      })}
    </Card>
  );
}

function formatDate(value: string) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

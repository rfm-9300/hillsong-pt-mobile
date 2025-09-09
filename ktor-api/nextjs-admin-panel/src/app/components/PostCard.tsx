import Image from 'next/image';
import { Post } from '@/lib/types';

interface PostCardProps {
  post: Post;
  onEdit: () => void;
  onDelete: () => void;
}

export default function PostCard({ post, onEdit, onDelete }: PostCardProps) {
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getImageUrl = (imagePath?: string) => {
    if (!imagePath) return null;
    // Handle both full URLs and relative paths
    if (imagePath.startsWith('http')) {
      return imagePath;
    }
    return `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}/resources/uploads/images/${imagePath}`;
  };

  const imageUrl = getImageUrl(post.imageUrl);

  return (
    <div 
      className="relative bg-white rounded-xl overflow-hidden shadow-lg transition-all duration-300 hover:shadow-xl border border-gray-100 h-64 sm:h-72 cursor-pointer hover:scale-[1.02] hover:-translate-y-1 group touch-target"
      onClick={onEdit}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === 'Enter' && onEdit()}
      aria-label={`Edit post: ${post.title}`}
    >
      {/* Background image with gradient overlay */}
      <div className="absolute inset-0">
        {imageUrl ? (
          <Image 
            src={imageUrl} 
            alt={post.title} 
            fill
            className="object-cover"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
            onError={(e) => {
              // Fallback to gradient background if image fails to load
              const target = e.target as HTMLImageElement;
              target.style.display = 'none';
              const parent = target.parentElement;
              if (parent) {
                parent.innerHTML = `
                  <div class="w-full h-full bg-gradient-to-r from-blue-100 to-indigo-100 flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                `;
              }
            }}
          />
        ) : (
          <div className="w-full h-full bg-gradient-to-r from-blue-100 to-indigo-100 flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}
        {/* Gradient overlay for better text readability */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-black/20"></div>
      </div>
      
      {/* Post content on top of the background */}
      <div className="relative h-full flex flex-col justify-between p-4 sm:p-5 text-white z-10">
        <div>
          <h3 className="text-lg sm:text-xl font-bold mb-2 text-white line-clamp-2">{post.title}</h3>
          <p className="text-white/80 mb-2 sm:mb-4 line-clamp-2 text-sm sm:text-base">{post.content}</p>
        </div>
        
        <div className="mt-auto">
          <div className="flex justify-between text-sm text-white/70 mb-4">
            <div className="flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              {formatDate(post.createdAt)}
            </div>
          </div>
          
          <div className="flex justify-between pt-2 sm:pt-3 border-t border-white/20">
            <button 
              className="text-white hover:text-blue-300 flex items-center transition-all duration-200 cursor-pointer touch-target hover:scale-105 text-sm sm:text-base"
              onClick={(e) => { 
                e.stopPropagation(); 
                onEdit(); 
              }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              <span className="hidden sm:inline">Edit</span>
            </button>
            <button 
              onClick={(e) => { 
                e.stopPropagation(); 
                onDelete(); 
              }}
              className="text-white hover:text-red-300 flex items-center transition-all duration-200 cursor-pointer hover:scale-105 touch-target text-sm sm:text-base"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              <span className="hidden sm:inline">Delete</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { Card, Button, DeleteConfirmationModal } from './ui';
import { Event } from '@/lib/types';
import { formatDate, formatTime, truncateText } from '@/lib/utils';
import { api, ENDPOINTS } from '@/lib/api';

interface EventCardProps {
  event: Event;
  onDelete?: (eventId: string) => void;
}

export default function EventCard({ event, onDelete }: EventCardProps) {
  const router = useRouter();
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const eventDate = new Date(event.date);
  const isUpcoming = eventDate > new Date();
  
  // Generate gradient based on event ID for visual variety
  const gradients = [
    'from-blue-500 to-purple-600',
    'from-green-500 to-teal-600',
    'from-orange-500 to-red-600',
    'from-purple-500 to-pink-600',
    'from-teal-500 to-blue-600',
    'from-red-500 to-orange-600',
  ];
  
  const gradientIndex = event.id.toString().split('').reduce((acc, char) => {
    return char.charCodeAt(0) + acc;
  }, 0) % gradients.length;
  
  const gradient = gradients[gradientIndex];

  const handleEdit = () => {
    router.push(`/admin/events/${event.id}`);
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await api.delete(ENDPOINTS.EVENT_DELETE, { id: event.id });
      onDelete?.(event.id);
      setShowDeleteModal(false);
    } catch (error) {
      console.error('Error deleting event:', error);
      // You might want to show an error message here
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <Card 
        hover 
        className="group cursor-pointer transition-all duration-300 hover:shadow-lg hover:-translate-y-1 touch-target"
        onClick={handleEdit}
      >
        <div className="relative overflow-hidden">
          {/* Gradient Header */}
          <div className={`h-32 bg-gradient-to-r ${gradient} relative`}>
            {(event.imageUrl || event.headerImagePath) && event.headerImagePath !== '' && (
              <Image
                src={event.imageUrl || (event.headerImagePath ? `/${event.headerImagePath}` : '')}
                alt={event.title}
                fill
                className="object-cover"
                sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
                onError={(e) => {
                  // Hide the image if it fails to load
                  e.currentTarget.style.display = 'none';
                }}
              />
            )}
            
            {/* Calendar Indicator */}
            <div className="absolute top-4 right-4 bg-white rounded-lg shadow-md p-2 text-center min-w-[60px]">
              <div className="text-xs font-semibold text-gray-600 uppercase">
                {eventDate.toLocaleDateString('en-US', { month: 'short' })}
              </div>
              <div className="text-lg font-bold text-gray-900">
                {eventDate.getDate()}
              </div>
            </div>
            
            {/* Status Badge */}
            <div className="absolute top-4 left-4">
              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                isUpcoming 
                  ? 'bg-green-100 text-green-800' 
                  : 'bg-gray-100 text-gray-800'
              }`}>
                {isUpcoming ? 'Upcoming' : 'Past'}
              </span>
            </div>
          </div>
          
          {/* Content */}
          <div className="p-4 sm:p-6">
            <div className="flex justify-between items-start mb-3">
              <h3 className="text-base sm:text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition-colors line-clamp-2">
                {event.title}
              </h3>
            </div>
            
            <p className="text-gray-600 text-sm mb-4 line-clamp-2">
              {truncateText(event.description, 120)}
            </p>
            
            {/* Event Metadata */}
            <div className="space-y-2 mb-4">
              <div className="flex items-center text-sm text-gray-500">
                <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                {formatDate(event.date)} at {formatTime(event.date)}
              </div>
              
              <div className="flex items-center text-sm text-gray-500">
                <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                {event.location}
              </div>
            </div>
            
            {/* Action Buttons */}
            <div className="flex gap-2 opacity-0 group-hover:opacity-100 sm:opacity-100 transition-opacity">
              <Button
                size="sm"
                variant="primary"
                onClick={(e) => {
                  e?.stopPropagation();
                  handleEdit();
                }}
                className="flex-1 sm:flex-none"
              >
                <span className="sm:hidden">✏️</span>
                <span className="hidden sm:inline">Edit</span>
              </Button>
              
              <Button
                size="sm"
                variant="danger"
                onClick={(e) => {
                  e?.stopPropagation();
                  setShowDeleteModal(true);
                }}
                className="flex-1 sm:flex-none"
              >
                <span className="sm:hidden">🗑️</span>
                <span className="hidden sm:inline">Delete</span>
              </Button>
            </div>
          </div>
        </div>
      </Card>
      
      <DeleteConfirmationModal
        show={showDeleteModal}
        title="Delete Event"
        message={`Are you sure you want to delete "${event.title}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteModal(false)}
        loading={deleting}
      />
    </>
  );
}
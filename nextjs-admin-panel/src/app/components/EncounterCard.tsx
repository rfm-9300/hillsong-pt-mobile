'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, Button, DeleteConfirmationModal } from './ui';
import { Encounter } from '@/lib/types';
import { api, ENDPOINTS } from '@/lib/api';

interface EncounterCardProps {
  encounter: Encounter;
  onDelete?: (encounterId: number) => void;
}

export default function EncounterCard({ encounter, onDelete }: EncounterCardProps) {
  const router = useRouter();
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  };

  const isUpcoming = () => {
    return new Date(encounter.date) > new Date();
  };

  const handleEdit = () => {
    router.push(`/admin/encounters/${encounter.id}`);
  };

  const handleDelete = async () => {
    try {
      setDeleting(true);
      await api.delete(ENDPOINTS.ENCOUNTER_DELETE(encounter.id.toString()));
      onDelete?.(encounter.id);
      setShowDeleteModal(false);
    } catch (error) {
      console.error('Error deleting encounter:', error);
      alert(error instanceof Error ? error.message : 'Failed to delete encounter');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <Card hover className="flex flex-col h-full">
        {/* Image Section */}
        <div className="relative h-48 bg-gradient-to-br from-blue-500 to-purple-600 rounded-t-lg overflow-hidden">
          {encounter.imagePath ? (
            <img
              src={`${process.env.NEXT_PUBLIC_API_BASE_URL}/files/${encounter.imagePath}`}
              alt={encounter.title}
              className="w-full h-full object-cover"
              onError={(e) => {
                // Fallback to gradient if image fails to load
                e.currentTarget.style.display = 'none';
              }}
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <svg className="w-16 h-16 text-white opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
          )}
          
          {/* Status Badge */}
          <div className="absolute top-3 right-3">
            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
              isUpcoming() 
                ? 'bg-green-500 text-white' 
                : 'bg-gray-500 text-white'
            }`}>
              {isUpcoming() ? 'Upcoming' : 'Past'}
            </span>
          </div>
        </div>

        {/* Content Section */}
        <div className="p-6 flex-1 flex flex-col">
          <h3 className="text-xl font-bold text-gray-900 mb-2 line-clamp-2">
            {encounter.title}
          </h3>
          
          <p className="text-gray-600 text-sm mb-4 line-clamp-3 flex-1">
            {encounter.description}
          </p>

          {/* Details */}
          <div className="space-y-2 mb-4">
            <div className="flex items-center text-sm text-gray-600">
              <svg className="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span>{formatDate(encounter.date)}</span>
            </div>
            
            <div className="flex items-center text-sm text-gray-600">
              <svg className="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span className="line-clamp-1">{encounter.location}</span>
            </div>
            
            <div className="flex items-center text-sm text-gray-600">
              <svg className="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              <span className="line-clamp-1">Organized by {encounter.organizerName}</span>
            </div>
          </div>

          {/* Actions */}
          <div className="flex gap-2 pt-4 border-t border-gray-200">
            <Button
              variant="primary"
              size="sm"
              onClick={handleEdit}
              className="flex-1"
            >
              Edit
            </Button>
            <Button
              variant="danger"
              size="sm"
              onClick={() => setShowDeleteModal(true)}
              className="flex-1"
            >
              Delete
            </Button>
          </div>
        </div>
      </Card>

      <DeleteConfirmationModal
        show={showDeleteModal}
        title="Delete Encounter"
        message={`Are you sure you want to delete "${encounter.title}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteModal(false)}
        loading={deleting}
      />
    </>
  );
}

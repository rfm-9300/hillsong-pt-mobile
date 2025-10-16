'use client';

import { useState, useEffect } from 'react';
import { EmptyState, LoadingSkeleton, Alert, AnimatedGrid } from './ui';
import EncounterCard from './EncounterCard';
import { Encounter } from '@/lib/types';
import { api, ENDPOINTS } from '@/lib/api';

interface EncountersListProps {
  onEncounterDeleted?: () => void;
}

export default function EncountersList({ onEncounterDeleted }: EncountersListProps) {
  const [encounters, setEncounters] = useState<Encounter[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchEncounters = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get<{ data: { content: Encounter[] } }>(ENDPOINTS.ENCOUNTERS);
      if (response?.data?.content) {
        // Sort encounters by date (upcoming first, then by date)
        const sortedEncounters = response.data.content.sort((a, b) => {
          const dateA = new Date(a.date);
          const dateB = new Date(b.date);
          const now = new Date();
          
          const aIsUpcoming = dateA > now;
          const bIsUpcoming = dateB > now;
          
          // If one is upcoming and other is past, upcoming comes first
          if (aIsUpcoming && !bIsUpcoming) return -1;
          if (!aIsUpcoming && bIsUpcoming) return 1;
          
          // If both are upcoming or both are past, sort by date
          return dateA.getTime() - dateB.getTime();
        });
        
        setEncounters(sortedEncounters);
      }
    } catch (error) {
      console.error('Error fetching encounters:', error);
      setError(error instanceof Error ? error.message : 'Failed to load encounters');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEncounters();
  }, []);

  const handleEncounterDeleted = (encounterId: number) => {
    setEncounters(prev => prev.filter(encounter => encounter.id !== encounterId));
    onEncounterDeleted?.();
  };

  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 6 }).map((_, index) => (
          <LoadingSkeleton 
            key={index} 
            className="h-80 rounded-lg"
            style={{ animationDelay: `${index * 100}ms` }}
          />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <Alert
        type="error"
        message={error}
        onClose={() => setError(null)}
      />
    );
  }

  if (encounters.length === 0) {
    return (
      <EmptyState
        title="No encounters found"
        description="Get started by creating your first encounter. Encounters help you organize simple community gatherings."
        actionText="Create Encounter"
        onAction={() => window.location.href = '/admin/encounters/create'}
        icon={
          <svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
        }
      />
    );
  }

  return (
    <AnimatedGrid cols="grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
      {encounters.map((encounter) => (
        <EncounterCard
          key={encounter.id}
          encounter={encounter}
          onDelete={handleEncounterDeleted}
        />
      ))}
    </AnimatedGrid>
  );
}

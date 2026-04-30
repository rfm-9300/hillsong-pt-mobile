'use client';

import { useState, useEffect } from 'react';
import { Card, EmptyState, LoadingSkeleton, Alert } from './ui';
import { EncountersIcon } from './icons/Icons';
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
      <div className="space-y-3">
        {Array.from({ length: 6 }).map((_, index) => (
          <LoadingSkeleton 
            key={index} 
            className="h-[120px] rounded-[10px]"
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
      <Card>
        <EmptyState
          title="No encounters found"
          description="Get started by creating your first encounter."
          actionText="Create Encounter"
          onAction={() => window.location.href = '/admin/encounters/create'}
          icon={<EncountersIcon />}
        />
      </Card>
    );
  }

  return (
    <div className="space-y-3">
      {encounters.map((encounter) => (
        <EncounterCard
          key={encounter.id}
          encounter={encounter}
          onDelete={handleEncounterDeleted}
        />
      ))}
    </div>
  );
}

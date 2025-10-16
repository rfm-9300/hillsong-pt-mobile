'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { Button, Alert, LoadingOverlay, NavigationHeader, LoadingSkeleton } from '@/app/components/ui';
import { FormContainer, Input, Textarea, ImageUpload } from '@/app/components/forms';
import { api, ENDPOINTS } from '@/lib/api';
import { Encounter } from '@/lib/types';

export default function EditEncounterPage() {
  const router = useRouter();
  const params = useParams();
  const encounterId = params.id as string;
  
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    date: '',
    location: '',
    imageFile: null as File | null,
    currentImagePath: '' as string | undefined
  });
  
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    const fetchEncounter = async () => {
      try {
        setFetchLoading(true);
        const response = await api.get<{ data: Encounter }>(ENDPOINTS.ENCOUNTER_BY_ID(encounterId));
        
        if (response?.data) {
          const encounter = response.data;
          // Convert backend datetime format to datetime-local format
          const dateStr = encounter.date.substring(0, 16); // Remove seconds
          
          setFormData({
            title: encounter.title,
            description: encounter.description,
            date: dateStr,
            location: encounter.location,
            imageFile: null,
            currentImagePath: encounter.imagePath
          });
        }
      } catch (error) {
        console.error('Error fetching encounter:', error);
        setAlert({
          type: 'error',
          message: error instanceof Error ? error.message : 'Failed to load encounter'
        });
      } finally {
        setFetchLoading(false);
      }
    };

    fetchEncounter();
  }, [encounterId]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    
    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
    }
    
    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    }
    
    if (!formData.date) {
      newErrors.date = 'Date is required';
    }
    
    if (!formData.location.trim()) {
      newErrors.location = 'Location is required';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    setAlert(null);
    
    try {
      const submitData = new FormData();
      
      // Create encounter object as JSON
      const encounterData = {
        title: formData.title,
        description: formData.description,
        date: formData.date + ':00', // Convert from datetime-local format to backend format
        location: formData.location
      };
      
      // Add encounter data as JSON blob
      submitData.append('encounter', new Blob([JSON.stringify(encounterData)], {
        type: 'application/json'
      }));
      
      // Add image if provided
      if (formData.imageFile) {
        submitData.append('image', formData.imageFile);
      }
      
      await api.putForm<Encounter>(ENDPOINTS.ENCOUNTER_UPDATE(encounterId), submitData);
      
      setAlert({ type: 'success', message: 'Encounter updated successfully!' });
      
      // Redirect after a short delay
      setTimeout(() => {
        router.push('/admin/encounters');
      }, 1500);
      
    } catch (error) {
      console.error('Error updating encounter:', error);
      setAlert({ 
        type: 'error', 
        message: error instanceof Error ? error.message : 'Failed to update encounter' 
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.push('/admin/encounters');
  };

  if (fetchLoading) {
    return (
      <div className="space-y-6">
        <NavigationHeader
          title="Edit Encounter"
          subtitle="Update encounter details"
          showBackButton={true}
          backButtonText="Back to Encounters"
          backButtonHref="/admin/encounters"
          breadcrumbs={[
            { label: 'Dashboard', href: '/admin/dashboard' },
            { label: 'Encounters', href: '/admin/encounters' },
            { label: 'Edit Encounter', current: true },
          ]}
        />
        <div className="max-w-2xl space-y-4">
          <LoadingSkeleton className="h-12" />
          <LoadingSkeleton className="h-32" />
          <LoadingSkeleton className="h-12" />
          <LoadingSkeleton className="h-12" />
          <LoadingSkeleton className="h-32" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <NavigationHeader
        title="Edit Encounter"
        subtitle="Update encounter details"
        showBackButton={true}
        backButtonText="Back to Encounters"
        backButtonHref="/admin/encounters"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Encounters', href: '/admin/encounters' },
          { label: 'Edit Encounter', current: true },
        ]}
      />
      
      {alert && (
        <Alert
          type={alert.type}
          message={alert.message}
          onClose={() => setAlert(null)}
        />
      )}
      
      <div className="max-w-2xl">
        <FormContainer onSubmit={handleSubmit}>
          <div className="space-y-6">
            <Input
              label="Encounter Title"
              value={formData.title}
              onChange={(value) => setFormData(prev => ({ ...prev, title: value }))}
              error={errors.title}
              placeholder="Enter encounter title"
              required
              disabled={loading}
            />
            
            <Textarea
              label="Description"
              value={formData.description}
              onChange={(value) => setFormData(prev => ({ ...prev, description: value }))}
              error={errors.description}
              placeholder="Enter encounter description"
              rows={4}
              required
              disabled={loading}
            />
            
            <Input
              label="Date & Time"
              type="datetime-local"
              value={formData.date}
              onChange={(value) => setFormData(prev => ({ ...prev, date: value }))}
              error={errors.date}
              required
              disabled={loading}
            />
            
            <Input
              label="Location"
              value={formData.location}
              onChange={(value) => setFormData(prev => ({ ...prev, location: value }))}
              error={errors.location}
              placeholder="Enter encounter location"
              required
              disabled={loading}
            />
            
            <ImageUpload
              label="Encounter Image (Optional)"
              value={formData.imageFile ? URL.createObjectURL(formData.imageFile) : formData.currentImagePath}
              onChange={(file) => setFormData(prev => ({ ...prev, imageFile: file }))}
              accept="image/*"
              disabled={loading}
            />
            
            <div className="flex gap-4 pt-4">
              <Button
                type="submit"
                variant="primary"
                loading={loading}
                disabled={loading}
              >
                Update Encounter
              </Button>
              
              <Button
                type="button"
                variant="secondary"
                onClick={handleCancel}
                disabled={loading}
              >
                Cancel
              </Button>
            </div>
          </div>
        </FormContainer>
      </div>
      
      <LoadingOverlay show={loading} message="Updating encounter..." />
    </div>
  );
}

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button, Alert, LoadingOverlay, NavigationHeader } from '@/app/components/ui';
import { FormContainer, Input, Textarea, ImageUpload } from '@/app/components/forms';
import { api, ENDPOINTS } from '@/lib/api';
import { Encounter } from '@/lib/types';

export default function CreateEncounterPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    date: '',
    location: '',
    imageFile: null as File | null
  });
  
  const [errors, setErrors] = useState<Record<string, string>>({});

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
    } else {
      const encounterDate = new Date(formData.date);
      const now = new Date();
      if (encounterDate < now) {
        newErrors.date = 'Encounter date cannot be in the past';
      }
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
      
      await api.postForm<Encounter>(ENDPOINTS.ENCOUNTERS, submitData);
      
      setAlert({ type: 'success', message: 'Encounter created successfully!' });
      
      // Redirect after a short delay
      setTimeout(() => {
        router.push('/admin/encounters');
      }, 1500);
      
    } catch (error) {
      console.error('Error creating encounter:', error);
      setAlert({ 
        type: 'error', 
        message: error instanceof Error ? error.message : 'Failed to create encounter' 
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.push('/admin/encounters');
  };

  return (
    <div className="space-y-6">
      <NavigationHeader
        title="Create Encounter"
        subtitle="Add a new encounter to the system"
        showBackButton={true}
        backButtonText="Back to Encounters"
        backButtonHref="/admin/encounters"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Encounters', href: '/admin/encounters' },
          { label: 'Create Encounter', current: true },
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
              value={formData.imageFile ? URL.createObjectURL(formData.imageFile) : undefined}
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
                Create Encounter
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
      
      <LoadingOverlay show={loading} message="Creating encounter..." />
    </div>
  );
}

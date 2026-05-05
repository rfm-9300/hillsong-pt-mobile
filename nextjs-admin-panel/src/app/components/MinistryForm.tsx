'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Alert, Button, LoadingOverlay, NavigationHeader } from './ui';
import { Checkbox, FormContainer, ImageUpload, Input, MinistrySelect, Textarea } from './forms';
import { api, ENDPOINTS } from '@/lib/api';
import {
  defaultMinistryFormValues,
  ministryFormSchema,
  MinistryFormValues,
} from '@/lib/ministries';
import { MinistryDoc } from '@/lib/types';
import { getImageUrl } from '@/lib/utils';

interface MinistryFormProps {
  mode: 'create' | 'edit';
  ministryId?: string;
  initialMinistry?: MinistryDoc | null;
}

export default function MinistryForm({ mode, ministryId, initialMinistry }: MinistryFormProps) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [imageFile, setImageFile] = useState<File | null>(null);

  const initialValues = useMemo<MinistryFormValues>(() => {
    if (!initialMinistry) return defaultMinistryFormValues;
    return {
      name: initialMinistry.name,
      type: initialMinistry.type,
      description: initialMinistry.description,
      isActive: initialMinistry.isActive,
      isJoinable: initialMinistry.isJoinable,
      requiresApproval: initialMinistry.requiresApproval,
      tags: initialMinistry.tags.join(', '),
      hasLocation: !!initialMinistry.location,
      location: initialMinistry.location
        ? {
            addressLine: initialMinistry.location.addressLine,
            city: initialMinistry.location.city,
            region: initialMinistry.location.region ?? '',
            postalCode: initialMinistry.location.postalCode ?? '',
            country: initialMinistry.location.country,
            latitude: initialMinistry.location.latitude,
            longitude: initialMinistry.location.longitude,
          }
        : undefined,
    };
  }, [initialMinistry]);

  const [formData, setFormData] = useState<MinistryFormValues>(initialValues);

  const validate = () => {
    const result = ministryFormSchema.safeParse(formData);
    if (result.success) {
      setErrors({});
      return result.data;
    }
    const fieldErrors: Record<string, string> = {};
    for (const issue of result.error.issues) {
      const path = issue.path.join('.');
      if (!fieldErrors[path]) fieldErrors[path] = issue.message;
    }
    setErrors(fieldErrors);
    return null;
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    const parsed = validate();
    if (!parsed) return;

    setLoading(true);
    setAlert(null);

    try {
      const payload = {
        name: parsed.name,
        type: parsed.type,
        description: parsed.description,
        isActive: parsed.isActive,
        isJoinable: parsed.isJoinable,
        requiresApproval: parsed.requiresApproval,
        tags: parsed.tags
          .split(',')
          .map((tag) => tag.trim())
          .filter(Boolean),
        location:
          parsed.hasLocation && parsed.location
            ? {
                ...parsed.location,
                region: parsed.location.region || null,
                postalCode: parsed.location.postalCode || null,
              }
            : null,
      };

      const submitData = new FormData();
      submitData.append('ministry', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
      if (imageFile) submitData.append('image', imageFile);

      if (mode === 'create') {
        await api.postForm(ENDPOINTS.ADMIN_MINISTRY_CREATE, submitData);
        setAlert({ type: 'success', message: 'Ministry created successfully.' });
      } else if (ministryId) {
        await api.putForm(ENDPOINTS.ADMIN_MINISTRY_UPDATE(ministryId), submitData);
        setAlert({ type: 'success', message: 'Ministry updated successfully.' });
      }

      setTimeout(() => router.push('/admin/ministries'), 800);
    } catch (submitError) {
      console.error('Error saving ministry:', submitError);
      setAlert({
        type: 'error',
        message: submitError instanceof Error ? submitError.message : 'Failed to save ministry',
      });
    } finally {
      setLoading(false);
    }
  };

  const currentImage = imageFile
    ? URL.createObjectURL(imageFile)
    : initialMinistry?.imagePath
      ? getImageUrl(initialMinistry.imagePath)
      : undefined;

  return (
    <div className="space-y-6">
      <NavigationHeader
        title={mode === 'create' ? 'Create Ministry' : 'Edit Ministry'}
        subtitle={
          mode === 'create'
            ? 'Add a new ministry — service team, connection-group umbrella, or campus team'
            : initialMinistry
              ? `Editing: ${initialMinistry.name}`
              : 'Update ministry details'
        }
        showBackButton
        backButtonText="Back to Ministries"
        backButtonHref="/admin/ministries"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Ministries', href: '/admin/ministries' },
          { label: mode === 'create' ? 'Create Ministry' : 'Edit Ministry', current: true },
        ]}
      />

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      <div className="max-w-4xl">
        <FormContainer onSubmit={handleSubmit}>
          <div className="space-y-6">
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <Input
                label="Ministry name"
                value={formData.name}
                onChange={(value) => setFormData((c) => ({ ...c, name: value }))}
                error={errors.name}
                placeholder="Worship Team — Lisboa"
                required
                disabled={loading}
              />

              <MinistrySelect
                value={formData.type}
                onChange={(value) => setFormData((c) => ({ ...c, type: value }))}
                error={errors.type}
                disabled={loading}
              />
            </div>

            <Textarea
              label="Description"
              value={formData.description}
              onChange={(value) => setFormData((c) => ({ ...c, description: value }))}
              error={errors.description}
              placeholder="Describe the ministry, who is on the team, and how to serve."
              rows={5}
              required
              disabled={loading}
              maxLength={2000}
              showCharCount
            />

            <Input
              label="Tags"
              value={formData.tags}
              onChange={(value) => setFormData((c) => ({ ...c, tags: value }))}
              error={errors.tags}
              placeholder="lisboa, music, sunday"
              disabled={loading}
            />

            <ImageUpload
              label="Ministry image"
              value={currentImage}
              onChange={setImageFile}
              accept="image/*"
              disabled={loading}
            />

            <div className="grid gap-3 md:grid-cols-3">
              <Checkbox
                label="Active (visible)"
                checked={formData.isActive}
                onChange={(checked) => setFormData((c) => ({ ...c, isActive: checked }))}
                disabled={loading}
              />
              <Checkbox
                label="Joinable (volunteers can self-request)"
                checked={formData.isJoinable}
                onChange={(checked) => setFormData((c) => ({ ...c, isJoinable: checked }))}
                disabled={loading}
              />
              <Checkbox
                label="Requires leader approval to join"
                checked={formData.requiresApproval}
                onChange={(checked) => setFormData((c) => ({ ...c, requiresApproval: checked }))}
                disabled={loading}
              />
            </div>

            <div className="flex gap-4 pt-4">
              <Button type="submit" variant="primary" loading={loading} disabled={loading}>
                {mode === 'create' ? 'Create Ministry' : 'Update Ministry'}
              </Button>
              <Button type="button" variant="secondary" onClick={() => router.push('/admin/ministries')} disabled={loading}>
                Cancel
              </Button>
            </div>
          </div>
        </FormContainer>
      </div>

      <LoadingOverlay show={loading} message={mode === 'create' ? 'Creating ministry...' : 'Updating ministry...'} />
    </div>
  );
}

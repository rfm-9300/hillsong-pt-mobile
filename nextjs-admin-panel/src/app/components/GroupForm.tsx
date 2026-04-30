'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Alert, Button, LoadingOverlay, NavigationHeader } from './ui';
import { Checkbox, FormContainer, ImageUpload, Input, LocationPicker, MinistrySelect, Textarea } from './forms';
import { api, ENDPOINTS } from '@/lib/api';
import {
  dayOptions,
  defaultGroupFormValues,
  formatGroupDay,
  groupFormSchema,
  GroupFormValues,
  frequencyOptions,
} from '@/lib/groups';
import { Group, GroupDayOfWeek, MeetingFrequency } from '@/lib/types';
import { getImageUrl } from '@/lib/utils';

interface GroupFormProps {
  mode: 'create' | 'edit';
  groupId?: string;
  initialGroup?: Group | null;
}

export default function GroupForm({ mode, groupId, initialGroup }: GroupFormProps) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [imageFile, setImageFile] = useState<File | null>(null);

  const initialValues = useMemo<GroupFormValues>(() => {
    if (!initialGroup) return defaultGroupFormValues;

    return {
      name: initialGroup.name,
      ministry: initialGroup.ministry,
      description: initialGroup.description,
      leaderName: initialGroup.leaderName,
      leaderContact: initialGroup.leaderContact,
      meetingDay: initialGroup.meetingDay,
      meetingTime: initialGroup.meetingTime,
      frequency: initialGroup.frequency,
      location: {
        addressLine: initialGroup.location.addressLine,
        city: initialGroup.location.city,
        region: initialGroup.location.region ?? '',
        postalCode: initialGroup.location.postalCode ?? '',
        country: initialGroup.location.country,
        latitude: initialGroup.location.latitude,
        longitude: initialGroup.location.longitude,
      },
      maxMembers: initialGroup.maxMembers ?? '',
      currentMembers: initialGroup.currentMembers,
      isActive: initialGroup.isActive,
      isJoinable: initialGroup.isJoinable,
      tags: initialGroup.tags.join(', '),
    };
  }, [initialGroup]);

  const [formData, setFormData] = useState<GroupFormValues>(initialValues);

  const validate = () => {
    const result = groupFormSchema.safeParse(formData);
    if (result.success) {
      setErrors({});
      return result.data;
    }

    const fieldErrors: Record<string, string> = {};
    for (const issue of result.error.issues) {
      const path = issue.path.join('.');
      if (!fieldErrors[path]) {
        fieldErrors[path] = issue.message;
      }
    }

    setErrors(fieldErrors);
    return null;
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    const parsed = validate();
    if (!parsed) {
      return;
    }

    setLoading(true);
    setAlert(null);

    try {
      const payload = {
        ...parsed,
        maxMembers: parsed.maxMembers === '' ? null : parsed.maxMembers,
        tags: parsed.tags
          .split(',')
          .map((tag) => tag.trim())
          .filter(Boolean),
        location: {
          ...parsed.location,
          region: parsed.location.region || null,
          postalCode: parsed.location.postalCode || null,
        },
      };

      const submitData = new FormData();
      submitData.append('group', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
      if (imageFile) {
        submitData.append('image', imageFile);
      }

      if (mode === 'create') {
        await api.postForm(ENDPOINTS.ADMIN_GROUP_CREATE, submitData);
        setAlert({ type: 'success', message: 'Group created successfully.' });
      } else if (groupId) {
        await api.putForm(ENDPOINTS.ADMIN_GROUP_UPDATE(groupId), submitData);
        setAlert({ type: 'success', message: 'Group updated successfully.' });
      }

      setTimeout(() => {
        router.push('/admin/groups');
      }, 1000);
    } catch (submitError) {
      console.error('Error saving group:', submitError);
      setAlert({
        type: 'error',
        message: submitError instanceof Error ? submitError.message : 'Failed to save group',
      });
    } finally {
      setLoading(false);
    }
  };

  const currentImage = imageFile
    ? URL.createObjectURL(imageFile)
    : initialGroup?.imagePath
      ? getImageUrl(initialGroup.imagePath)
      : undefined;

  return (
    <div className="space-y-6">
      <NavigationHeader
        title={mode === 'create' ? 'Create Group' : 'Edit Group'}
        subtitle={
          mode === 'create'
            ? 'Add a new connection group to the app'
            : initialGroup
              ? `Editing: ${initialGroup.name}`
              : 'Update group details'
        }
        showBackButton
        backButtonText="Back to Groups"
        backButtonHref="/admin/groups"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Groups', href: '/admin/groups' },
          { label: mode === 'create' ? 'Create Group' : 'Edit Group', current: true },
        ]}
      />

      {alert && (
        <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />
      )}

      <div className="max-w-4xl">
        <FormContainer onSubmit={handleSubmit}>
          <div className="space-y-6">
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <Input
                label="Group name"
                value={formData.name}
                onChange={(value) => setFormData((current) => ({ ...current, name: value }))}
                error={errors.name}
                placeholder="Sisterhood Lisboa Centro"
                required
                disabled={loading}
              />

              <MinistrySelect
                value={formData.ministry}
                onChange={(value) => setFormData((current) => ({ ...current, ministry: value }))}
                error={errors.ministry}
                disabled={loading}
              />
            </div>

            <Textarea
              label="Description"
              value={formData.description}
              onChange={(value) => setFormData((current) => ({ ...current, description: value }))}
              error={errors.description}
              placeholder="Describe the group, the atmosphere, and who it is for."
              rows={5}
              required
              disabled={loading}
              maxLength={2000}
              showCharCount
            />

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <Input
                label="Leader name"
                value={formData.leaderName}
                onChange={(value) => setFormData((current) => ({ ...current, leaderName: value }))}
                error={errors.leaderName}
                placeholder="Ana Silva"
                required
                disabled={loading}
              />

              <Input
                label="Leader contact / WhatsApp"
                value={formData.leaderContact}
                onChange={(value) => setFormData((current) => ({ ...current, leaderContact: value }))}
                error={errors.leaderContact}
                placeholder="+351912345678"
                required
                disabled={loading}
              />
            </div>

            <div className="grid gap-6 md:grid-cols-3">
              <SelectField
                label="Meeting day"
                value={formData.meetingDay}
                options={dayOptions.map((option) => ({ value: option.value, label: option.label }))}
                error={errors.meetingDay}
                disabled={loading}
                onChange={(value) =>
                  setFormData((current) => ({ ...current, meetingDay: value as GroupDayOfWeek }))
                }
              />

              <Input
                label="Meeting time"
                type="time"
                value={formData.meetingTime}
                onChange={(value) => setFormData((current) => ({ ...current, meetingTime: value }))}
                error={errors.meetingTime}
                required
                disabled={loading}
              />

              <SelectField
                label="Frequency"
                value={formData.frequency}
                options={frequencyOptions.map((option) => ({ value: option.value, label: option.label }))}
                error={errors.frequency}
                disabled={loading}
                onChange={(value) =>
                  setFormData((current) => ({ ...current, frequency: value as MeetingFrequency }))
                }
              />
            </div>

            <LocationPicker
              value={formData.location}
              onChange={(value) => setFormData((current) => ({ ...current, location: value }))}
              errors={{
                addressLine: errors['location.addressLine'],
                city: errors['location.city'],
                region: errors['location.region'],
                postalCode: errors['location.postalCode'],
                country: errors['location.country'],
                latitude: errors['location.latitude'],
                longitude: errors['location.longitude'],
              }}
              disabled={loading}
            />

            <div className="grid gap-6 md:grid-cols-2">
              <Input
                label="Max members"
                type="number"
                value={formData.maxMembers === '' ? '' : String(formData.maxMembers)}
                onChange={(value) =>
                  setFormData((current) => ({
                    ...current,
                    maxMembers: value === '' ? '' : Number(value),
                  }))
                }
                error={errors.maxMembers}
                placeholder="Optional"
                disabled={loading}
              />

              <Input
                label="Current members"
                type="number"
                value={String(formData.currentMembers)}
                onChange={(value) =>
                  setFormData((current) => ({ ...current, currentMembers: Number(value) || 0 }))
                }
                error={errors.currentMembers}
                disabled={loading}
              />
            </div>

            <Input
              label="Tags"
              value={formData.tags}
              onChange={(value) => setFormData((current) => ({ ...current, tags: value }))}
              error={errors.tags}
              placeholder="women, downtown, prayer"
              disabled={loading}
            />

            <ImageUpload
              label="Group image"
              value={currentImage}
              onChange={setImageFile}
              accept="image/*"
              disabled={loading}
            />

            <div className="grid gap-3 md:grid-cols-2">
              <Checkbox
                label="Visible in the mobile app"
                checked={formData.isActive}
                onChange={(checked) => setFormData((current) => ({ ...current, isActive: checked }))}
                disabled={loading}
              />
              <Checkbox
                label="Allow new people to join"
                checked={formData.isJoinable}
                onChange={(checked) => setFormData((current) => ({ ...current, isJoinable: checked }))}
                disabled={loading}
              />
            </div>

            <div className="flex gap-4 pt-4">
              <Button type="submit" variant="primary" loading={loading} disabled={loading}>
                {mode === 'create' ? 'Create Group' : 'Update Group'}
              </Button>
              <Button
                type="button"
                variant="secondary"
                onClick={() => router.push('/admin/groups')}
                disabled={loading}
              >
                Cancel
              </Button>
            </div>

            {mode === 'edit' && initialGroup && (
              <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                <p className="font-medium text-slate-900">Current schedule</p>
                <p className="mt-1">
                  {formatGroupDay(initialGroup.meetingDay)} at {initialGroup.meetingTime}
                </p>
              </div>
            )}
          </div>
        </FormContainer>
      </div>

      <LoadingOverlay
        show={loading}
        message={mode === 'create' ? 'Creating group...' : 'Updating group...'}
      />
    </div>
  );
}

function SelectField({
  label,
  value,
  options,
  error,
  disabled,
  onChange,
}: {
  label: string;
  value: string;
  options: Array<{ value: string; label: string }>;
  error?: string;
  disabled?: boolean;
  onChange: (value: string) => void;
}) {
  return (
    <label className="block space-y-1">
      <span className="text-sm font-medium text-gray-700">{label}</span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        disabled={disabled}
        className={`block w-full rounded-md border px-3 py-3 text-sm shadow-sm focus:outline-none focus:ring-2 ${
          error
            ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
            : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500'
        } ${disabled ? 'cursor-not-allowed bg-gray-100 text-gray-500' : 'bg-white'}`}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error && <span className="text-sm text-red-600">{error}</span>}
    </label>
  );
}

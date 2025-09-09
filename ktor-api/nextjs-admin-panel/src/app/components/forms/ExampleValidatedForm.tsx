'use client';

import React from 'react';
import { useFormValidation, validationSchemas } from '@/app/hooks/useFormValidation';
import { useApiErrorHandling } from '@/app/hooks/useErrorHandling';
import { FormContainer, FormField } from './index';
import { Button } from '../ui';
import { api } from '@/lib/api';

interface PostFormData {
  title: string;
  description: string;
  content: string;
  imageUrl?: string;
}

interface ExampleValidatedFormProps {
  initialData?: Partial<PostFormData>;
  onSuccess?: (data: PostFormData) => void;
  onCancel?: () => void;
}

const ExampleValidatedForm: React.FC<ExampleValidatedFormProps> = ({
  initialData,
  onSuccess,
  onCancel,
}) => {
  const { handleApiError } = useApiErrorHandling();
  
  const {
    control,
    handleSubmit,
    isSubmitting,
    submitError,
    submitSuccess,
    clearSubmitError,
    clearSubmitSuccess,
    reset,
    formState: { isValid, isDirty },
  } = useFormValidation(validationSchemas.post, {
    defaultValues: {
      title: initialData?.title || '',
      description: initialData?.description || '',
      content: initialData?.content || '',
      imageUrl: initialData?.imageUrl || '',
    },
  });

  const onSubmit = async (data: PostFormData) => {
    try {
      // Simulate API call
      const result = await api.post('/posts', data);
      console.log('Form submitted successfully:', result);
      onSuccess?.(data);
      
      // Reset form on success if creating new post
      if (!initialData) {
        reset();
      }
    } catch (error) {
      handleApiError(error, 'POST /posts');
      throw error; // Re-throw to trigger form error state
    }
  };

  return (
    <FormContainer
      onSubmit={handleSubmit(onSubmit)}
      isSubmitting={isSubmitting}
      submitError={submitError}
      submitSuccess={submitSuccess}
      successMessage={initialData ? 'Post updated successfully!' : 'Post created successfully!'}
      onClearError={clearSubmitError}
      onClearSuccess={clearSubmitSuccess}
    >
      <FormField
        control={control}
        name="title"
        type="input"
        label="Post Title"
        placeholder="Enter post title..."
        required
        successMessage="Great title!"
      />

      <FormField
        control={control}
        name="description"
        type="textarea"
        label="Description"
        placeholder="Enter post description..."
        rows={3}
        maxLength={500}
        showCharCount
        required
        successMessage="Description looks good!"
      />

      <FormField
        control={control}
        name="content"
        type="textarea"
        label="Content"
        placeholder="Enter post content..."
        rows={8}
        maxLength={10000}
        showCharCount
        required
      />

      <FormField
        control={control}
        name="imageUrl"
        type="input"
        inputType="url"
        label="Image URL (optional)"
        placeholder="https://example.com/image.jpg"
      />

      <div className="flex justify-end space-x-3 pt-4">
        {onCancel && (
          <Button
            type="button"
            variant="secondary"
            onClick={onCancel}
            disabled={isSubmitting}
          >
            Cancel
          </Button>
        )}
        <Button
          type="submit"
          variant="primary"
          loading={isSubmitting}
          disabled={!isValid || (!isDirty && !initialData)}
        >
          {isSubmitting 
            ? (initialData ? 'Updating...' : 'Creating...') 
            : (initialData ? 'Update Post' : 'Create Post')
          }
        </Button>
      </div>
    </FormContainer>
  );
};

export default ExampleValidatedForm;
'use client';

import React from 'react';
import { FormContainerProps } from '@/lib/types';
import { cn } from '@/lib/utils';
import Alert from '../ui/Alert';
import LoadingOverlay from '../ui/LoadingOverlay';

interface EnhancedFormContainerProps extends FormContainerProps {
  isSubmitting?: boolean;
  submitError?: string | null;
  submitSuccess?: boolean;
  successMessage?: string;
  onClearError?: () => void;
  onClearSuccess?: () => void;
  showLoadingOverlay?: boolean;
}

const FormContainer: React.FC<EnhancedFormContainerProps> = ({
  children,
  onSubmit,
  className,
  isSubmitting = false,
  submitError,
  submitSuccess = false,
  successMessage = 'Operation completed successfully!',
  onClearError,
  onClearSuccess,
  showLoadingOverlay = true,
}) => {
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (onSubmit && !isSubmitting) {
      onSubmit(e);
    }
  };

  return (
    <div className="relative">
      {/* Loading overlay */}
      {showLoadingOverlay && (
        <LoadingOverlay 
          show={isSubmitting} 
          message="Processing..." 
        />
      )}
      
      {/* Success message */}
      {submitSuccess && (
        <div className="mb-6">
          <Alert
            type="success"
            message={successMessage}
            onClose={onClearSuccess}
          />
        </div>
      )}
      
      {/* Error message */}
      {submitError && (
        <div className="mb-6">
          <Alert
            type="error"
            message={submitError}
            onClose={onClearError}
          />
        </div>
      )}
      
      <form
        onSubmit={handleSubmit}
        className={cn('space-y-6', className)}
        noValidate
      >
        <fieldset disabled={isSubmitting} className="space-y-6">
          {children}
        </fieldset>
      </form>
    </div>
  );
};

export default FormContainer;
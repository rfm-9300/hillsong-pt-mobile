/**
 * @jest-environment jsdom
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useFormValidation, validationSchemas } from '@/app/hooks/useFormValidation';
import { FormContainer, FormField } from '../index';
import { Button } from '../../ui';

// Mock component for testing
const TestForm: React.FC<{ onSubmit: (data: Record<string, unknown>) => Promise<void> }> = ({ onSubmit }) => {
  const {
    control,
    handleSubmit,
    isSubmitting,
    submitError,
    submitSuccess,
    clearSubmitError,
    clearSubmitSuccess,
    formState: { isValid },
  } = useFormValidation(validationSchemas.post);

  return (
    <FormContainer
      onSubmit={handleSubmit(onSubmit)}
      isSubmitting={isSubmitting}
      submitError={submitError}
      submitSuccess={submitSuccess}
      onClearError={clearSubmitError}
      onClearSuccess={clearSubmitSuccess}
    >
      <FormField
        control={control}
        name="title"
        type="input"
        label="Title"
        required
      />
      
      <FormField
        control={control}
        name="description"
        type="textarea"
        label="Description"
        required
      />
      
      <Button type="submit" disabled={!isValid}>
        Submit
      </Button>
    </FormContainer>
  );
};

describe('Form Validation', () => {
  it('should show validation errors for empty required fields', async () => {
    const mockSubmit = jest.fn();
    render(<TestForm onSubmit={mockSubmit} />);

    const submitButton = screen.getByRole('button', { name: /submit/i });
    
    // Button should be disabled initially
    expect(submitButton).toBeDisabled();

    // Try to submit without filling fields
    fireEvent.click(submitButton);
    
    // Should not call submit function
    expect(mockSubmit).not.toHaveBeenCalled();
  });

  it('should enable submit button when form is valid', async () => {
    const user = userEvent.setup();
    const mockSubmit = jest.fn().mockResolvedValue(undefined);
    
    render(<TestForm onSubmit={mockSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const descriptionInput = screen.getByLabelText(/description/i);
    const submitButton = screen.getByRole('button', { name: /submit/i });

    // Fill in required fields
    await user.type(titleInput, 'Test Title');
    await user.type(descriptionInput, 'Test Description');

    // Wait for validation
    await waitFor(() => {
      expect(submitButton).not.toBeDisabled();
    });
  });

  it('should show real-time validation feedback', async () => {
    const user = userEvent.setup();
    const mockSubmit = jest.fn();
    
    render(<TestForm onSubmit={mockSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);

    // Type a very long title to trigger validation
    const longTitle = 'a'.repeat(201); // Exceeds 200 character limit
    await user.type(titleInput, longTitle);

    // Should show validation error
    await waitFor(() => {
      expect(screen.getByText(/title must be less than 200 characters/i)).toBeInTheDocument();
    });
  });

  it('should handle form submission errors', async () => {
    const user = userEvent.setup();
    const mockSubmit = jest.fn().mockRejectedValue(new Error('Submission failed'));
    
    render(<TestForm onSubmit={mockSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const descriptionInput = screen.getByLabelText(/description/i);
    const submitButton = screen.getByRole('button', { name: /submit/i });

    // Fill in valid data
    await user.type(titleInput, 'Valid Title');
    await user.type(descriptionInput, 'Valid Description');

    // Submit form
    await user.click(submitButton);

    // Should show error message
    await waitFor(() => {
      expect(screen.getByText(/submission failed/i)).toBeInTheDocument();
    });

    expect(mockSubmit).toHaveBeenCalledWith({
      title: 'Valid Title',
      description: 'Valid Description',
      content: '',
      imageUrl: '',
    });
  });

  it('should show success message on successful submission', async () => {
    const user = userEvent.setup();
    const mockSubmit = jest.fn().mockResolvedValue(undefined);
    
    render(<TestForm onSubmit={mockSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const descriptionInput = screen.getByLabelText(/description/i);
    const submitButton = screen.getByRole('button', { name: /submit/i });

    // Fill in valid data
    await user.type(titleInput, 'Valid Title');
    await user.type(descriptionInput, 'Valid Description');

    // Submit form
    await user.click(submitButton);

    // Should show success message
    await waitFor(() => {
      expect(screen.getByText(/operation completed successfully/i)).toBeInTheDocument();
    });
  });
});

export {};
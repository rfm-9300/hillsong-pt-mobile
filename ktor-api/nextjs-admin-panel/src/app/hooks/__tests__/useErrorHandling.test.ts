/**
 * @jest-environment jsdom
 */

import { renderHook, act } from '@testing-library/react';
import { useErrorHandling, useApiErrorHandling } from '../useErrorHandling';

describe('useErrorHandling', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('should handle errors correctly', () => {
    const onError = jest.fn();
    const { result } = renderHook(() => useErrorHandling({ onError }));

    const testError = new Error('Test error');

    act(() => {
      result.current.handleError(testError);
    });

    expect(result.current.hasError).toBe(true);
    expect(result.current.error).toEqual(
      expect.objectContaining({
        message: 'Test error',
        retryable: true,
      })
    );
    expect(onError).toHaveBeenCalledWith(
      expect.objectContaining({
        message: 'Test error',
      })
    );
  });

  it('should handle string errors', () => {
    const { result } = renderHook(() => useErrorHandling());

    act(() => {
      result.current.handleError('String error message');
    });

    expect(result.current.error?.message).toBe('String error message');
  });

  it('should handle unknown errors', () => {
    const { result } = renderHook(() => useErrorHandling());

    act(() => {
      result.current.handleError({ unknown: 'object' });
    });

    expect(result.current.error?.message).toBe('An unknown error occurred');
  });

  it('should add context to error messages', () => {
    const { result } = renderHook(() => useErrorHandling());

    act(() => {
      result.current.handleError(new Error('Original error'), 'Test Context');
    });

    expect(result.current.error?.message).toBe('Test Context: Original error');
  });

  it('should maintain error history', () => {
    const { result } = renderHook(() => useErrorHandling());

    act(() => {
      result.current.handleError(new Error('First error'));
    });

    act(() => {
      result.current.handleError(new Error('Second error'));
    });

    expect(result.current.errorHistory).toHaveLength(2);
    expect(result.current.errorHistory[0].message).toBe('Second error');
    expect(result.current.errorHistory[1].message).toBe('First error');
  });

  it('should clear errors', () => {
    const { result } = renderHook(() => useErrorHandling());

    act(() => {
      result.current.handleError(new Error('Test error'));
    });

    expect(result.current.hasError).toBe(true);

    act(() => {
      result.current.clearError();
    });

    expect(result.current.hasError).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('should determine retryable errors correctly', () => {
    const { result } = renderHook(() => useErrorHandling());

    // Retryable error
    act(() => {
      result.current.handleError(new Error('Network timeout'));
    });
    expect(result.current.error?.retryable).toBe(true);

    // Non-retryable error
    act(() => {
      result.current.handleError(new Error('Authentication required'));
    });
    expect(result.current.error?.retryable).toBe(false);
  });
});

describe('useApiErrorHandling', () => {
  beforeEach(() => {
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('should transform authentication errors', () => {
    const { result } = renderHook(() => useApiErrorHandling());

    act(() => {
      result.current.handleApiError(new Error('Authentication required'), '/api/test');
    });

    expect(result.current.error?.message).toBe('API Error (/api/test): Please log in to continue');
  });

  it('should transform access denied errors', () => {
    const { result } = renderHook(() => useApiErrorHandling());

    act(() => {
      result.current.handleApiError(new Error('Access denied'));
    });

    expect(result.current.error?.message).toBe('API Error: You do not have permission to perform this action');
  });

  it('should transform not found errors', () => {
    const { result } = renderHook(() => useApiErrorHandling());

    act(() => {
      result.current.handleApiError(new Error('Not found'));
    });

    expect(result.current.error?.message).toBe('API Error: The requested resource was not found');
  });

  it('should transform network errors', () => {
    const { result } = renderHook(() => useApiErrorHandling());

    act(() => {
      result.current.handleApiError(new Error('Network error: Failed to fetch'));
    });

    expect(result.current.error?.message).toBe('API Error: Unable to connect to the server. Please check your internet connection');
  });

  it('should transform timeout errors', () => {
    const { result } = renderHook(() => useApiErrorHandling());

    act(() => {
      result.current.handleApiError(new Error('Request timed out'));
    });

    expect(result.current.error?.message).toBe('API Error: The request timed out. Please try again');
  });

  it('should pass through unknown errors', () => {
    const { result } = renderHook(() => useApiErrorHandling());

    act(() => {
      result.current.handleApiError(new Error('Unknown API error'));
    });

    expect(result.current.error?.message).toBe('API Error: Unknown API error');
  });
});

export {};
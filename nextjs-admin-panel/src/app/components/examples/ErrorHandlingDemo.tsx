'use client';

import React, { useState } from 'react';
import { useEnhancedApiCall } from '@/app/hooks/useApiCall';
import { enhancedApiService } from '@/lib/enhancedApiService';
import { 
  Button, 
  Card, 
  PageHeader, 
  LoadingOverlay, 
  RetryButton,
  Alert,
} from '@/app/components/ui';

// Mock API functions for demonstration
const mockSuccessfulApi = async (): Promise<{ message: string; data: number[] }> => {
  await new Promise(resolve => setTimeout(resolve, 2000));
  return { message: 'Success!', data: [1, 2, 3, 4, 5] };
};

const mockFailingApi = async (): Promise<never> => {
  await new Promise(resolve => setTimeout(resolve, 1500));
  throw new Error('This API call always fails for demonstration purposes');
};

const mockTimeoutApi = async (): Promise<{ message: string }> => {
  await new Promise(resolve => setTimeout(resolve, 10000)); // 10 second delay
  return { message: 'This should timeout' };
};

const mockProgressApi = async (updateProgress?: (progress: number, message?: string) => void): Promise<{ message: string }> => {
  for (let i = 0; i <= 100; i += 10) {
    await new Promise(resolve => setTimeout(resolve, 200));
    updateProgress?.(i, `Processing step ${i / 10 + 1}/11...`);
  }
  return { message: 'Progress API completed!' };
};

const ErrorHandlingDemo: React.FC = () => {
  const [demoResults, setDemoResults] = useState<Record<string, unknown>>({});

  // Enhanced API call hooks
  const successfulCall = useEnhancedApiCall(mockSuccessfulApi, {
    loadingKey: 'successful_demo',
    context: 'Successful API Demo',
    message: 'Loading successful demo...',
    onSuccess: (data) => {
      setDemoResults(prev => ({ ...prev, successful: data }));
    },
  });

  const failingCall = useEnhancedApiCall(mockFailingApi, {
    loadingKey: 'failing_demo',
    context: 'Failing API Demo',
    message: 'Attempting failing demo...',
    retries: 2,
    retryDelay: 1000,
  });

  const timeoutCall = useEnhancedApiCall(mockTimeoutApi, {
    loadingKey: 'timeout_demo',
    context: 'Timeout API Demo',
    message: 'Testing timeout handling...',
    timeout: 5000, // 5 second timeout
  });

  const progressCall = useEnhancedApiCall(
    (updateProgress?: (progress: number, message?: string) => void) => mockProgressApi(updateProgress),
    {
      loadingKey: 'progress_demo',
      context: 'Progress API Demo',
      message: 'Demonstrating progress tracking...',
      showProgress: true,
      onSuccess: (data) => {
        setDemoResults(prev => ({ ...prev, progress: data }));
      },
    }
  );

  // Direct enhanced API service calls
  const handleDirectApiCall = async (type: 'success' | 'error' | 'batch') => {
    try {
      switch (type) {
        case 'success':
          const result = await enhancedApiService.get('/api/demo/success', {
            loadingKey: 'direct_success',
            message: 'Direct API success call...',
            context: 'Direct API Success Demo',
          });
          setDemoResults(prev => ({ ...prev, directSuccess: result }));
          break;

        case 'error':
          await enhancedApiService.get('/api/demo/error', {
            loadingKey: 'direct_error',
            message: 'Direct API error call...',
            context: 'Direct API Error Demo',
          });
          break;

        case 'batch':
          const batchResult = await enhancedApiService.batch({
            posts: () => enhancedApiService.get('/api/posts'),
            users: () => enhancedApiService.get('/api/users'),
            events: () => enhancedApiService.get('/api/events'),
          }, {
            batchLoadingKey: 'batch_demo',
            message: 'Loading batch data...',
            context: 'Batch API Demo',
            showProgress: true,
          });
          setDemoResults(prev => ({ ...prev, batch: batchResult }));
          break;
      }
    } catch (error) {
      console.error('Direct API call error:', error);
    }
  };

  return (
    <div className="p-6 space-y-6">
      <PageHeader
        title="Error Handling & Loading States Demo"
        subtitle="Comprehensive demonstration of enhanced error handling and loading state management"
      />

      {/* Hook-based API Calls */}
      <Card className="p-6">
        <h3 className="text-lg font-semibold mb-4">Hook-based API Calls</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          
          {/* Successful API Call */}
          <div className="space-y-3">
            <h4 className="font-medium text-green-700">Successful Call</h4>
            <Button
              onClick={() => successfulCall.execute()}
              loading={successfulCall.loading}
              disabled={successfulCall.loading}
              variant="primary"
              className="w-full"
            >
              Execute Success
            </Button>
            {successfulCall.error && (
              <Alert type="error" message={successfulCall.error.message} />
            )}
            {!!demoResults.successful && (
              <div className="text-sm text-green-600">
                ✓ Success: {(demoResults.successful as { message: string }).message}
              </div>
            )}
          </div>

          {/* Failing API Call */}
          <div className="space-y-3">
            <h4 className="font-medium text-red-700">Failing Call (with Retry)</h4>
            <Button
              onClick={() => failingCall.execute()}
              loading={failingCall.loading}
              disabled={failingCall.loading}
              variant="danger"
              className="w-full"
            >
              Execute Failure
            </Button>
            {failingCall.error && (
              <div className="space-y-2">
                <Alert type="error" message={failingCall.error.message} />
                {failingCall.canRetry && (
                  <RetryButton
                    onRetry={() => { failingCall.retry(); }}
                    size="sm"
                    className="w-full"
                  >
                    Manual Retry
                  </RetryButton>
                )}
              </div>
            )}
            {failingCall.isRetrying && (
              <div className="text-sm text-orange-600">
                Retrying... (Attempt {failingCall.retryAttempt})
              </div>
            )}
          </div>

          {/* Timeout API Call */}
          <div className="space-y-3">
            <h4 className="font-medium text-yellow-700">Timeout Call</h4>
            <Button
              onClick={() => timeoutCall.execute()}
              loading={timeoutCall.loading}
              disabled={timeoutCall.loading}
              variant="secondary"
              className="w-full"
            >
              Execute Timeout
            </Button>
            {timeoutCall.error && (
              <Alert type="warning" message="Request timed out after 5 seconds" />
            )}
          </div>

          {/* Progress API Call */}
          <div className="space-y-3">
            <h4 className="font-medium text-blue-700">Progress Call</h4>
            <Button
              onClick={() => progressCall.execute()}
              loading={progressCall.loading}
              disabled={progressCall.loading}
              variant="primary"
              className="w-full"
            >
              Execute Progress
            </Button>
            {progressCall.progress !== undefined && (
              <div className="space-y-1">
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${progressCall.progress}%` }}
                  />
                </div>
                <div className="text-xs text-gray-600">{progressCall.progress}%</div>
              </div>
            )}
            {!!demoResults.progress && (
              <div className="text-sm text-blue-600">
                ✓ Progress: {(demoResults.progress as { message: string }).message}
              </div>
            )}
          </div>
        </div>
      </Card>

      {/* Direct API Service Calls */}
      <Card className="p-6">
        <h3 className="text-lg font-semibold mb-4">Direct API Service Calls</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          
          <div className="space-y-3">
            <h4 className="font-medium">Direct Success Call</h4>
            <Button
              onClick={() => handleDirectApiCall('success')}
              variant="primary"
              className="w-full"
            >
              Direct Success
            </Button>
            {!!demoResults.directSuccess && (
              <div className="text-sm text-green-600">
                ✓ Direct success completed
              </div>
            )}
          </div>

          <div className="space-y-3">
            <h4 className="font-medium">Direct Error Call</h4>
            <Button
              onClick={() => handleDirectApiCall('error')}
              variant="danger"
              className="w-full"
            >
              Direct Error
            </Button>
          </div>

          <div className="space-y-3">
            <h4 className="font-medium">Batch API Call</h4>
            <Button
              onClick={() => handleDirectApiCall('batch')}
              variant="secondary"
              className="w-full"
            >
              Batch Request
            </Button>
            {!!demoResults.batch && (
              <div className="text-sm text-blue-600">
                ✓ Batch completed: {Object.keys((demoResults.batch as { data: Record<string, unknown> }).data).length} successful
              </div>
            )}
          </div>
        </div>
      </Card>

      {/* Loading Overlay Demo */}
      <Card className="p-6">
        <h3 className="text-lg font-semibold mb-4">Loading Overlay Demos</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          
          <div className="space-y-3">
            <h4 className="font-medium">Inline Loading</h4>
            <div className="relative min-h-[100px] border-2 border-dashed border-gray-300 rounded-lg">
              <LoadingOverlay
                show={successfulCall.loading}
                type="inline"
                message="Loading inline demo..."
              />
              {!successfulCall.loading && (
                <div className="p-4 text-center text-gray-500">
                  Content area
                </div>
              )}
            </div>
          </div>

          <div className="space-y-3">
            <h4 className="font-medium">Overlay Loading</h4>
            <div className="relative min-h-[100px] border-2 border-dashed border-gray-300 rounded-lg">
              <LoadingOverlay
                show={failingCall.loading}
                type="overlay"
                message="Loading overlay demo..."
              />
              <div className="p-4 text-center text-gray-500">
                Content behind overlay
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <h4 className="font-medium">Progress Loading</h4>
            <div className="relative min-h-[100px] border-2 border-dashed border-gray-300 rounded-lg">
              <LoadingOverlay
                show={progressCall.loading}
                type="overlay"
                message="Progress demo..."
                progress={progressCall.progress}
                showProgress={true}
              />
              <div className="p-4 text-center text-gray-500">
                Progress content
              </div>
            </div>
          </div>
        </div>
      </Card>

      {/* Reset Demo */}
      <Card className="p-6">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold">Demo Controls</h3>
            <p className="text-sm text-gray-600">Reset all demo states and results</p>
          </div>
          <Button
            onClick={() => {
              setDemoResults({});
              successfulCall.reset();
              failingCall.reset();
              timeoutCall.reset();
              progressCall.reset();
            }}
            variant="secondary"
          >
            Reset All
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default ErrorHandlingDemo;
import { Modal, Button } from './';

interface DeleteConfirmationModalProps {
  show: boolean;
  title?: string;
  message?: string;
  onConfirm: () => void;
  onCancel: () => void;
  loading?: boolean;
}

export default function DeleteConfirmationModal({
  show,
  title = 'Confirm Deletion',
  message = 'Are you sure you want to delete this item? This action cannot be undone.',
  onConfirm,
  onCancel,
  loading = false
}: DeleteConfirmationModalProps) {
  return (
    <Modal show={show} title={title} size="sm" onClose={onCancel}>
      <div className="space-y-4">
        <p className="text-gray-700">{message}</p>
        
        <div className="flex justify-end gap-3 pt-4">
          <Button
            variant="secondary"
            onClick={onCancel}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={onConfirm}
            loading={loading}
            disabled={loading}
          >
            {loading ? 'Deleting...' : 'Delete'}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
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
  loading = false,
}: DeleteConfirmationModalProps) {
  return (
    <Modal show={show} title={title} size="sm" onClose={onCancel}>
      <p className="text-[13px] leading-[1.5] text-[var(--color-text-sub)]">{message}</p>
      <div className="mt-5 flex justify-end gap-2">
        <Button variant="secondary" size="sm" onClick={onCancel} disabled={loading}>Cancel</Button>
        <Button variant="danger" size="sm" onClick={onConfirm} loading={loading} disabled={loading}>Delete</Button>
      </div>
    </Modal>
  );
}

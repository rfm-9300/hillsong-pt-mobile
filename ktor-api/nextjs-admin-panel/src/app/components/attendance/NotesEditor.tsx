'use client';

import React, { useState, useEffect } from 'react';
import { Button } from '@/app/components/ui';
import { Textarea } from '@/app/components/forms';
import { cn } from '@/lib/utils';

interface NotesEditorProps {
  initialNotes: string;
  onSave: (notes: string) => void;
  onCancel: () => void;
  placeholder?: string;
  maxLength?: number;
  className?: string;
  disabled?: boolean;
}

export const NotesEditor: React.FC<NotesEditorProps> = ({
  initialNotes,
  onSave,
  onCancel,
  placeholder = 'Add notes about this attendance record...',
  maxLength = 500,
  className,
  disabled = false,
}) => {
  const [notes, setNotes] = useState(initialNotes);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setNotes(initialNotes);
  }, [initialNotes]);

  const handleSave = async () => {
    if (disabled || isSaving) return;
    
    setIsSaving(true);
    try {
      await onSave(notes.trim());
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    if (disabled || isSaving) return;
    setNotes(initialNotes);
    onCancel();
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Escape') {
      handleCancel();
    } else if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      e.preventDefault();
      handleSave();
    }
  };

  const remainingChars = maxLength - notes.length;
  const isOverLimit = remainingChars < 0;

  return (
    <div className={cn('space-y-3', className)}>
      <div className="relative">
        <Textarea
          value={notes}
          onChange={setNotes}
          placeholder={placeholder}
          rows={4}
          disabled={disabled || isSaving}
          className={cn(
            'resize-none',
            isOverLimit && 'border-red-300 focus:border-red-500 focus:ring-red-500'
          )}
          onKeyDown={handleKeyDown}
        />
        
        {/* Character Counter */}
        <div className="absolute bottom-2 right-2 text-xs text-gray-400">
          <span className={cn(
            isOverLimit && 'text-red-500 font-medium'
          )}>
            {notes.length}/{maxLength}
          </span>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex items-center justify-end space-x-2">
        <Button
          variant="ghost"
          size="sm"
          onClick={handleCancel}
          disabled={disabled || isSaving}
        >
          Cancel
        </Button>
        <Button
          variant="primary"
          size="sm"
          onClick={handleSave}
          disabled={disabled || isSaving || isOverLimit}
          loading={isSaving}
        >
          {isSaving ? 'Saving...' : 'Save'}
        </Button>
      </div>

      {/* Keyboard Shortcuts Help */}
      <div className="text-xs text-gray-400 text-center">
        Press <kbd className="px-1 py-0.5 bg-gray-100 rounded text-xs">Esc</kbd> to cancel, 
        <kbd className="px-1 py-0.5 bg-gray-100 rounded text-xs ml-1">Ctrl+Enter</kbd> to save
      </div>
    </div>
  );
};

export default NotesEditor;
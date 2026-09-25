import { X } from "lucide-react";
import ResumeUpload from "./ResumeUpload";

export default function ResumeUploadModal({ isOpen, onClose, onUpload, uploading, successMessage }) {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal-card" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "560px" }}>
        <div className="modal-header">
          <h3 className="modal-title" style={{ margin: 0 }}>
            Upload New Resume
          </h3>
          <button className="modal-close-btn" onClick={onClose} aria-label="Close dialog">
            <X size={18} />
          </button>
        </div>

        <div className="modal-body">
          <ResumeUpload
            onUpload={async (file) => {
              const res = await onUpload(file);
              if (res) {
                setTimeout(() => {
                  onClose();
                }, 1000);
              }
            }}
            uploading={uploading}
            successMessage={successMessage}
          />
        </div>

        <div className="modal-footer">
          <button type="button" className="secondary-button" onClick={onClose} disabled={uploading}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}

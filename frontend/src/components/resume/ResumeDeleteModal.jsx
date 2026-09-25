import { AlertTriangle, Trash2, X } from "lucide-react";

export default function ResumeDeleteModal({ isOpen, onClose, onConfirm, resume, deleting = false }) {
  if (!isOpen || !resume) return null;

  return (
    <div className="modal-overlay" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal-card" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "460px" }}>
        <div className="modal-header">
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <AlertTriangle size={20} color="var(--danger)" />
            <h3 className="modal-title" style={{ margin: 0 }}>
              Remove Resume?
            </h3>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Close dialog">
            <X size={18} />
          </button>
        </div>

        <div className="modal-body">
          <p style={{ fontSize: "14px", color: "var(--text-muted)", margin: "0 0 12px 0", lineHeight: 1.5 }}>
            Are you sure you want to remove <strong>{resume.fileName}</strong> from your active stored resumes?
          </p>
          <div style={{
            background: "var(--warning-bg)",
            border: "1px solid var(--warning-border)",
            borderRadius: "var(--radius-md)",
            padding: "12px 14px",
            fontSize: "13px",
            color: "var(--warning-dark)",
          }}>
            Uploading a new resume will automatically overwrite the active file and re-calculate your ATS metrics.
          </div>
        </div>

        <div className="modal-footer">
          <button type="button" className="secondary-button" onClick={onClose} disabled={deleting}>
            Cancel
          </button>
          <button
            type="button"
            className="secondary-button"
            onClick={onConfirm}
            disabled={deleting}
            style={{
              background: "var(--danger)",
              color: "#FFFFFF",
              borderColor: "var(--danger)",
            }}
          >
            <Trash2 size={15} />
            <span>{deleting ? "Removing..." : "Remove Resume"}</span>
          </button>
        </div>
      </div>
    </div>
  );
}

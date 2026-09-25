import { useState, useEffect } from "react";
import { X, Download, FileText, RefreshCw, AlertCircle } from "lucide-react";
import { api } from "../../services/api";

export default function ResumePreviewModal({ isOpen, onClose, resume, onDownload }) {
  const [pdfBlobUrl, setPdfBlobUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let objectUrl = null;
    if (isOpen && resume) {
      setLoading(true);
      setError("");
      api
        .downloadMyResume()
        .then((blob) => {
          objectUrl = URL.createObjectURL(blob);
          setPdfBlobUrl(objectUrl);
        })
        .catch((err) => {
          console.error("Preview load error:", err);
          setError("Unable to load embedded preview. You can still download the file directly.");
        })
        .finally(() => {
          setLoading(false);
        });
    }

    return () => {
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [isOpen, resume]);

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose} role="dialog" aria-modal="true">
      <div
        className="modal-card modal-large"
        onClick={(e) => e.stopPropagation()}
        style={{ height: "85vh", display: "flex", flexDirection: "column" }}
      >
        <div className="modal-header">
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <FileText size={20} color="var(--primary-600)" />
            <h3 className="modal-title" style={{ margin: 0 }}>
              {resume?.fileName || "Resume Preview"}
            </h3>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Close dialog">
            <X size={18} />
          </button>
        </div>

        <div className="modal-body" style={{ flex: 1, padding: 0, background: "#334155", position: "relative" }}>
          {loading ? (
            <div style={{ height: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", color: "#FFFFFF", gap: "12px" }}>
              <RefreshCw size={32} className="spinning" />
              <p style={{ fontSize: "14px", margin: 0 }}>Loading PDF document...</p>
            </div>
          ) : error ? (
            <div style={{ height: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", color: "#FFFFFF", padding: "24px", textAlign: "center", gap: "12px" }}>
              <AlertCircle size={36} color="#F87171" />
              <p style={{ fontSize: "14px", maxWidth: "400px", margin: 0 }}>{error}</p>
              <button
                type="button"
                className="primary-button"
                onClick={() => {
                  onDownload();
                  onClose();
                }}
              >
                <Download size={15} />
                <span>Download Resume Directly</span>
              </button>
            </div>
          ) : pdfBlobUrl ? (
            <iframe
              src={pdfBlobUrl}
              title="Resume Preview"
              width="100%"
              height="100%"
              style={{ border: "none" }}
            />
          ) : null}
        </div>

        <div className="modal-footer">
          <button type="button" className="secondary-button" onClick={onClose}>
            Close
          </button>
          <button
            type="button"
            className="primary-button"
            onClick={() => {
              onDownload();
              onClose();
            }}
          >
            <Download size={15} />
            <span>Download PDF</span>
          </button>
        </div>
      </div>
    </div>
  );
}

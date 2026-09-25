import { useState } from "react";
import { UploadCloud, RefreshCw, FileText, CheckCircle2 } from "lucide-react";

export default function ResumeUpload({ onUpload, uploading, successMessage }) {
  const [isDragging, setIsDragging] = useState(false);

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      onUpload(e.dataTransfer.files[0]);
    }
  };

  const handleFileInput = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      onUpload(e.target.files[0]);
    }
  };

  return (
    <div className="resume-dropzone-card">
      <div style={{ marginBottom: "16px" }}>
        <h3 style={{ fontSize: "16px", fontWeight: 700, color: "var(--text-heading)", margin: "0 0 4px 0" }}>
          Upload &amp; Analyze Resume
        </h3>
        <p style={{ fontSize: "13px", color: "var(--text-muted)", margin: 0 }}>
          Upload your latest PDF resume to parse skills, compute ATS score, and receive AI optimizations.
        </p>
      </div>

      <label
        className={`dropzone-label ${uploading ? "uploading" : ""} ${isDragging ? "drag-over" : ""}`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        <input
          type="file"
          accept="application/pdf"
          onChange={handleFileInput}
          disabled={uploading}
          style={{ display: "none" }}
        />
        {uploading ? (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "8px" }}>
            <RefreshCw size={36} className="spinning" color="var(--primary-600)" style={{ margin: "0 auto 8px auto" }} />
            <h4 className="dropzone-title">Analyzing Resume with AI...</h4>
            <p className="dropzone-subtitle" style={{ maxWidth: "320px", margin: "0 auto" }}>
              Extracting document structure, benchmarking keywords, and generating ATS insights.
            </p>
          </div>
        ) : (
          <div>
            <div className="dropzone-icon-circle">
              <UploadCloud size={28} />
            </div>
            <h4 className="dropzone-title">Drag &amp; drop your resume here</h4>
            <p className="dropzone-subtitle">Supported format: PDF up to 5 MB</p>
            <span className="browse-pill-btn">
              <FileText size={14} />
              <span>Browse Files</span>
            </span>
          </div>
        )}
      </label>

      {successMessage && !uploading && (
        <div style={{
          marginTop: "14px",
          padding: "10px 14px",
          background: "var(--success-bg)",
          border: "1px solid var(--success-border)",
          borderRadius: "var(--radius-md)",
          display: "flex",
          alignItems: "center",
          gap: "8px",
          color: "var(--success-dark)",
          fontSize: "13px",
          fontWeight: 600,
        }}>
          <CheckCircle2 size={16} color="var(--success)" />
          <span>{successMessage}</span>
        </div>
      )}
    </div>
  );
}

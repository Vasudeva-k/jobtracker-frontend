import { FileText, Download, Eye, RefreshCw, Trash2, Calendar, HardDrive, CheckCircle2 } from "lucide-react";

export default function ResumeCard({
  resume,
  onDownload,
  onPreview,
  onReupload,
  onDelete,
  downloading = false,
  atsScore = null,
}) {
  if (!resume) return null;

  const formattedSize = resume.fileSize
    ? `${(resume.fileSize / 1024).toFixed(1)} KB`
    : "PDF Document";

  const formattedDate = resume.uploadedAt
    ? new Date(resume.uploadedAt).toLocaleDateString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric",
      })
    : "Recently";

  return (
    <div className="stored-resume-card">
      <div className="stored-resume-header">
        <div className="pdf-icon-badge-box">
          <FileText size={24} />
        </div>
        <div className="stored-resume-title-wrap">
          <span className="stored-resume-label">Active Stored Resume</span>
          <strong className="stored-resume-name" title={resume.fileName}>
            {resume.fileName || "Resume.pdf"}
          </strong>
        </div>
        <span
          className="status-pill offered"
          style={{ fontSize: "11px", padding: "3px 8px", display: "inline-flex", alignItems: "center", gap: "4px" }}
        >
          <CheckCircle2 size={12} />
          <span>Active</span>
        </span>
      </div>

      <div className="stored-resume-meta">
        <div className="meta-tag">
          <HardDrive size={14} />
          <span>Size: <strong>{formattedSize}</strong></span>
        </div>
        <div className="meta-tag">
          <Calendar size={14} />
          <span>Uploaded: <strong>{formattedDate}</strong></span>
        </div>
        {atsScore !== null && atsScore !== undefined && (
          <div className="meta-tag">
            <span>ATS Score: <strong style={{ color: "var(--primary-700)" }}>{atsScore}%</strong></span>
          </div>
        )}
      </div>

      <div className="stored-resume-actions">
        {onPreview && (
          <button
            type="button"
            className="secondary-button"
            style={{ flex: 1, padding: "8px 12px", fontSize: "13px" }}
            onClick={() => onPreview(resume)}
          >
            <Eye size={15} />
            <span>Preview</span>
          </button>
        )}

        <button
          type="button"
          className="primary-button"
          style={{ flex: 1.2, padding: "8px 12px", fontSize: "13px" }}
          onClick={onDownload}
          disabled={downloading}
        >
          {downloading ? (
            <>
              <RefreshCw size={15} className="spinning" />
              <span>Downloading...</span>
            </>
          ) : (
            <>
              <Download size={15} />
              <span>Download PDF</span>
            </>
          )}
        </button>

        {onReupload && (
          <button
            type="button"
            className="secondary-button icon-only"
            title="Upload replacement resume"
            onClick={onReupload}
            style={{ padding: "8px 10px" }}
          >
            <RefreshCw size={15} />
          </button>
        )}

        {onDelete && (
          <button
            type="button"
            className="secondary-button icon-only"
            title="Delete resume"
            onClick={() => onDelete(resume)}
            style={{ padding: "8px 10px", color: "var(--danger)" }}
          >
            <Trash2 size={15} />
          </button>
        )}
      </div>
    </div>
  );
}

import { useState, useMemo } from "react";
import { Search, ArrowUpDown, UploadCloud, FileText, Wand2 } from "lucide-react";
import ResumeCard from "./ResumeCard";

export default function ResumeManager({
  resumeInfo,
  loading,
  downloading,
  analysisResult,
  onDownload,
  onPreview,
  onOpenUploadModal,
  onDelete,
  onNavigateTab,
}) {
  const [searchTerm, setSearchTerm] = useState("");
  const [sortBy, setSortBy] = useState("date-desc"); // date-desc | date-asc | name-asc | size-desc

  const resumesList = useMemo(() => {
    if (!resumeInfo) return [];
    const list = [resumeInfo];
    return list.filter((r) =>
      r.fileName ? r.fileName.toLowerCase().includes(searchTerm.toLowerCase()) : true
    );
  }, [resumeInfo, searchTerm]);

  return (
    <div className="resume-hub-container">
      {/* Header and Toolbar */}
      <div className="manager-toolbar">
        <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap", flex: 1 }}>
          <div className="manager-search-box">
            <Search size={16} color="var(--text-dim)" />
            <input
              type="text"
              placeholder="Search resumes by name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
            <ArrowUpDown size={15} color="var(--text-dim)" />
            <select
              className="manager-sort-select"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              aria-label="Sort resumes"
            >
              <option value="date-desc">Newest First</option>
              <option value="date-asc">Oldest First</option>
              <option value="name-asc">File Name (A-Z)</option>
              <option value="size-desc">Largest File Size</option>
            </select>
          </div>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <button
            type="button"
            className="secondary-button"
            onClick={() => onNavigateTab("builder")}
            style={{ fontSize: "13px", padding: "7px 14px" }}
          >
            <Wand2 size={15} />
            <span>Open Builder</span>
          </button>
          <button
            type="button"
            className="primary-button"
            onClick={onOpenUploadModal}
            style={{ fontSize: "13px", padding: "7px 14px" }}
          >
            <UploadCloud size={15} />
            <span>Upload Resume</span>
          </button>
        </div>
      </div>

      {/* Main Content: Cards or Table */}
      {loading ? (
        <div className="resume-cards-list">
          <div className="stored-resume-card">
            <div className="skeleton-box" style={{ height: "46px", width: "100%" }} />
            <div className="skeleton-box" style={{ height: "40px", width: "100%", margin: "10px 0" }} />
            <div className="skeleton-box" style={{ height: "36px", width: "100%" }} />
          </div>
        </div>
      ) : resumesList.length > 0 ? (
        <div className="resume-cards-list">
          {resumesList.map((res, index) => (
            <ResumeCard
              key={res.id || index}
              resume={res}
              onDownload={onDownload}
              onPreview={onPreview}
              onReupload={onOpenUploadModal}
              onDelete={onDelete}
              downloading={downloading}
              atsScore={analysisResult?.atsScore}
            />
          ))}
        </div>
      ) : (
        <div className="resume-empty-state" style={{ padding: "64px 24px" }}>
          <div className="resume-empty-icon" style={{ width: "68px", height: "68px" }}>
            <FileText size={32} />
          </div>
          <h4 className="resume-empty-title" style={{ fontSize: "18px" }}>No Resumes Uploaded Yet</h4>
          <p className="resume-empty-desc">
            Upload your resume to organize, inspect ATS scores, preview documents, and optimize for target job applications.
          </p>
          <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
            <button
              type="button"
              className="primary-button"
              onClick={onOpenUploadModal}
            >
              <UploadCloud size={16} />
              <span>Upload Your First Resume</span>
            </button>
            <button
              type="button"
              className="secondary-button"
              onClick={() => onNavigateTab("builder")}
            >
              <Wand2 size={16} />
              <span>Create with PDF Builder</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

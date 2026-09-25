import { FileText, Sparkles, Clock, CheckCircle2, Wand2 } from "lucide-react";
import ResumeUpload from "./ResumeUpload";
import ResumeAnalysisCard from "./ResumeAnalysisCard";
import ResumeCard from "./ResumeCard";

export default function ResumeOverview({
  resumeInfo,
  analysisResult,
  loading,
  uploading,
  downloading,
  successMessage,
  onUpload,
  onDownload,
  onPreview,
  onDelete,
  onNavigateTab,
}) {
  const totalResumes = resumeInfo ? 1 : 0;
  const latestAtsScore = analysisResult
    ? `${analysisResult.atsScore}%`
    : resumeInfo
    ? "Stored"
    : "N/A";
  const analysisCount = analysisResult ? "1 Active" : totalResumes > 0 ? "Ready" : "0";
  const lastUpdated = resumeInfo?.uploadedAt
    ? new Date(resumeInfo.uploadedAt).toLocaleDateString()
    : "Never";

  return (
    <div className="resume-hub-container">
      {/* 4 Metric KPI Cards */}
      <div className="resume-stats-grid">
        <div className="resume-stat-card">
          <div className="resume-stat-content">
            <span className="resume-stat-label">Total Resumes</span>
            <span className="resume-stat-value">{totalResumes}</span>
            <span className="resume-stat-sub">{totalResumes > 0 ? "Active uploaded file" : "No resume on file"}</span>
          </div>
          <div className="resume-stat-icon primary">
            <FileText size={22} />
          </div>
        </div>

        <div className="resume-stat-card">
          <div className="resume-stat-content">
            <span className="resume-stat-label">Latest ATS Score</span>
            <span className="resume-stat-value">{latestAtsScore}</span>
            <span className="resume-stat-sub">Gemini &amp; heuristic match</span>
          </div>
          <div className="resume-stat-icon success">
            <Sparkles size={22} />
          </div>
        </div>

        <div className="resume-stat-card">
          <div className="resume-stat-content">
            <span className="resume-stat-label">Resume Analyses</span>
            <span className="resume-stat-value">{analysisCount}</span>
            <span className="resume-stat-sub">AI skill audits</span>
          </div>
          <div className="resume-stat-icon purple">
            <CheckCircle2 size={22} />
          </div>
        </div>

        <div className="resume-stat-card">
          <div className="resume-stat-content">
            <span className="resume-stat-label">Last Updated</span>
            <span className="resume-stat-value" style={{ fontSize: "18px" }}>{lastUpdated}</span>
            <span className="resume-stat-sub">File sync status</span>
          </div>
          <div className="resume-stat-icon info">
            <Clock size={22} />
          </div>
        </div>
      </div>

      {/* Main 2-Column Responsive Layout */}
      <div className="resume-overview-grid">
        {/* Left Column: Upload Dropzone & Active Stored Resume */}
        <div className="resume-col">
          <ResumeUpload
            onUpload={onUpload}
            uploading={uploading}
            successMessage={successMessage}
          />

          {loading ? (
            <div className="stored-resume-card">
              <div className="skeleton-box" style={{ height: "46px", width: "100%" }} />
              <div className="skeleton-box" style={{ height: "40px", width: "100%" }} />
              <div className="skeleton-box" style={{ height: "36px", width: "100%" }} />
            </div>
          ) : resumeInfo ? (
            <ResumeCard
              resume={resumeInfo}
              onDownload={onDownload}
              onPreview={onPreview}
              onReupload={() => {
                const dropzone = document.querySelector(".dropzone-label");
                if (dropzone) dropzone.scrollIntoView({ behavior: "smooth" });
              }}
              onDelete={onDelete}
              downloading={downloading}
              atsScore={analysisResult?.atsScore}
            />
          ) : (
            <div className="resume-empty-state">
              <div className="resume-empty-icon">
                <FileText size={26} />
              </div>
              <h4 className="resume-empty-title">No Resume Uploaded Yet</h4>
              <p className="resume-empty-desc">
                Upload your PDF resume above to unlock automated AI job matching, ATS scores, and recruiter insights.
              </p>
              <button
                type="button"
                className="secondary-button"
                onClick={() => onNavigateTab("builder")}
                style={{ display: "inline-flex", alignItems: "center", gap: "6px" }}
              >
                <Wand2 size={15} />
                <span>Build Resume from Scratch</span>
              </button>
            </div>
          )}
        </div>

        {/* Right Column: AI Resume Analysis */}
        <div className="resume-col">
          <ResumeAnalysisCard analysisResult={analysisResult} />
        </div>
      </div>
    </div>
  );
}

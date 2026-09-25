import { Sparkles, CheckCircle, AlertCircle, ArrowUpRight, FileCheck } from "lucide-react";

export default function ResumeAnalysisCard({ analysisResult }) {
  if (!analysisResult) {
    return (
      <div className="ats-analysis-card">
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <Sparkles size={20} color="var(--primary-600)" />
          <h3 style={{ margin: 0, fontSize: "16px", fontWeight: 700 }}>AI Resume &amp; ATS Feedback</h3>
        </div>

        <div className="resume-empty-state" style={{ padding: "40px 20px" }}>
          <div className="resume-empty-icon">
            <FileCheck size={28} />
          </div>
          <h4 className="resume-empty-title">Upload a resume to see AI analysis</h4>
          <p className="resume-empty-desc">
            Get instant feedback on your ATS compatibility score, identified industry keywords, and missing technical skills.
          </p>
        </div>
      </div>
    );
  }

  const score = Math.min(Math.max(Number(analysisResult.atsScore) || 0, 0), 100);
  const matched = Array.isArray(analysisResult.matchedSkills) ? analysisResult.matchedSkills : [];
  const missing = Array.isArray(analysisResult.missingSkills) ? analysisResult.missingSkills : [];
  const suggestions = Array.isArray(analysisResult.suggestions) ? analysisResult.suggestions : [];
  const isGemini = analysisResult.source === "GEMINI";

  let tier = "Needs Skill Enrichment";
  let tierDesc = "Enhance keyword depth and structure to pass enterprise ATS scanners.";
  if (score >= 80) {
    tier = "Strong Keyword Alignment";
    tierDesc = "Excellent ATS optimization with strong domain keywords matching industry expectations.";
  } else if (score >= 60) {
    tier = "Moderate ATS Match";
    tierDesc = "Good baseline match, but targeted skill additions can elevate interview callback rates.";
  }

  return (
    <div className="ats-analysis-card">
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", flexWrap: "wrap", gap: "10px" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <Sparkles size={20} color="var(--primary-600)" />
          <h3 style={{ margin: 0, fontSize: "16px", fontWeight: 700 }}>AI Resume &amp; ATS Feedback</h3>
        </div>
        <span
          className={`status-pill ${isGemini ? "offered" : "interview"}`}
          style={{ fontSize: "0.75rem", padding: "4px 10px", display: "inline-flex", alignItems: "center", gap: "5px" }}
        >
          <Sparkles size={12} />
          <span>{isGemini ? "Gemini AI Engine" : "Rule-Based Analyzer"}</span>
        </span>
      </div>

      {/* ATS Score Banner */}
      <div className="ats-banner-grid">
        <div className="ats-score-circle-wrapper">
          <div className="ats-score-inner">
            <div className="ats-score-number">{score}</div>
            <div className="ats-score-scale">/ 100 ATS</div>
          </div>
        </div>
        <div className="ats-banner-info">
          <div className="ats-banner-title">{tier}</div>
          <div className="ats-banner-desc">{tierDesc}</div>
        </div>
      </div>

      {/* Matched Skills */}
      <div className="analysis-section-block">
        <div className="analysis-section-header">
          <CheckCircle size={16} color="var(--success)" />
          <span>Identified &amp; Matched Skills ({matched.length})</span>
        </div>
        <div className="skill-pills-wrap">
          {matched.length > 0 ? (
            matched.map((skill, idx) => (
              <span key={idx} className="skill-pill matched">
                ✓ {skill}
              </span>
            ))
          ) : (
            <span style={{ fontSize: "13px", color: "var(--text-dim)" }}>No matched skills detected yet</span>
          )}
        </div>
      </div>

      {/* Missing Recommended Skills */}
      <div className="analysis-section-block">
        <div className="analysis-section-header">
          <AlertCircle size={16} color="var(--warning)" />
          <span>Recommended Skills to Add ({missing.length})</span>
        </div>
        <div className="skill-pills-wrap">
          {missing.length > 0 ? (
            missing.map((skill, idx) => (
              <span key={idx} className="skill-pill missing">
                + {skill}
              </span>
            ))
          ) : (
            <span style={{ fontSize: "13px", color: "var(--text-dim)" }}>All essential core keywords covered!</span>
          )}
        </div>
      </div>

      {/* Actionable Improvement Suggestions */}
      <div className="analysis-section-block">
        <div className="analysis-section-header">
          <ArrowUpRight size={16} color="var(--primary-600)" />
          <span>Actionable Improvements</span>
        </div>
        <ul className="suggestions-check-list">
          {suggestions.length > 0 ? (
            suggestions.map((item, idx) => (
              <li key={idx}>
                <Sparkles size={14} />
                <span>{item}</span>
              </li>
            ))
          ) : (
            <li>
              <Sparkles size={14} />
              <span>Resume structure, headers, and bullet formatting align well with ATS parsing standards.</span>
            </li>
          )}
        </ul>
      </div>
    </div>
  );
}

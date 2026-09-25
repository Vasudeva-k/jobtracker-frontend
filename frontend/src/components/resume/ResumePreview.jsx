import { Mail, Phone, MapPin, Link2, Code2 } from "lucide-react";

export default function ResumePreview({ formData, selectedTemplate }) {
  const {
    fullName = "",
    title = "",
    email = "",
    phone = "",
    location = "",
    linkedin = "",
    github = "",
    summary = "",
    skillsList = [],
    experienceList = [],
    educationList = [],
    projectList = [],
    certifications = "",
  } = formData;

  const parsedCertifications = certifications
    ? certifications
        .split(/\r?\n|,/)
        .map((c) => c.trim())
        .filter(Boolean)
    : [];

  return (
    <div className="builder-preview-container">
      {/* Top Preview Toolbar */}
      <div className="preview-toolbar">
        <div className="preview-badge-live">
          <span className="preview-live-dot" />
          <span>Live ATS Document Preview</span>
        </div>
        <span style={{ fontSize: "11px", color: "var(--text-dim)", textTransform: "capitalize" }}>
          Template: <strong>{selectedTemplate}</strong>
        </span>
      </div>

      {/* A4 Viewport */}
      <div className="a4-paper-viewport">
        <div className={`a4-document-sheet template-${selectedTemplate}`}>
          {/* Header */}
          <div className="doc-header">
            <h1 className="doc-name">{fullName || "Your Full Name"}</h1>
            {title && <div className="doc-title">{title}</div>}

            <div className="doc-contacts">
              {email && (
                <span>
                  <Mail size={11} style={{ display: "inline", verticalAlign: "middle", marginRight: "3px" }} />
                  {email}
                </span>
              )}
              {phone && (
                <span>
                  <Phone size={11} style={{ display: "inline", verticalAlign: "middle", marginRight: "3px" }} />
                  {phone}
                </span>
              )}
              {location && (
                <span>
                  <MapPin size={11} style={{ display: "inline", verticalAlign: "middle", marginRight: "3px" }} />
                  {location}
                </span>
              )}
              {linkedin && (
                <span>
                  <Link2 size={11} style={{ display: "inline", verticalAlign: "middle", marginRight: "3px" }} />
                  {linkedin}
                </span>
              )}
              {github && (
                <span>
                  <Code2 size={11} style={{ display: "inline", verticalAlign: "middle", marginRight: "3px" }} />
                  {github}
                </span>
              )}
            </div>
          </div>

          {/* Professional Summary */}
          {summary && (
            <div className="doc-section">
              <h2 className="doc-section-title">Professional Summary</h2>
              <div className="doc-text-block">{summary}</div>
            </div>
          )}

          {/* Core Competencies / Skills */}
          {skillsList.length > 0 && (
            <div className="doc-section">
              <h2 className="doc-section-title">Core Competencies &amp; Skills</h2>
              <div className="doc-skill-tags">
                {skillsList.map((sk, idx) => (
                  <span key={idx} className="doc-skill-tag">
                    {sk}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Work Experience */}
          {experienceList.length > 0 && (
            <div className="doc-section">
              <h2 className="doc-section-title">Professional Experience</h2>
              {experienceList.map((exp, idx) => (
                <div key={idx} className="doc-entry-item">
                  <div className="doc-entry-head">
                    <strong>{exp.role || "Role / Title"}</strong>
                    <span className="doc-entry-date">{exp.startDate || "Date Range"}</span>
                  </div>
                  {(exp.company || exp.location) && (
                    <div className="doc-entry-sub">
                      {exp.company} {exp.location ? `• ${exp.location}` : ""}
                    </div>
                  )}
                  {exp.description && (
                    <div className="doc-text-block" style={{ marginTop: "4px" }}>
                      {exp.description}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          {/* Education */}
          {educationList.length > 0 && (
            <div className="doc-section">
              <h2 className="doc-section-title">Education</h2>
              {educationList.map((edu, idx) => (
                <div key={idx} className="doc-entry-item">
                  <div className="doc-entry-head">
                    <strong>{edu.degree || "Degree / Major"}</strong>
                    <span className="doc-entry-date">{edu.startDate || ""}</span>
                  </div>
                  <div className="doc-entry-sub">
                    {edu.institution || "Institution"} {edu.grade ? `• ${edu.grade}` : ""}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Key Projects */}
          {projectList.length > 0 && (
            <div className="doc-section">
              <h2 className="doc-section-title">Key Projects</h2>
              {projectList.map((proj, idx) => (
                <div key={idx} className="doc-entry-item">
                  <div className="doc-entry-head">
                    <strong>{proj.name || "Project Title"}</strong>
                    {proj.link && (
                      <span className="doc-entry-date" style={{ color: "var(--primary-600)" }}>
                        {proj.link}
                      </span>
                    )}
                  </div>
                  {proj.techStack && (
                    <div className="doc-entry-sub">
                      Technologies: {proj.techStack}
                    </div>
                  )}
                  {proj.description && (
                    <div className="doc-text-block" style={{ marginTop: "4px" }}>
                      {proj.description}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          {/* Certifications */}
          {parsedCertifications.length > 0 && (
            <div className="doc-section">
              <h2 className="doc-section-title">Certifications &amp; Accreditations</h2>
              <div className="doc-skill-tags">
                {parsedCertifications.map((cert, idx) => (
                  <span key={idx} className="doc-skill-tag" style={{ background: "#FEF3C7", borderColor: "#FDE68A" }}>
                    {cert}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

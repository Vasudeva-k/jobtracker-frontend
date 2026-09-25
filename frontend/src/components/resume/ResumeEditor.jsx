import { useState } from "react";
import {
  User,
  FileText,
  Briefcase,
  GraduationCap,
  Sparkles,
  FolderGit2,
  Award,
  ChevronDown,
  ChevronUp,
  Plus,
  Trash2,
  Download,
  RotateCcw,
  Save,
  UserCheck,
  RefreshCw,
} from "lucide-react";
import ResumeTemplateSelector from "./ResumeTemplateSelector";

export default function ResumeEditor({
  formData,
  setFormData,
  selectedTemplate,
  setSelectedTemplate,
  onGeneratePdf,
  onSaveDraft,
  onResetSample,
  onLoadProfile,
  generatingPdf = false,
  loadingProfile = false,
}) {
  const [expandedSections, setExpandedSections] = useState({
    personal: true,
    summary: true,
    skills: true,
    experience: false,
    education: false,
    projects: false,
    certifications: false,
  });

  const [skillInput, setSkillInput] = useState("");

  const toggleSection = (key) => {
    setExpandedSections((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  // Add / Remove dynamic experience
  const addExperience = () => {
    setFormData((prev) => ({
      ...prev,
      experienceList: [
        ...prev.experienceList,
        {
          company: "",
          role: "",
          location: "",
          startDate: "",
          endDate: "Present",
          description: "",
        },
      ],
    }));
  };

  const updateExperience = (index, field, value) => {
    setFormData((prev) => {
      const updated = [...prev.experienceList];
      updated[index] = { ...updated[index], [field]: value };
      return { ...prev, experienceList: updated };
    });
  };

  const removeExperience = (index) => {
    setFormData((prev) => ({
      ...prev,
      experienceList: prev.experienceList.filter((_, idx) => idx !== index),
    }));
  };

  // Add / Remove dynamic education
  const addEducation = () => {
    setFormData((prev) => ({
      ...prev,
      educationList: [
        ...prev.educationList,
        {
          institution: "",
          degree: "",
          fieldOfStudy: "",
          startDate: "",
          endDate: "",
          grade: "",
        },
      ],
    }));
  };

  const updateEducation = (index, field, value) => {
    setFormData((prev) => {
      const updated = [...prev.educationList];
      updated[index] = { ...updated[index], [field]: value };
      return { ...prev, educationList: updated };
    });
  };

  const removeEducation = (index) => {
    setFormData((prev) => ({
      ...prev,
      educationList: prev.educationList.filter((_, idx) => idx !== index),
    }));
  };

  // Add / Remove dynamic projects
  const addProject = () => {
    setFormData((prev) => ({
      ...prev,
      projectList: [
        ...prev.projectList,
        {
          name: "",
          techStack: "",
          link: "",
          description: "",
        },
      ],
    }));
  };

  const updateProject = (index, field, value) => {
    setFormData((prev) => {
      const updated = [...prev.projectList];
      updated[index] = { ...updated[index], [field]: value };
      return { ...prev, projectList: updated };
    });
  };

  const removeProject = (index) => {
    setFormData((prev) => ({
      ...prev,
      projectList: prev.projectList.filter((_, idx) => idx !== index),
    }));
  };

  // Add / Remove skills
  const handleAddSkill = (e) => {
    if (e.key === "Enter" || e.type === "click") {
      e.preventDefault();
      const val = skillInput.trim().replace(/^,+|,+$/g, "");
      if (val) {
        const skillsToAdd = val
          .split(",")
          .map((s) => s.trim())
          .filter(Boolean);
        const existing = new Set(formData.skillsList);
        const nextList = [...formData.skillsList];
        skillsToAdd.forEach((s) => {
          if (!existing.has(s)) {
            nextList.push(s);
            existing.add(s);
          }
        });
        setFormData((prev) => ({ ...prev, skillsList: nextList }));
        setSkillInput("");
      }
    }
  };

  const removeSkill = (skillToRemove) => {
    setFormData((prev) => ({
      ...prev,
      skillsList: prev.skillsList.filter((s) => s !== skillToRemove),
    }));
  };

  return (
    <div className="builder-editor-card">
      {/* Top Header Strip with Action Controls */}
      <div className="builder-header-strip">
        <div>
          <h3 style={{ margin: 0, fontSize: "17px", fontWeight: 800 }}>Resume Form &amp; Content</h3>
          <p style={{ margin: "2px 0 0 0", fontSize: "13px", color: "var(--text-muted)" }}>
            Fill in your background details. Live preview updates automatically.
          </p>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "8px", flexWrap: "wrap" }}>
          {onLoadProfile && (
            <button
              type="button"
              className="secondary-button"
              onClick={onLoadProfile}
              disabled={loadingProfile}
              title="Pre-populate from your Candidate Profile"
              style={{ fontSize: "12px", padding: "6px 12px" }}
            >
              <UserCheck size={14} />
              <span>{loadingProfile ? "Loading Profile..." : "Auto-fill from Profile"}</span>
            </button>
          )}

          <button
            type="button"
            className="secondary-button"
            onClick={onSaveDraft}
            title="Save draft to browser storage"
            style={{ fontSize: "12px", padding: "6px 12px" }}
          >
            <Save size={14} />
            <span>Save Draft</span>
          </button>

          <button
            type="button"
            className="secondary-button"
            onClick={onResetSample}
            title="Reset to sample data"
            style={{ fontSize: "12px", padding: "6px 12px" }}
          >
            <RotateCcw size={14} />
            <span>Reset</span>
          </button>
        </div>
      </div>

      {/* Template Selector Section */}
      <div>
        <label style={{ display: "block", fontSize: "12.5px", fontWeight: 700, color: "var(--text-heading)", marginBottom: "8px" }}>
          Select ATS Resume Style
        </label>
        <ResumeTemplateSelector
          selectedTemplate={selectedTemplate}
          onSelectTemplate={setSelectedTemplate}
        />
      </div>

      {/* Accordion Group */}
      <div className="accordion-group">
        {/* 1. Personal Information */}
        <div className={`accordion-item ${expandedSections.personal ? "expanded" : ""}`}>
          <div className="accordion-header" onClick={() => toggleSection("personal")}>
            <div className="accordion-title">
              <User size={17} />
              <span>Personal Information</span>
            </div>
            {expandedSections.personal ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          </div>

          {expandedSections.personal && (
            <div className="accordion-body">
              <div className="form-grid-2">
                <div className="form-group">
                  <label>Full Name *</label>
                  <input
                    type="text"
                    className="app-input"
                    placeholder="e.g. Alex Morgan"
                    value={formData.fullName}
                    onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Professional Title</label>
                  <input
                    type="text"
                    className="app-input"
                    placeholder="e.g. Senior Full Stack Engineer"
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-grid-2">
                <div className="form-group">
                  <label>Email Address *</label>
                  <input
                    type="email"
                    className="app-input"
                    placeholder="alex.morgan@example.com"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Phone Number</label>
                  <input
                    type="tel"
                    className="app-input"
                    placeholder="+1 (555) 234-5678"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-grid-3">
                <div className="form-group">
                  <label>Location / City</label>
                  <input
                    type="text"
                    className="app-input"
                    placeholder="San Francisco, CA"
                    value={formData.location}
                    onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>LinkedIn URL</label>
                  <input
                    type="text"
                    className="app-input"
                    placeholder="linkedin.com/in/alexmorgan"
                    value={formData.linkedin}
                    onChange={(e) => setFormData({ ...formData, linkedin: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>GitHub / Portfolio</label>
                  <input
                    type="text"
                    className="app-input"
                    placeholder="github.com/alexmorgan"
                    value={formData.github}
                    onChange={(e) => setFormData({ ...formData, github: e.target.value })}
                  />
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 2. Professional Summary */}
        <div className={`accordion-item ${expandedSections.summary ? "expanded" : ""}`}>
          <div className="accordion-header" onClick={() => toggleSection("summary")}>
            <div className="accordion-title">
              <FileText size={17} />
              <span>Professional Summary</span>
            </div>
            {expandedSections.summary ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          </div>

          {expandedSections.summary && (
            <div className="accordion-body">
              <div className="form-group">
                <label>Summary Statement</label>
                <textarea
                  rows={4}
                  className="app-textarea"
                  placeholder="Results-driven Software Engineer with experience in scalable distributed systems..."
                  value={formData.summary}
                  onChange={(e) => setFormData({ ...formData, summary: e.target.value })}
                />
              </div>
            </div>
          )}
        </div>

        {/* 3. Skills & Competencies */}
        <div className={`accordion-item ${expandedSections.skills ? "expanded" : ""}`}>
          <div className="accordion-header" onClick={() => toggleSection("skills")}>
            <div className="accordion-title">
              <Sparkles size={17} />
              <span>Core Skills &amp; Competencies ({formData.skillsList.length})</span>
            </div>
            {expandedSections.skills ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          </div>

          {expandedSections.skills && (
            <div className="accordion-body">
              <div className="form-group">
                <label>Add Skill Tags (type and press Enter or comma)</label>
                <div style={{ display: "flex", gap: "8px" }}>
                  <input
                    type="text"
                    className="app-input"
                    placeholder="e.g. React, Spring Boot, PostgreSQL, Docker"
                    value={skillInput}
                    onChange={(e) => setSkillInput(e.target.value)}
                    onKeyDown={handleAddSkill}
                  />
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={handleAddSkill}
                    style={{ whiteSpace: "nowrap" }}
                  >
                    <Plus size={15} />
                    <span>Add</span>
                  </button>
                </div>
              </div>

              <div className="skill-pills-wrap" style={{ marginTop: "6px" }}>
                {formData.skillsList.map((skill, idx) => (
                  <span
                    key={idx}
                    className="skill-pill matched"
                    style={{ cursor: "pointer", display: "inline-flex", alignItems: "center", gap: "6px" }}
                    onClick={() => removeSkill(skill)}
                    title="Click to remove"
                  >
                    <span>{skill}</span>
                    <span style={{ fontSize: "11px", fontWeight: 800 }}>×</span>
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* 4. Work Experience */}
        <div className={`accordion-item ${expandedSections.experience ? "expanded" : ""}`}>
          <div className="accordion-header" onClick={() => toggleSection("experience")}>
            <div className="accordion-title">
              <Briefcase size={17} />
              <span>Work Experience ({formData.experienceList.length})</span>
            </div>
            {expandedSections.experience ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          </div>

          {expandedSections.experience && (
            <div className="accordion-body">
              {formData.experienceList.map((exp, index) => (
                <div key={index} className="dynamic-item-card">
                  <div className="dynamic-item-top">
                    <span className="dynamic-item-number">Role #{index + 1}</span>
                    <button
                      type="button"
                      className="remove-item-btn"
                      onClick={() => removeExperience(index)}
                      title="Remove entry"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>

                  <div className="form-grid-2">
                    <div className="form-group">
                      <label>Job Title / Role</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="Software Engineer"
                        value={exp.role}
                        onChange={(e) => updateExperience(index, "role", e.target.value)}
                      />
                    </div>
                    <div className="form-group">
                      <label>Company / Organization</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="Tech Solutions Inc."
                        value={exp.company}
                        onChange={(e) => updateExperience(index, "company", e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="form-grid-2">
                    <div className="form-group">
                      <label>Dates / Duration</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="Jan 2022 – Present"
                        value={exp.startDate}
                        onChange={(e) => updateExperience(index, "startDate", e.target.value)}
                      />
                    </div>
                    <div className="form-group">
                      <label>Location</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="Remote / New York, NY"
                        value={exp.location}
                        onChange={(e) => updateExperience(index, "location", e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Key Accomplishments &amp; Bullet Points</label>
                    <textarea
                      rows={3}
                      className="app-textarea"
                      placeholder="• Built REST APIs reducing response latency by 35%&#10;• Orchestrated microservices with Docker"
                      value={exp.description}
                      onChange={(e) => updateExperience(index, "description", e.target.value)}
                    />
                  </div>
                </div>
              ))}

              <button
                type="button"
                className="add-entry-btn"
                onClick={addExperience}
              >
                <Plus size={16} />
                <span>Add Work Experience Entry</span>
              </button>
            </div>
          )}
        </div>

        {/* 5. Education */}
        <div className={`accordion-item ${expandedSections.education ? "expanded" : ""}`}>
          <div className="accordion-header" onClick={() => toggleSection("education")}>
            <div className="accordion-title">
              <GraduationCap size={17} />
              <span>Education ({formData.educationList.length})</span>
            </div>
            {expandedSections.education ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          </div>

          {expandedSections.education && (
            <div className="accordion-body">
              {formData.educationList.map((edu, index) => (
                <div key={index} className="dynamic-item-card">
                  <div className="dynamic-item-top">
                    <span className="dynamic-item-number">Education #{index + 1}</span>
                    <button
                      type="button"
                      className="remove-item-btn"
                      onClick={() => removeEducation(index)}
                      title="Remove entry"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>

                  <div className="form-grid-2">
                    <div className="form-group">
                      <label>Institution / University</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="University of California, Berkeley"
                        value={edu.institution}
                        onChange={(e) => updateEducation(index, "institution", e.target.value)}
                      />
                    </div>
                    <div className="form-group">
                      <label>Degree &amp; Major</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="B.S. in Computer Science"
                        value={edu.degree}
                        onChange={(e) => updateEducation(index, "degree", e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="form-grid-2">
                    <div className="form-group">
                      <label>Dates</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="2019 – 2023"
                        value={edu.startDate}
                        onChange={(e) => updateEducation(index, "startDate", e.target.value)}
                      />
                    </div>
                    <div className="form-group">
                      <label>GPA / Honours</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="GPA: 3.8 / 4.0"
                        value={edu.grade}
                        onChange={(e) => updateEducation(index, "grade", e.target.value)}
                      />
                    </div>
                  </div>
                </div>
              ))}

              <button
                type="button"
                className="add-entry-btn"
                onClick={addEducation}
              >
                <Plus size={16} />
                <span>Add Education Entry</span>
              </button>
            </div>
          )}
        </div>

        {/* 6. Projects */}
        <div className={`accordion-item ${expandedSections.projects ? "expanded" : ""}`}>
          <div className="accordion-header" onClick={() => toggleSection("projects")}>
            <div className="accordion-title">
              <FolderGit2 size={17} />
              <span>Projects ({formData.projectList.length})</span>
            </div>
            {expandedSections.projects ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          </div>

          {expandedSections.projects && (
            <div className="accordion-body">
              {formData.projectList.map((proj, index) => (
                <div key={index} className="dynamic-item-card">
                  <div className="dynamic-item-top">
                    <span className="dynamic-item-number">Project #{index + 1}</span>
                    <button
                      type="button"
                      className="remove-item-btn"
                      onClick={() => removeProject(index)}
                      title="Remove entry"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>

                  <div className="form-grid-2">
                    <div className="form-group">
                      <label>Project Name</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="JobTracker SaaS"
                        value={proj.name}
                        onChange={(e) => updateProject(index, "name", e.target.value)}
                      />
                    </div>
                    <div className="form-group">
                      <label>Technologies Used</label>
                      <input
                        type="text"
                        className="app-input"
                        placeholder="React, Spring Boot, MySQL, Gemini AI"
                        value={proj.techStack}
                        onChange={(e) => updateProject(index, "techStack", e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Project Link / Demo</label>
                    <input
                      type="text"
                      className="app-input"
                      placeholder="github.com/user/project"
                      value={proj.link}
                      onChange={(e) => updateProject(index, "link", e.target.value)}
                    />
                  </div>

                  <div className="form-group">
                    <label>Description</label>
                    <textarea
                      rows={3}
                      className="app-textarea"
                      placeholder="• Architected full-stack workflow with automated ATS parsing..."
                      value={proj.description}
                      onChange={(e) => updateProject(index, "description", e.target.value)}
                    />
                  </div>
                </div>
              ))}

              <button
                type="button"
                className="add-entry-btn"
                onClick={addProject}
              >
                <Plus size={16} />
                <span>Add Project Entry</span>
              </button>
            </div>
          )}
        </div>

        {/* 7. Certifications & Achievements */}
        <div className={`accordion-item ${expandedSections.certifications ? "expanded" : ""}`}>
          <div className="accordion-header" onClick={() => toggleSection("certifications")}>
            <div className="accordion-title">
              <Award size={17} />
              <span>Certifications &amp; Achievements</span>
            </div>
            {expandedSections.certifications ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          </div>

          {expandedSections.certifications && (
            <div className="accordion-body">
              <div className="form-group">
                <label>Certifications (One per line or comma-separated)</label>
                <textarea
                  rows={3}
                  className="app-textarea"
                  placeholder="AWS Certified Solutions Architect&#10;Oracle Certified Professional: Java SE"
                  value={formData.certifications}
                  onChange={(e) => setFormData({ ...formData, certifications: e.target.value })}
                />
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Main Download Button */}
      <div style={{ marginTop: "10px" }}>
        <button
          type="button"
          className="primary-button"
          onClick={onGeneratePdf}
          disabled={generatingPdf}
          style={{ width: "100%", padding: "14px", fontSize: "15px", justifyContent: "center" }}
        >
          {generatingPdf ? (
            <>
              <RefreshCw size={18} className="spinning" />
              <span>Generating ATS PDF Resume...</span>
            </>
          ) : (
            <>
              <Download size={18} />
              <span>Download ATS PDF Resume</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
}

import { useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import { api } from "../../services/api";
import {
  Mail,
  Phone,
  Briefcase,
  Award,
  FolderGit2,
  Sparkles,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Save,
  TrendingUp,
  Cpu,
} from "lucide-react";

export default function ProfilePage() {
  const { user } = useAuth();
  const toast = useToast();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Controlled form state
  const [formData, setFormData] = useState({
    atsScore: 0,
    yearsOfExperience: 0,
    projects: 0,
    certifications: 0,
    skills: "",
  });

  // AI Success Prediction state
  const [predicting, setPredicting] = useState(false);
  const [predictionResult, setPredictionResult] = useState(null);

  const loadProfile = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await api.getProfile();
      setProfile(data);
      if (data) {
        setFormData({
          atsScore: data.atsScore ?? 0,
          yearsOfExperience: data.yearsOfExperience ?? 0,
          projects: data.projects ?? 0,
          certifications: data.certifications ?? 0,
          skills: Array.isArray(data.skills) ? data.skills.join(", ") : "",
        });
      }
    } catch (err) {
      console.error("Failed to load profile:", err);
      setError(err instanceof Error ? err.message : "Failed to load candidate profile");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError("");
      setSuccess("");

      const skillList = formData.skills
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean);

      const payload = {
        atsScore: Number(formData.atsScore) || 0,
        yearsOfExperience: Number(formData.yearsOfExperience) || 0,
        projects: Number(formData.projects) || 0,
        certifications: Number(formData.certifications) || 0,
        skills: skillList,
      };

      const updated = await api.saveProfile(payload);
      setProfile(updated);
      const msg = "Candidate profile updated successfully!";
      setSuccess(msg);
      toast.success(msg);
    } catch (err) {
      console.error("Save profile error:", err);
      const msg = err instanceof Error ? err.message : "Failed to save profile changes";
      setError(msg);
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  const handlePredictSuccess = async () => {
    try {
      setPredicting(true);
      setError("");
      const res = await api.predictJobSuccessFromProfile();
      setPredictionResult(res);
      toast.success("Application readiness assessed!");
    } catch (err) {
      console.error("Prediction error:", err);
      const msg = err instanceof Error ? err.message : "Failed to generate AI job success prediction";
      setError(msg);
      toast.error(msg);
    } finally {
      setPredicting(false);
    }
  };

  return (
    <div className="page-container">
      {/* Page Heading */}
      <div className="page-heading">
        <div>
          <h2>Candidate Career Profile</h2>
          <p>Manage your technical credentials, experience background, and AI candidate metrics</p>
        </div>
        <div className="heading-actions">
          <button className="secondary-button" onClick={loadProfile} disabled={loading || saving}>
            <RefreshCw size={15} className={loading ? "spinning" : ""} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="dashboard-alert error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="dashboard-alert success">
          <CheckCircle2 size={18} />
          <span>{success}</span>
        </div>
      )}

      {/* Account Info Header Card */}
      <div className="content-card">
        <div style={{ display: "flex", alignItems: "center", gap: "20px", flexWrap: "wrap" }}>
          <div
            style={{
              width: "64px",
              height: "64px",
              fontSize: "26px",
              borderRadius: "var(--radius-xl)",
              background: "linear-gradient(135deg, var(--primary-600), var(--purple))",
              color: "#FFFFFF",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontWeight: 800,
              boxShadow: "0 4px 14px rgba(79, 70, 229, 0.3)",
              flexShrink: 0,
            }}
          >
            {user?.firstName?.charAt(0)?.toUpperCase() || "U"}
          </div>
          <div style={{ flex: 1 }}>
            <h3 style={{ margin: 0, fontSize: "22px", fontWeight: 800 }}>
              {user?.firstName} {user?.lastName}
            </h3>
            <div
              style={{
                display: "flex",
                flexWrap: "wrap",
                gap: "18px",
                marginTop: "6px",
                color: "var(--text-muted)",
                fontSize: "14px",
              }}
            >
              <span style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                <Mail size={15} color="var(--primary-600)" />
                {user?.email}
              </span>
              {user?.phone && (
                <span style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <Phone size={15} color="var(--success)" />
                  {user?.phone}
                </span>
              )}
              <span className="user-role-badge">
                <span className="online-dot" />
                {user?.role || "JOB_SEEKER"}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Profile KPI Cards */}
      <div className="metric-cards-grid">
        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">ATS Benchmark</span>
            <span className="metric-card-value">{profile?.atsScore ?? formData.atsScore}%</span>
            <span className="metric-card-sub">Resume ATS rating</span>
          </div>
          <div className="metric-card-icon primary">
            <Sparkles size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">Total Experience</span>
            <span className="metric-card-value">{profile?.yearsOfExperience ?? formData.yearsOfExperience} Yrs</span>
            <span className="metric-card-sub">Industry tenure</span>
          </div>
          <div className="metric-card-icon info">
            <Briefcase size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">Featured Projects</span>
            <span className="metric-card-value">{profile?.projects ?? formData.projects}</span>
            <span className="metric-card-sub">Delivered deliverables</span>
          </div>
          <div className="metric-card-icon success">
            <FolderGit2 size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">Certifications</span>
            <span className="metric-card-value">{profile?.certifications ?? formData.certifications}</span>
            <span className="metric-card-sub">Verified credentials</span>
          </div>
          <div className="metric-card-icon warning">
            <Award size={24} />
          </div>
        </div>
      </div>

      {/* Profile Form & AI Success Prediction Grid */}
      <div className="dashboard-grid-2">
        {/* Profile Edit Form */}
        <div className="content-card">
          <div className="card-header">
            <h3>Edit Candidate Credentials</h3>
          </div>

          <form onSubmit={handleSaveProfile} className="app-form">
            <div className="form-grid-2">
              <div className="form-group">
                <label>ATS Score (%)</label>
                <input
                  type="number"
                  min="0"
                  max="100"
                  className="app-input"
                  value={formData.atsScore}
                  onChange={(e) => setFormData({ ...formData, atsScore: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Years of Experience</label>
                <input
                  type="number"
                  min="0"
                  max="40"
                  className="app-input"
                  value={formData.yearsOfExperience}
                  onChange={(e) =>
                    setFormData({ ...formData, yearsOfExperience: e.target.value })
                  }
                />
              </div>
            </div>

            <div className="form-grid-2">
              <div className="form-group">
                <label>Projects Completed</label>
                <input
                  type="number"
                  min="0"
                  max="100"
                  className="app-input"
                  value={formData.projects}
                  onChange={(e) => setFormData({ ...formData, projects: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Certifications Earned</label>
                <input
                  type="number"
                  min="0"
                  max="50"
                  className="app-input"
                  value={formData.certifications}
                  onChange={(e) =>
                    setFormData({ ...formData, certifications: e.target.value })
                  }
                />
              </div>
            </div>

            <div className="form-group">
              <label>Skills & Technologies (Comma-separated)</label>
              <textarea
                rows={3}
                className="app-textarea"
                placeholder="Java, Spring Boot, React, MySQL, Docker, AWS, Microservices"
                value={formData.skills}
                onChange={(e) => setFormData({ ...formData, skills: e.target.value })}
              />
            </div>

            <button type="submit" className="primary-button" disabled={saving}>
              <Save size={16} />
              <span>{saving ? "Saving Changes..." : "Save Profile Details"}</span>
            </button>
          </form>
        </div>

        {/* AI Job Success Predictor Box */}
        <div className="content-card">
          <div className="card-header">
            <h3>
              <Cpu size={20} color="var(--primary-600)" />
              AI Application Readiness
            </h3>
          </div>
          <p style={{ fontSize: "14px", color: "var(--text-muted)", marginBottom: "16px" }}>
            Generate an AI application-readiness assessment based on your saved profile credentials.
          </p>

          <button
            type="button"
            className="primary-button"
            onClick={handlePredictSuccess}
            disabled={predicting}
            style={{ width: "100%", justifyContent: "center" }}
          >
            <TrendingUp size={16} />
            <span>{predicting ? "Analyzing Profile Readiness..." : "Assess Readiness From Profile (AI)"}</span>
          </button>

          {predictionResult ? (
            <div style={{ marginTop: "24px", display: "flex", flexDirection: "column", gap: "16px" }}>
              <div
                style={{
                  background: "var(--primary-50)",
                  border: "1px solid var(--primary-200)",
                  padding: "20px",
                  borderRadius: "var(--radius-xl)",
                  textAlign: "center",
                }}
              >
                <span style={{ fontSize: "12px", fontWeight: 700, color: "var(--primary-700)", textTransform: "uppercase" }}>
                  Application Readiness Assessment
                </span>
                <div style={{ fontSize: "36px", fontWeight: 800, color: "var(--primary-700)", margin: "6px 0" }}>
                  {predictionResult.successProbability || 75}%
                </div>
                <span style={{ fontSize: "13px", color: "var(--text-muted)" }}>
                  Experience Tier: <strong>{predictionResult.experienceLevel || "Mid-Level"}</strong>
                </span>
              </div>

              {predictionResult.strengths && predictionResult.strengths.length > 0 && (
                <div>
                  <h4 style={{ fontSize: "13px", fontWeight: 700, color: "var(--text-heading)", marginBottom: "6px" }}>
                    Candidate Strengths:
                  </h4>
                  <div style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}>
                    {predictionResult.strengths.map((s, idx) => (
                      <span key={idx} className="status-pill offered">
                        ✓ {s}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {predictionResult.recommendations && predictionResult.recommendations.length > 0 && (
                <div>
                  <h4 style={{ fontSize: "13px", fontWeight: 700, color: "var(--text-heading)", marginBottom: "6px" }}>
                    AI Recommendations:
                  </h4>
                  <ul style={{ paddingLeft: "18px", color: "var(--text-muted)", fontSize: "13px", lineHeight: 1.5 }}>
                    {predictionResult.recommendations.map((rec, idx) => (
                      <li key={idx}>{rec}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ) : (
            <div className="empty-state-box" style={{ marginTop: "24px" }}>
              <Sparkles size={32} />
              <h4>Run AI Readiness Assessment</h4>
              <p>Click the button above to generate an AI application-readiness assessment based on your profile.</p>
            </div>
          )}
        </div>
      </div>

      {/* Demonstrated Skills Inventory */}
      <div className="content-card">
        <div className="card-header">
          <h3>Demonstrated Skills Inventory</h3>
        </div>

        <div style={{ display: "flex", flexWrap: "wrap", gap: "8px", marginTop: "12px" }}>
          {profile?.skills && profile.skills.length > 0 ? (
            profile.skills.map((skill, index) => (
              <span key={index} className="status-pill interview" style={{ padding: "6px 14px", fontSize: "13px" }}>
                <CheckCircle2 size={14} style={{ marginRight: "4px" }} />
                {skill}
              </span>
            ))
          ) : (
            <span style={{ color: "var(--text-dim)", fontSize: "14px" }}>
              No skills added yet. Use the form above to catalog your core technical skills.
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

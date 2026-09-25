import { useEffect, useState, useCallback } from "react";
import { api } from "../../services/api";
import {
  Briefcase,
  Search,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Eye,
  Trash2,
  X,
  Calendar,
} from "lucide-react";

const STATUS_OPTIONS = [
  { value: "ALL", label: "All Statuses" },
  { value: "APPLIED", label: "Applied" },
  { value: "INTERVIEW", label: "Interview" },
  { value: "OFFERED", label: "Offered" },
  { value: "REJECTED", label: "Rejected" },
  { value: "SAVED", label: "Saved" },
];

export default function AdminApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [actionLoading, setActionLoading] = useState(null);

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [selectedApp, setSelectedApp] = useState(null);

  const loadApplications = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const data = await api.getAdminApplications(statusFilter);
      setApplications(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Failed to load admin applications:", err);
      setError(err instanceof Error ? err.message : "Failed to load platform applications");
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => {
    loadApplications();
  }, [loadApplications]);

  const handleDeleteApplication = async (id) => {
    const confirmed = window.confirm(
      `Are you sure you want to delete application record #${id}?`
    );
    if (!confirmed) return;

    try {
      setActionLoading(id);
      setError("");
      setSuccess("");
      await api.deleteAdminApplication(id);
      setSuccess(`Application #${id} deleted successfully.`);
      if (selectedApp?.id === id) {
        setSelectedApp(null);
      }
      await loadApplications();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete application");
    } finally {
      setActionLoading(null);
    }
  };

  const filteredApplications = applications.filter((app) => {
    const q = search.toLowerCase().trim();
    if (!q) return true;

    return (
      String(app.id || "").includes(q) ||
      (app.companyName || "").toLowerCase().includes(q) ||
      (app.jobTitle || "").toLowerCase().includes(q) ||
      (app.location || "").toLowerCase().includes(q) ||
      (app.userName || "").toLowerCase().includes(q) ||
      (app.userEmail || "").toLowerCase().includes(q) ||
      (app.status || "").toLowerCase().includes(q)
    );
  });

  const getStatusBadge = (status) => {
    const s = String(status || "").toUpperCase();
    switch (s) {
      case "APPLIED":
        return <span className="status-pill applied">Applied</span>;
      case "INTERVIEW":
        return <span className="status-pill interview">Interview</span>;
      case "OFFERED":
        return <span className="status-pill offered">Offered</span>;
      case "REJECTED":
        return <span className="status-pill rejected">Rejected</span>;
      default:
        return <span className="status-pill saved">Saved</span>;
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    try {
      return new Date(dateStr).toLocaleDateString();
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="page-container">
      <div className="page-heading">
        <div>
          <h2>Platform Job Applications</h2>
          <p>Oversee all submissions, candidate progress, and interview records across the platform</p>
        </div>
        <div className="heading-actions">
          <button className="secondary-button" onClick={loadApplications} disabled={loading}>
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

      {/* Search & Filter Bar */}
      <div className="content-card" style={{ padding: "16px 20px" }}>
        <div style={{ display: "flex", flexWrap: "wrap", gap: "16px", alignItems: "center", justifyContent: "space-between" }}>
          <div className="header-search" style={{ width: "320px", display: "flex" }}>
            <Search size={16} />
            <input
              type="text"
              placeholder="Search by company, role, user email, or ID..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            {search && (
              <button
                onClick={() => setSearch("")}
                style={{ position: "absolute", right: "12px", top: "50%", transform: "translateY(-50%)", background: "none", border: "none", cursor: "pointer", color: "var(--text-dim)" }}
              >
                <X size={14} />
              </button>
            )}
          </div>

          <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
            <select
              className="app-select"
              style={{ width: "160px" }}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              {STATUS_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            <span style={{ fontSize: "13px", color: "var(--text-muted)", fontWeight: 600 }}>
              Showing {filteredApplications.length} application(s)
            </span>
          </div>
        </div>
      </div>

      {/* Applications Table */}
      <div className="content-card">
        {loading ? (
          <div className="empty-state-box">
            <RefreshCw size={28} className="spinning" color="var(--primary-600)" />
            <h4>Loading platform applications...</h4>
          </div>
        ) : filteredApplications.length > 0 ? (
          <div className="table-responsive">
            <table className="saas-table">
              <thead>
                <tr>
                  <th>Job & Company</th>
                  <th>Candidate</th>
                  <th>Status</th>
                  <th>Applied Date</th>
                  <th>Interview</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredApplications.map((app) => (
                  <tr key={app.id}>
                    <td>
                      <div className="company-cell">
                        <div className="company-logo-placeholder">
                          {app.companyName?.charAt(0)?.toUpperCase() || "J"}
                        </div>
                        <div className="company-details">
                          <strong>{app.jobTitle || "Software Engineer"}</strong>
                          <span>{app.companyName} • {app.location || "Remote"}</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div style={{ display: "flex", flexDirection: "column" }}>
                        <strong style={{ fontSize: "13px", color: "var(--text-heading)" }}>{app.userName || "Candidate"}</strong>
                        <span style={{ fontSize: "12px", color: "var(--text-dim)" }}>{app.userEmail}</span>
                      </div>
                    </td>
                    <td>{getStatusBadge(app.status)}</td>
                    <td style={{ color: "var(--text-muted)", fontSize: "13px" }}>
                      {formatDate(app.appliedDate)}
                    </td>
                    <td>
                      {app.interviewDate ? (
                        <span className="status-pill interview">
                          <Calendar size={12} />
                          {formatDate(app.interviewDate)}
                        </span>
                      ) : (
                        <span style={{ color: "var(--text-dim)" }}>—</span>
                      )}
                    </td>
                    <td style={{ textAlign: "right" }}>
                      <div style={{ display: "inline-flex", gap: "8px" }}>
                        <button
                          className="secondary-button"
                          style={{ padding: "5px 10px", fontSize: "12px" }}
                          onClick={() => setSelectedApp(app)}
                          title="View Details"
                        >
                          <Eye size={14} />
                          <span>View</span>
                        </button>
                        <button
                          className="danger-button"
                          style={{ padding: "5px 10px", fontSize: "12px" }}
                          onClick={() => handleDeleteApplication(app.id)}
                          disabled={actionLoading === app.id}
                          title="Delete Record"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state-box">
            <div className="empty-state-icon">
              <Briefcase size={24} />
            </div>
            <h4>No applications found</h4>
            <p>Try adjusting your search criteria or status filter.</p>
          </div>
        )}
      </div>

      {/* Details Modal */}
      {selectedApp && (
        <div className="modal-overlay" onClick={() => setSelectedApp(null)}>
          <div className="modal-container" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Application #{selectedApp.id} Details</h3>
              <button className="modal-close-btn" onClick={() => setSelectedApp(null)}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px", marginBottom: "20px" }}>
                <div>
                  <label style={{ fontSize: "12px", color: "var(--text-dim)", textTransform: "uppercase", fontWeight: 700 }}>Company</label>
                  <div style={{ fontSize: "16px", fontWeight: 700, color: "var(--text-heading)", marginTop: "2px" }}>
                    {selectedApp.companyName}
                  </div>
                </div>
                <div>
                  <label style={{ fontSize: "12px", color: "var(--text-dim)", textTransform: "uppercase", fontWeight: 700 }}>Job Role</label>
                  <div style={{ fontSize: "16px", fontWeight: 700, color: "var(--text-heading)", marginTop: "2px" }}>
                    {selectedApp.jobTitle}
                  </div>
                </div>
                <div>
                  <label style={{ fontSize: "12px", color: "var(--text-dim)", textTransform: "uppercase", fontWeight: 700 }}>Candidate</label>
                  <div style={{ fontSize: "14px", fontWeight: 600, color: "var(--text-heading)", marginTop: "2px" }}>
                    {selectedApp.userName} ({selectedApp.userEmail})
                  </div>
                </div>
                <div>
                  <label style={{ fontSize: "12px", color: "var(--text-dim)", textTransform: "uppercase", fontWeight: 700 }}>Current Status</label>
                  <div style={{ marginTop: "4px" }}>
                    {getStatusBadge(selectedApp.status)}
                  </div>
                </div>
              </div>

              {selectedApp.jobDescription && (
                <div style={{ marginBottom: "16px" }}>
                  <label style={{ fontSize: "12px", color: "var(--text-dim)", textTransform: "uppercase", fontWeight: 700 }}>Job Description</label>
                  <div style={{ background: "var(--bg-app)", padding: "12px", borderRadius: "var(--radius-md)", fontSize: "13px", lineHeight: 1.5, marginTop: "4px" }}>
                    {selectedApp.jobDescription}
                  </div>
                </div>
              )}

              {selectedApp.notes && (
                <div>
                  <label style={{ fontSize: "12px", color: "var(--text-dim)", textTransform: "uppercase", fontWeight: 700 }}>Candidate Notes</label>
                  <div style={{ background: "var(--bg-app)", padding: "12px", borderRadius: "var(--radius-md)", fontSize: "13px", lineHeight: 1.5, marginTop: "4px" }}>
                    {selectedApp.notes}
                  </div>
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button className="secondary-button" onClick={() => setSelectedApp(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

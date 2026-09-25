import { useState, useEffect, useMemo, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { api } from "../../services/api";
import { useToast } from "../../context/ToastContext";
import {
  Plus,
  Search,
  LayoutList,
  Columns,
  ExternalLink,
  Edit2,
  Trash2,
  Calendar,
  MapPin,
  AlertCircle,
  X,
  RefreshCw,
  Briefcase,
  Eye,
  Clock,
  CheckCircle2,
  XCircle,
  ChevronDown,
  RotateCcw,
} from "lucide-react";
import "./JobTrackerPage.css";

const STATUS_OPTIONS = ["APPLIED", "INTERVIEW", "OFFERED", "REJECTED", "SAVED"];
const JOB_TYPES = ["Full Time", "Part Time", "Contract", "Internship", "Remote"];

export default function JobTrackerPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const toast = useToast();
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [viewMode, setViewMode] = useState("table"); // 'table' | 'kanban'

  // Search & Filter & Sort state
  const [searchQuery, setSearchQuery] = useState(
    () => searchParams.get("search") || ""
  );
  const [statusFilter, setStatusFilter] = useState(
    () => searchParams.get("status") || "ALL"
  );
  const [typeFilter, setTypeFilter] = useState("ALL");
  const [sortBy, setSortBy] = useState("newest"); // 'newest' | 'oldest' | 'company' | 'salary'

  // Modals state
  const [modalOpen, setModalOpen] = useState(false);
  const [editingJob, setEditingJob] = useState(null);
  const [detailsJob, setDetailsJob] = useState(null);
  const [deleteId, setDeleteId] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Form state
  const [formData, setFormData] = useState({
    companyName: "",
    jobRole: "",
    jobDescription: "",
    location: "",
    salary: "",
    jobType: "Full Time",
    jobLink: "",
    status: "APPLIED",
    notes: "",
    appliedDate: new Date().toISOString().split("T")[0],
    interviewDate: "",
    interviewTime: "",
  });

  const openAddModal = () => {
    setEditingJob(null);
    setFormData({
      companyName: "",
      jobRole: "",
      jobDescription: "",
      location: "",
      salary: "",
      jobType: "Full Time",
      jobLink: "",
      status: "APPLIED",
      notes: "",
      appliedDate: new Date().toISOString().split("T")[0],
      interviewDate: "",
      interviewTime: "",
    });
    setModalOpen(true);
  };

  const loadJobs = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const data = await api.getJobs();
      const jobList = Array.isArray(data) ? data : [];
      setJobs(jobList);

      // Check ?view=ID param
      const viewId = searchParams.get("view");
      if (viewId) {
        const targetJob = jobList.find((j) => String(j.id) === String(viewId));
        if (targetJob) {
          setDetailsJob(targetJob);
        }
      }
    } catch (err) {
      console.error("Load jobs error:", err);
      setError(err instanceof Error ? err.message : "Failed to load jobs");
    } finally {
      setLoading(false);
    }
  }, [searchParams]);

  useEffect(() => {
    loadJobs();
    if (searchParams.get("new") === "true") {
      openAddModal();
      setSearchParams({}, { replace: true });
    }
  }, [loadJobs, searchParams, setSearchParams]);

  // Handle ESC key to close open modals
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        if (deleteId) setDeleteId(null);
        else if (detailsJob) setDetailsJob(null);
        else if (modalOpen) setModalOpen(false);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [modalOpen, detailsJob, deleteId]);

  const openEditModal = (job) => {
    setEditingJob(job);
    setFormData({
      companyName: job.companyName || "",
      jobRole: job.jobRole || "",
      jobDescription: job.jobDescription || "",
      location: job.location || "",
      salary: job.salary ? String(job.salary) : "",
      jobType: job.jobType || "Full Time",
      jobLink: job.jobLink || "",
      status: job.status || "APPLIED",
      notes: job.notes || "",
      appliedDate: job.appliedDate || "",
      interviewDate: job.interviewDate || "",
      interviewTime: job.interviewTime || "",
    });
    setModalOpen(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (!formData.companyName.trim() || !formData.jobRole.trim()) {
      toast.warning("Company Name and Job Role are required.");
      return;
    }

    try {
      setSubmitting(true);
      const payload = {
        ...formData,
        salary: formData.salary ? Number(formData.salary) : null,
        appliedDate: formData.appliedDate || null,
        interviewDate: formData.interviewDate || null,
        interviewTime: formData.interviewTime || null,
      };

      if (editingJob) {
        await api.updateJob(editingJob.id, payload);
        toast.success("Job application updated successfully!");
      } else {
        await api.addJob(payload);
        toast.success("Job application added successfully!");
      }

      setModalOpen(false);
      await loadJobs();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to save job application");
    } finally {
      setSubmitting(false);
    }
  };

  const handleQuickStatusChange = async (job, newStatus) => {
    const previousJobs = [...jobs];
    try {
      // Optimistic update
      setJobs((prev) =>
        prev.map((j) => (j.id === job.id ? { ...j, status: newStatus } : j))
      );
      await api.updateJob(job.id, {
        companyName: job.companyName,
        jobRole: job.jobRole,
        jobDescription: job.jobDescription,
        location: job.location,
        salary: job.salary,
        jobType: job.jobType,
        jobLink: job.jobLink,
        status: newStatus,
        notes: job.notes,
        appliedDate: job.appliedDate,
        interviewDate: job.interviewDate,
        interviewTime: job.interviewTime,
      });
      toast.success(`Status updated to ${newStatus}`);
    } catch (err) {
      setJobs(previousJobs);
      toast.error(err instanceof Error ? err.message : "Failed to update status");
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await api.deleteJob(deleteId);
      if (detailsJob?.id === deleteId) {
        setDetailsJob(null);
      }
      setDeleteId(null);
      toast.success("Job application deleted.");
      await loadJobs();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to delete job application");
    }
  };

  // Dynamic Summary Metrics computed from actual active application data
  const summaryCounts = useMemo(() => {
    return {
      total: jobs.length,
      applied: jobs.filter((j) => (j.status || "").toUpperCase() === "APPLIED").length,
      interview: jobs.filter((j) => (j.status || "").toUpperCase() === "INTERVIEW").length,
      offer: jobs.filter((j) => (j.status || "").toUpperCase() === "OFFERED").length,
      rejected: jobs.filter((j) => (j.status || "").toUpperCase() === "REJECTED").length,
    };
  }, [jobs]);

  // Filter and sort jobs
  const filteredJobs = useMemo(() => {
    return jobs
      .filter((job) => {
        const q = searchQuery.toLowerCase().trim();
        const matchesSearch =
          q === "" ||
          job.companyName?.toLowerCase().includes(q) ||
          job.jobRole?.toLowerCase().includes(q) ||
          job.location?.toLowerCase().includes(q) ||
          job.notes?.toLowerCase().includes(q);

        const matchesStatus =
          statusFilter === "ALL" ||
          job.status?.toUpperCase() === statusFilter.toUpperCase();

        const matchesType =
          typeFilter === "ALL" ||
          job.jobType?.toLowerCase() === typeFilter.toLowerCase();

        return matchesSearch && matchesStatus && matchesType;
      })
      .sort((a, b) => {
        if (sortBy === "newest") {
          return new Date(b.appliedDate || 0) - new Date(a.appliedDate || 0);
        }
        if (sortBy === "oldest") {
          return new Date(a.appliedDate || 0) - new Date(b.appliedDate || 0);
        }
        if (sortBy === "company") {
          return (a.companyName || "").localeCompare(b.companyName || "");
        }
        if (sortBy === "salary") {
          return (Number(b.salary) || 0) - (Number(a.salary) || 0);
        }
        return 0;
      });
  }, [jobs, searchQuery, statusFilter, typeFilter, sortBy]);

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    try {
      return new Date(dateStr).toLocaleDateString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric",
      });
    } catch {
      return dateStr;
    }
  };

  const hasActiveFilters = searchQuery || statusFilter !== "ALL" || typeFilter !== "ALL" || sortBy !== "newest";

  const handleResetFilters = () => {
    setSearchQuery("");
    setStatusFilter("ALL");
    setTypeFilter("ALL");
    setSortBy("newest");
  };

  return (
    <div className="jt-apps-container">
      {/* 1. Header */}
      <div className="jt-apps-header">
        <div className="jt-apps-header-left">
          <h2>Applications</h2>
          <p>Track and manage your job applications in one place.</p>
        </div>
        <div className="jt-apps-header-actions">
          <button
            className="jt-btn-secondary"
            onClick={async () => {
              await loadJobs();
              toast.success("Applications refreshed.");
            }}
            disabled={loading}
            title="Refresh applications list"
          >
            <RefreshCw size={14} className={loading ? "spinning" : ""} />
            <span>Refresh</span>
          </button>
          <button className="jt-add-btn" onClick={openAddModal}>
            <Plus size={16} />
            <span>Add Application</span>
          </button>
        </div>
      </div>

      {/* Inline Error Alert */}
      {error && (
        <div className="jt-error-banner">
          <div className="jt-error-content">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
          <button onClick={loadJobs} className="jt-retry-btn">
            Retry
          </button>
        </div>
      )}

      {/* 2. Dynamic Summary Cards */}
      {loading ? (
        <div className="jt-skeleton-metrics">
          <div className="jt-skeleton-card" />
          <div className="jt-skeleton-card" />
          <div className="jt-skeleton-card" />
          <div className="jt-skeleton-card" />
          <div className="jt-skeleton-card" />
        </div>
      ) : (
        <div className="jt-summary-grid">
          {/* Total */}
          <div
            className={`jt-summary-card ${statusFilter === "ALL" ? "active-filter" : ""}`}
            onClick={() => setStatusFilter("ALL")}
            title="Show all applications"
          >
            <div className="jt-summary-info">
              <span className="jt-summary-label">Total Applications</span>
              <span className="jt-summary-value">{summaryCounts.total}</span>
            </div>
            <div className="jt-summary-icon total">
              <Briefcase size={20} />
            </div>
          </div>

          {/* Applied */}
          <div
            className={`jt-summary-card ${statusFilter === "APPLIED" ? "active-filter" : ""}`}
            onClick={() => setStatusFilter(statusFilter === "APPLIED" ? "ALL" : "APPLIED")}
            title="Filter by Applied"
          >
            <div className="jt-summary-info">
              <span className="jt-summary-label">Applied</span>
              <span className="jt-summary-value">{summaryCounts.applied}</span>
            </div>
            <div className="jt-summary-icon applied">
              <Clock size={20} />
            </div>
          </div>

          {/* Interview */}
          <div
            className={`jt-summary-card ${statusFilter === "INTERVIEW" ? "active-filter" : ""}`}
            onClick={() => setStatusFilter(statusFilter === "INTERVIEW" ? "ALL" : "INTERVIEW")}
            title="Filter by Interview"
          >
            <div className="jt-summary-info">
              <span className="jt-summary-label">Interview</span>
              <span className="jt-summary-value">{summaryCounts.interview}</span>
            </div>
            <div className="jt-summary-icon interview">
              <Calendar size={20} />
            </div>
          </div>

          {/* Offer */}
          <div
            className={`jt-summary-card ${statusFilter === "OFFERED" ? "active-filter" : ""}`}
            onClick={() => setStatusFilter(statusFilter === "OFFERED" ? "ALL" : "OFFERED")}
            title="Filter by Offers"
          >
            <div className="jt-summary-info">
              <span className="jt-summary-label">Offers</span>
              <span className="jt-summary-value">{summaryCounts.offer}</span>
            </div>
            <div className="jt-summary-icon offer">
              <CheckCircle2 size={20} />
            </div>
          </div>

          {/* Rejected */}
          <div
            className={`jt-summary-card ${statusFilter === "REJECTED" ? "active-filter" : ""}`}
            onClick={() => setStatusFilter(statusFilter === "REJECTED" ? "ALL" : "REJECTED")}
            title="Filter by Rejected"
          >
            <div className="jt-summary-info">
              <span className="jt-summary-label">Rejected</span>
              <span className="jt-summary-value">{summaryCounts.rejected}</span>
            </div>
            <div className="jt-summary-icon rejected">
              <XCircle size={20} />
            </div>
          </div>
        </div>
      )}

      {/* 3. Search and Filters Bar */}
      <div className="jt-controls-bar">
        <div className="jt-controls-left">
          {/* Search Input */}
          <div className="jt-search-box">
            <Search size={16} className="jt-search-icon" />
            <input
              type="text"
              placeholder="Search applications..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="jt-search-input"
            />
            {searchQuery && (
              <button
                className="jt-search-clear"
                onClick={() => setSearchQuery("")}
                title="Clear search"
              >
                <X size={14} />
              </button>
            )}
          </div>

          {/* Filter Dropdowns */}
          <div className="jt-filters-group">
            {/* Status Dropdown */}
            <div className="jt-select-wrapper">
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="jt-select"
              >
                <option value="ALL">All Statuses</option>
                {STATUS_OPTIONS.map((st) => (
                  <option key={st} value={st}>
                    {st.charAt(0) + st.slice(1).toLowerCase()}
                  </option>
                ))}
              </select>
              <ChevronDown size={14} className="jt-select-chevron" />
            </div>

            {/* Job Type Dropdown */}
            <div className="jt-select-wrapper">
              <select
                value={typeFilter}
                onChange={(e) => setTypeFilter(e.target.value)}
                className="jt-select"
              >
                <option value="ALL">All Job Types</option>
                {JOB_TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select>
              <ChevronDown size={14} className="jt-select-chevron" />
            </div>

            {/* Sort Dropdown */}
            <div className="jt-select-wrapper">
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="jt-select"
              >
                <option value="newest">Sort: Newest First</option>
                <option value="oldest">Sort: Oldest First</option>
                <option value="company">Sort: Company (A-Z)</option>
                <option value="salary">Sort: Highest Salary</option>
              </select>
              <ChevronDown size={14} className="jt-select-chevron" />
            </div>

            {/* Reset Filters CTA if modified */}
            {hasActiveFilters && (
              <button
                className="jt-reset-btn"
                onClick={handleResetFilters}
                title="Reset all filters"
              >
                <RotateCcw size={13} />
                <span>Reset</span>
              </button>
            )}
          </div>
        </div>

        {/* View Toggle */}
        <div className="jt-view-toggle">
          <button
            className={`jt-toggle-btn ${viewMode === "table" ? "active" : ""}`}
            onClick={() => setViewMode("table")}
            title="Table View"
          >
            <LayoutList size={15} />
            <span>Table</span>
          </button>
          <button
            className={`jt-toggle-btn ${viewMode === "kanban" ? "active" : ""}`}
            onClick={() => setViewMode("kanban")}
            title="Board View"
          >
            <Columns size={15} />
            <span>Board</span>
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      {loading ? (
        viewMode === "table" ? (
          <div className="jt-skeleton-table" />
        ) : (
          <div className="jt-kanban-board">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="jt-kanban-col">
                <div className="jt-kanban-col-header">
                  <div className="skeleton-box" style={{ width: "80px", height: "16px" }} />
                </div>
                <div className="jt-kanban-cards-list">
                  <div className="skeleton-box" style={{ height: "90px", width: "100%", borderRadius: "8px" }} />
                  <div className="skeleton-box" style={{ height: "90px", width: "100%", borderRadius: "8px" }} />
                </div>
              </div>
            ))}
          </div>
        )
      ) : filteredJobs.length === 0 ? (
        /* Empty State */
        <div className="jt-empty-card">
          <div className="jt-empty-icon-wrap">
            <Briefcase size={28} />
          </div>
          <h3>
            {jobs.length === 0
              ? "No applications yet"
              : "No matching applications found"}
          </h3>
          <p>
            {jobs.length === 0
              ? "Start tracking your job search by adding your first application."
              : "Try adjusting your search criteria or resetting filters."}
          </p>
          {jobs.length === 0 ? (
            <button className="jt-add-btn" onClick={openAddModal}>
              <Plus size={16} />
              <span>Add Application</span>
            </button>
          ) : (
            <button className="jt-btn-secondary" onClick={handleResetFilters}>
              Reset Filters
            </button>
          )}
        </div>
      ) : viewMode === "table" ? (
        /* 4. TABLE VIEW */
        <div className="jt-table-card">
          <div className="jt-table-responsive">
            <table className="jt-table">
              <thead>
                <tr>
                  <th>Company</th>
                  <th>Job Title</th>
                  <th>Job Type</th>
                  <th>Location</th>
                  <th>Salary</th>
                  <th>Status</th>
                  <th>Applied Date</th>
                  <th>Interview</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredJobs.map((job) => (
                  <tr key={job.id}>
                    <td>
                      <div className="jt-company-cell">
                        <div className="jt-company-badge">
                          {job.companyName?.charAt(0)?.toUpperCase() || "J"}
                        </div>
                        <div className="jt-company-meta">
                          <strong>{job.companyName}</strong>
                          {job.jobLink && (
                            <a
                              href={job.jobLink}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="jt-job-link"
                            >
                              Link <ExternalLink size={11} />
                            </a>
                          )}
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className="jt-role-text">{job.jobRole}</span>
                    </td>
                    <td>
                      <span className="jt-type-badge">
                        {job.jobType || "Full Time"}
                      </span>
                    </td>
                    <td>
                      <span style={{ color: job.location ? "inherit" : "#94A3B8" }}>
                        {job.location || "—"}
                      </span>
                    </td>
                    <td>
                      <span className="jt-salary-text">
                        {job.salary
                          ? `₹${Number(job.salary).toLocaleString()}`
                          : "—"}
                      </span>
                    </td>
                    <td>
                      <div className="jt-status-select-wrap">
                        <select
                          className={`jt-status-select ${job.status?.toLowerCase() || "applied"}`}
                          value={job.status || "APPLIED"}
                          onChange={(e) =>
                            handleQuickStatusChange(job, e.target.value)
                          }
                        >
                          {STATUS_OPTIONS.map((st) => (
                            <option key={st} value={st}>
                              {st.charAt(0) + st.slice(1).toLowerCase()}
                            </option>
                          ))}
                        </select>
                        <ChevronDown size={11} className="jt-status-chevron" />
                      </div>
                    </td>
                    <td style={{ color: "#64748B" }}>
                      {formatDate(job.appliedDate)}
                    </td>
                    <td>
                      {job.interviewDate ? (
                        <div className="jt-interview-badge">
                          <Calendar size={12} />
                          <span>{formatDate(job.interviewDate)}</span>
                          {job.interviewTime && <span>({job.interviewTime})</span>}
                        </div>
                      ) : (
                        <span style={{ color: "#94A3B8" }}>—</span>
                      )}
                    </td>
                    <td style={{ textAlign: "right" }}>
                      <div className="jt-actions-wrap">
                        <button
                          className="jt-action-icon-btn view"
                          onClick={() => setDetailsJob(job)}
                          title="View Details"
                        >
                          <Eye size={14} />
                        </button>
                        <button
                          className="jt-action-icon-btn edit"
                          onClick={() => openEditModal(job)}
                          title="Edit Application"
                        >
                          <Edit2 size={14} />
                        </button>
                        <button
                          className="jt-action-icon-btn delete"
                          onClick={() => setDeleteId(job.id)}
                          title="Delete Application"
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
        </div>
      ) : (
        /* 5. KANBAN / BOARD VIEW */
        <div className="jt-kanban-board">
          {STATUS_OPTIONS.map((columnStatus) => {
            const columnJobs = filteredJobs.filter(
              (j) => (j.status || "APPLIED").toUpperCase() === columnStatus
            );
            return (
              <div key={columnStatus} className="jt-kanban-col">
                <div className="jt-kanban-col-header">
                  <div className="jt-kanban-col-title">
                    <span
                      className={`jt-status-dot ${columnStatus.toLowerCase()}`}
                    />
                    <h3>{columnStatus.charAt(0) + columnStatus.slice(1).toLowerCase()}</h3>
                  </div>
                  <span className="jt-col-count-badge">
                    {columnJobs.length}
                  </span>
                </div>

                <div className="jt-kanban-cards-list">
                  {columnJobs.map((job) => (
                    <div key={job.id} className="jt-kanban-card">
                      <div className="jt-kanban-card-top">
                        <span className="jt-kanban-company">{job.companyName}</span>
                        <div className="jt-actions-wrap">
                          <button
                            className="jt-action-icon-btn view"
                            onClick={() => setDetailsJob(job)}
                            title="View Details"
                          >
                            <Eye size={12} />
                          </button>
                          <button
                            className="jt-action-icon-btn edit"
                            onClick={() => openEditModal(job)}
                            title="Edit"
                          >
                            <Edit2 size={12} />
                          </button>
                          <button
                            className="jt-action-icon-btn delete"
                            onClick={() => setDeleteId(job.id)}
                            title="Delete"
                          >
                            <Trash2 size={12} />
                          </button>
                        </div>
                      </div>

                      <div className="jt-kanban-role">{job.jobRole}</div>

                      <div className="jt-kanban-meta-tags">
                        {job.location && (
                          <span className="jt-kanban-tag">
                            <MapPin size={10} /> {job.location}
                          </span>
                        )}
                        {job.jobType && (
                          <span className="jt-kanban-tag">{job.jobType}</span>
                        )}
                        {job.salary && (
                          <span className="jt-kanban-tag" style={{ fontWeight: 600, color: "#0F172A" }}>
                            ₹{Number(job.salary).toLocaleString()}
                          </span>
                        )}
                      </div>

                      {job.interviewDate && (
                        <div className="jt-kanban-interview-notice">
                          <Calendar size={12} />
                          <span>
                            {formatDate(job.interviewDate)}{" "}
                            {job.interviewTime && `at ${job.interviewTime}`}
                          </span>
                        </div>
                      )}

                      <div className="jt-kanban-card-footer">
                        <span>Applied {formatDate(job.appliedDate)}</span>
                        <select
                          className="jt-kanban-status-select"
                          value={job.status || "APPLIED"}
                          onChange={(e) =>
                            handleQuickStatusChange(job, e.target.value)
                          }
                        >
                          {STATUS_OPTIONS.map((st) => (
                            <option key={st} value={st}>
                              Move: {st.charAt(0) + st.slice(1).toLowerCase()}
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>
                  ))}

                  {columnJobs.length === 0 && (
                    <div className="jt-kanban-empty-col">
                      No applications in this stage
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* 6. Add / Edit Modal */}
      {modalOpen && (
        <div className="jt-modal-overlay" onClick={() => setModalOpen(false)}>
          <div className="jt-modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="jt-modal-header">
              <div className="jt-modal-header-left">
                <h3>{editingJob ? "Edit Application" : "Add Application"}</h3>
                <p>
                  {editingJob
                    ? "Update details for this job opportunity."
                    : "Fill in the details to start tracking a new opportunity."}
                </p>
              </div>
              <button
                className="jt-modal-close-btn"
                onClick={() => setModalOpen(false)}
                aria-label="Close modal"
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleFormSubmit} style={{ display: "contents" }}>
              <div className="jt-modal-body">
                {/* Job Information */}
                <div className="jt-form-section">
                  <div className="jt-section-title">Job Information</div>
                  <div className="jt-form-grid-2">
                    <div className="jt-field-group">
                      <label>
                        Company Name <span className="req">*</span>
                      </label>
                      <input
                        type="text"
                        required
                        placeholder="e.g. Google, Stripe"
                        value={formData.companyName}
                        onChange={(e) =>
                          setFormData({ ...formData, companyName: e.target.value })
                        }
                        className="jt-input"
                      />
                    </div>

                    <div className="jt-field-group">
                      <label>
                        Job Title / Role <span className="req">*</span>
                      </label>
                      <input
                        type="text"
                        required
                        placeholder="e.g. Senior Frontend Engineer"
                        value={formData.jobRole}
                        onChange={(e) =>
                          setFormData({ ...formData, jobRole: e.target.value })
                        }
                        className="jt-input"
                      />
                    </div>
                  </div>

                  <div className="jt-form-grid-3">
                    <div className="jt-field-group">
                      <label>Location</label>
                      <input
                        type="text"
                        placeholder="e.g. Bengaluru, Remote"
                        value={formData.location}
                        onChange={(e) =>
                          setFormData({ ...formData, location: e.target.value })
                        }
                        className="jt-input"
                      />
                    </div>

                    <div className="jt-field-group">
                      <label>Job Type</label>
                      <select
                        value={formData.jobType}
                        onChange={(e) =>
                          setFormData({ ...formData, jobType: e.target.value })
                        }
                        className="jt-form-select"
                      >
                        {JOB_TYPES.map((t) => (
                          <option key={t} value={t}>
                            {t}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="jt-field-group">
                      <label>Salary (Annual INR)</label>
                      <input
                        type="number"
                        placeholder="e.g. 1500000"
                        value={formData.salary}
                        onChange={(e) =>
                          setFormData({ ...formData, salary: e.target.value })
                        }
                        className="jt-input"
                      />
                    </div>
                  </div>
                </div>

                {/* Application Information */}
                <div className="jt-form-section">
                  <div className="jt-section-title">Application Details</div>
                  <div className="jt-form-grid-2">
                    <div className="jt-field-group">
                      <label>Status</label>
                      <select
                        value={formData.status}
                        onChange={(e) =>
                          setFormData({ ...formData, status: e.target.value })
                        }
                        className="jt-form-select"
                      >
                        {STATUS_OPTIONS.map((st) => (
                          <option key={st} value={st}>
                            {st.charAt(0) + st.slice(1).toLowerCase()}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="jt-field-group">
                      <label>Applied Date</label>
                      <input
                        type="date"
                        value={formData.appliedDate}
                        onChange={(e) =>
                          setFormData({ ...formData, appliedDate: e.target.value })
                        }
                        className="jt-input"
                      />
                    </div>
                  </div>

                  <div className="jt-field-group">
                    <label>Job Posting URL</label>
                    <input
                      type="url"
                      placeholder="https://jobs.example.com/posting/123"
                      value={formData.jobLink}
                      onChange={(e) =>
                        setFormData({ ...formData, jobLink: e.target.value })
                      }
                      className="jt-input"
                    />
                  </div>
                </div>

                {/* Interview Information (Optional) */}
                <div className="jt-form-section">
                  <div className="jt-section-title">Interview Schedule (Optional)</div>
                  <div className="jt-form-grid-2">
                    <div className="jt-field-group">
                      <label>Interview Date</label>
                      <input
                        type="date"
                        value={formData.interviewDate}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            interviewDate: e.target.value,
                          })
                        }
                        className="jt-input"
                      />
                    </div>

                    <div className="jt-field-group">
                      <label>Interview Time</label>
                      <input
                        type="text"
                        placeholder="e.g. 11:00 AM IST"
                        value={formData.interviewTime}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            interviewTime: e.target.value,
                          })
                        }
                        className="jt-input"
                      />
                    </div>
                  </div>
                </div>

                {/* Additional Information */}
                <div className="jt-form-section">
                  <div className="jt-section-title">Additional Information</div>
                  <div className="jt-field-group">
                    <label>Job Description & Requirements</label>
                    <textarea
                      placeholder="Key responsibilities, required skills, tech stack..."
                      value={formData.jobDescription}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          jobDescription: e.target.value,
                        })
                      }
                      className="jt-textarea"
                      rows={3}
                    />
                  </div>

                  <div className="jt-field-group">
                    <label>Personal Notes & Next Steps</label>
                    <textarea
                      placeholder="Recruiter contact info, referral notes, questions to ask..."
                      value={formData.notes}
                      onChange={(e) =>
                        setFormData({ ...formData, notes: e.target.value })
                      }
                      className="jt-textarea"
                      rows={2}
                    />
                  </div>
                </div>
              </div>

              <div className="jt-modal-footer">
                <button
                  type="button"
                  className="jt-btn-secondary"
                  onClick={() => setModalOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="jt-btn-primary"
                  disabled={submitting}
                >
                  {submitting ? (
                    <>
                      <RefreshCw size={14} className="spinning" />
                      <span>Saving...</span>
                    </>
                  ) : editingJob ? (
                    "Save Changes"
                  ) : (
                    "Add Application"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 7. Details Modal */}
      {detailsJob && (
        <div className="jt-modal-overlay" onClick={() => setDetailsJob(null)}>
          <div className="jt-modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="jt-modal-header">
              <div className="jt-modal-header-left">
                <h3>{detailsJob.companyName}</h3>
                <p>{detailsJob.jobRole}</p>
              </div>
              <button
                className="jt-modal-close-btn"
                onClick={() => setDetailsJob(null)}
                aria-label="Close modal"
              >
                <X size={18} />
              </button>
            </div>

            <div className="jt-modal-body">
              <div className="jt-details-grid">
                <div className="jt-detail-card">
                  <span className="jt-detail-card-label">Status</span>
                  <div className="jt-detail-card-val">
                    <span className={`jt-status-select ${detailsJob.status?.toLowerCase() || "applied"}`}>
                      {detailsJob.status}
                    </span>
                  </div>
                </div>

                <div className="jt-detail-card">
                  <span className="jt-detail-card-label">Job Type</span>
                  <div className="jt-detail-card-val">
                    {detailsJob.jobType || "Full Time"}
                  </div>
                </div>

                <div className="jt-detail-card">
                  <span className="jt-detail-card-label">Location</span>
                  <div className="jt-detail-card-val">
                    <MapPin size={13} style={{ color: "#64748B" }} />
                    {detailsJob.location || "Not specified"}
                  </div>
                </div>

                <div className="jt-detail-card">
                  <span className="jt-detail-card-label">Salary</span>
                  <div className="jt-detail-card-val">
                    {detailsJob.salary
                      ? `₹${Number(detailsJob.salary).toLocaleString()}`
                      : "Not disclosed"}
                  </div>
                </div>

                <div className="jt-detail-card">
                  <span className="jt-detail-card-label">Applied Date</span>
                  <div className="jt-detail-card-val">
                    {formatDate(detailsJob.appliedDate)}
                  </div>
                </div>

                <div className="jt-detail-card">
                  <span className="jt-detail-card-label">Interview</span>
                  <div className="jt-detail-card-val">
                    {detailsJob.interviewDate ? (
                      <>
                        <Calendar size={13} style={{ color: "#0284C7" }} />
                        <span>
                          {formatDate(detailsJob.interviewDate)}{" "}
                          {detailsJob.interviewTime && `at ${detailsJob.interviewTime}`}
                        </span>
                      </>
                    ) : (
                      <span style={{ color: "#94A3B8" }}>None scheduled</span>
                    )}
                  </div>
                </div>
              </div>

              {detailsJob.jobLink && (
                <div className="jt-detail-box-card">
                  <span className="jt-detail-card-label">Job Posting</span>
                  <a
                    href={detailsJob.jobLink}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="jt-job-link"
                    style={{ fontSize: "13.5px" }}
                  >
                    {detailsJob.jobLink} <ExternalLink size={13} />
                  </a>
                </div>
              )}

              {detailsJob.jobDescription && (
                <div className="jt-detail-box-card">
                  <span className="jt-detail-card-label">Job Description</span>
                  <div className="jt-detail-box-content">
                    {detailsJob.jobDescription}
                  </div>
                </div>
              )}

              {detailsJob.notes && (
                <div className="jt-detail-box-card notes">
                  <span className="jt-detail-card-label">Personal Notes</span>
                  <div className="jt-detail-box-content">{detailsJob.notes}</div>
                </div>
              )}
            </div>

            <div className="jt-modal-footer">
              <button
                className="jt-btn-secondary"
                onClick={() => {
                  const jobToEdit = detailsJob;
                  setDetailsJob(null);
                  openEditModal(jobToEdit);
                }}
              >
                <Edit2 size={14} style={{ display: "inline", marginRight: "6px" }} />
                <span>Edit Application</span>
              </button>
              <button
                className="jt-btn-primary"
                onClick={() => setDetailsJob(null)}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 8. Delete Confirmation Modal */}
      {deleteId && (
        <div className="jt-modal-overlay" onClick={() => setDeleteId(null)}>
          <div
            className="jt-delete-modal-card"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="jt-delete-icon-wrap">
              <AlertCircle size={28} />
            </div>
            <h3>Delete Application?</h3>
            <p>
              Are you sure you want to delete this job application? This action
              cannot be undone.
            </p>
            <div className="jt-delete-actions">
              <button
                className="jt-btn-secondary"
                onClick={() => setDeleteId(null)}
              >
                Cancel
              </button>
              <button className="jt-btn-danger" onClick={handleDelete}>
                Yes, Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

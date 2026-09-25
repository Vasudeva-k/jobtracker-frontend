import { useEffect, useState, useMemo, useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import { api } from "../../services/api";
import {
  Briefcase,
  Calendar,
  CheckCircle2,
  Clock,
  XCircle,
  TrendingUp,
  Sparkles,
  FileText,
  Plus,
  ArrowRight,
  RefreshCw,
  AlertCircle,
  Building,
  HelpCircle,
  Map,
  Target,
  DollarSign,
  MessageSquare,
  Award,
} from "lucide-react";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from "recharts";

const STATUS_COLORS = {
  APPLIED: "#F59E0B",
  INTERVIEW: "#3B82F6",
  OFFERED: "#10B981",
  REJECTED: "#EF4444",
  SAVED: "#8B5CF6",
};

const MONTH_NAMES = {
  JANUARY: "Jan",
  FEBRUARY: "Feb",
  MARCH: "Mar",
  APRIL: "Apr",
  MAY: "May",
  JUNE: "Jun",
  JULY: "Jul",
  AUGUST: "Aug",
  SEPTEMBER: "Sep",
  OCTOBER: "Oct",
  NOVEMBER: "Nov",
  DECEMBER: "Dec",
};

export default function UserDashboardPage() {
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [stats, setStats] = useState({
    totalJobs: 0,
    applied: 0,
    interview: 0,
    offer: 0,
    rejected: 0,
    saved: 0,
  });

  const [analytics, setAnalytics] = useState({
    monthlyApplications: [],
    statusAnalytics: [],
    topCompanies: [],
    topJobRoles: [],
  });

  const [recentJobs, setRecentJobs] = useState([]);
  const [todayInterviews, setTodayInterviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDashboardData = useCallback(async (isManual = false) => {
    try {
      setLoading(true);
      setError("");

      const [statsData, analyticsData, jobsData, todayData] =
        await Promise.all([
          api.getDashboardStats().catch(() => ({})),
          api.getDashboardAnalytics().catch(() => ({})),
          api.getJobs().catch(() => []),
          api.getTodayInterviews().catch(() => []),
        ]);

      if (statsData) {
        setStats({
          totalJobs: statsData.totalJobs || 0,
          applied: statsData.applied || 0,
          interview: statsData.interview || 0,
          offer: statsData.offer || 0,
          rejected: statsData.rejected || 0,
          saved: statsData.saved || 0,
        });
      }

      if (analyticsData) {
        setAnalytics({
          monthlyApplications: Array.isArray(analyticsData.monthlyApplications)
            ? analyticsData.monthlyApplications
            : [],
          statusAnalytics: Array.isArray(analyticsData.statusAnalytics)
            ? analyticsData.statusAnalytics
            : [],
          topCompanies: Array.isArray(analyticsData.topCompanies)
            ? analyticsData.topCompanies
            : [],
          topJobRoles: Array.isArray(analyticsData.topJobRoles)
            ? analyticsData.topJobRoles
            : [],
        });
      }

      if (Array.isArray(jobsData)) {
        setRecentJobs(jobsData.slice(0, 6));
      }

      if (Array.isArray(todayData)) {
        setTodayInterviews(todayData);
      }

      if (isManual) {
        toast.success("Dashboard data refreshed.");
      }
    } catch (err) {
      console.error("Dashboard error:", err);
      const msg = err instanceof Error ? err.message : "Failed to load dashboard data";
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => {
    loadDashboardData();
  }, [loadDashboardData]);

  const statusChartData = useMemo(() => {
    if (analytics.statusAnalytics.length > 0) {
      return analytics.statusAnalytics
        .filter((item) => item && item.status)
        .map((item) => ({
          name: item.status || "Unknown",
          value: Number(item.count) || 0,
        }))
        .filter((item) => item.value > 0);
    }
    return [
      { name: "APPLIED", value: stats.applied },
      { name: "INTERVIEW", value: stats.interview },
      { name: "OFFERED", value: stats.offer },
      { name: "REJECTED", value: stats.rejected },
      { name: "SAVED", value: stats.saved },
    ].filter((item) => item.value > 0);
  }, [analytics.statusAnalytics, stats]);

  const monthlyChartData = useMemo(() => {
    return analytics.monthlyApplications.map((item) => {
      const formattedMonth =
        MONTH_NAMES[String(item.month).toUpperCase()] || String(item.month);
      return {
        month: formattedMonth,
        applications: Number(item.applications ?? item.totalApplications ?? item.count ?? 0),
      };
    });
  }, [analytics.monthlyApplications]);

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
      case "SAVED":
        return <span className="status-pill saved">Saved</span>;
      default:
        return <span className="status-pill">{status || "Unknown"}</span>;
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

  const aiTools = [
    {
      title: "Cover Letter",
      desc: "Generate tailored, ATS-friendly cover letters instantly.",
      icon: FileText,
      tab: "cover-letter",
      color: "primary",
    },
    {
      title: "Interview Prep",
      desc: "Practice company-specific technical and behavioral questions.",
      icon: HelpCircle,
      tab: "interview",
      color: "info",
    },
    {
      title: "Career Roadmap",
      desc: "Step-by-step milestones to land your target engineering role.",
      icon: Map,
      tab: "roadmap",
      color: "purple",
    },
    {
      title: "Skill Gap Analysis",
      desc: "Identify missing skills and get structured learning timelines.",
      icon: Target,
      tab: "skill-gap",
      color: "warning",
    },
    {
      title: "Salary Estimator",
      desc: "Accurate compensation ranges based on your experience.",
      icon: DollarSign,
      tab: "salary",
      color: "success",
    },
    {
      title: "Resume Matcher",
      desc: "Compare your resume against any job description for keywords.",
      icon: Sparkles,
      tab: "job-match",
      color: "primary",
    },
    {
      title: "AI Career Chat",
      desc: "Interactive career coach for resumes, negotiation, and strategy.",
      icon: MessageSquare,
      tab: "chat",
      color: "purple",
    },
    {
      title: "Success Predictor",
      desc: "Calculate interview probability based on candidate credentials.",
      icon: Award,
      tab: "success",
      color: "info",
    },
  ];

  return (
    <div className="page-container">
      {/* Top Greeting Header */}
      <div className="page-heading">
        <div>
          <h2>Hello, {user?.firstName || "Job Seeker"} 👋</h2>
          <p>Here&apos;s what&apos;s happening with your job search today.</p>
        </div>
        <div className="heading-actions">
          <button
            className="secondary-button"
            onClick={() => loadDashboardData(true)}
            disabled={loading}
            title="Refresh dashboard metrics"
          >
            <RefreshCw size={15} className={loading ? "spinning" : ""} />
            <span>Refresh</span>
          </button>
          <Link to="/user/applications?new=true" className="primary-button">
            <Plus size={16} />
            <span>New Application</span>
          </Link>
        </div>
      </div>

      {error && (
        <div className="dashboard-alert error">
          <AlertCircle size={18} />
          <div>
            <strong>Unable to load dashboard</strong>
            <p>{error}</p>
          </div>
          <button onClick={() => loadDashboardData(true)} className="secondary-button" style={{ marginLeft: "auto" }}>
            Retry
          </button>
        </div>
      )}

      {/* 5 Metric Cards */}
      <div className="metric-cards-grid">
        <div
          className="metric-card interactive"
          onClick={() => navigate("/user/applications")}
          title="Click to view all applications"
          role="button"
          tabIndex={0}
          onKeyDown={(e) => e.key === "Enter" && navigate("/user/applications")}
        >
          <div className="metric-card-info">
            <span className="metric-card-label">Total Applications</span>
            {loading ? (
              <div className="skeleton-box" style={{ height: "32px", width: "50px", margin: "4px 0" }} />
            ) : (
              <span className="metric-card-value">{stats.totalJobs}</span>
            )}
            <span className="metric-card-sub">All tracked opportunities</span>
          </div>
          <div className="metric-card-icon primary">
            <Briefcase size={24} />
          </div>
        </div>

        <div
          className="metric-card interactive"
          onClick={() => navigate("/user/applications?status=INTERVIEW")}
          title="Click to view interview applications"
          role="button"
          tabIndex={0}
          onKeyDown={(e) => e.key === "Enter" && navigate("/user/applications?status=INTERVIEW")}
        >
          <div className="metric-card-info">
            <span className="metric-card-label">Interviews</span>
            {loading ? (
              <div className="skeleton-box" style={{ height: "32px", width: "40px", margin: "4px 0" }} />
            ) : (
              <span className="metric-card-value">{stats.interview}</span>
            )}
            <span className="metric-card-sub">Active interview rounds</span>
          </div>
          <div className="metric-card-icon info">
            <Calendar size={24} />
          </div>
        </div>

        <div
          className="metric-card interactive"
          onClick={() => navigate("/user/applications?status=OFFERED")}
          title="Click to view job offers"
          role="button"
          tabIndex={0}
          onKeyDown={(e) => e.key === "Enter" && navigate("/user/applications?status=OFFERED")}
        >
          <div className="metric-card-info">
            <span className="metric-card-label">Offers</span>
            {loading ? (
              <div className="skeleton-box" style={{ height: "32px", width: "40px", margin: "4px 0" }} />
            ) : (
              <span className="metric-card-value">{stats.offer}</span>
            )}
            <span className="metric-card-sub">Job offers received</span>
          </div>
          <div className="metric-card-icon success">
            <CheckCircle2 size={24} />
          </div>
        </div>

        <div
          className="metric-card interactive"
          onClick={() => navigate("/user/applications?status=APPLIED")}
          title="Click to view applied applications"
          role="button"
          tabIndex={0}
          onKeyDown={(e) => e.key === "Enter" && navigate("/user/applications?status=APPLIED")}
        >
          <div className="metric-card-info">
            <span className="metric-card-label">Pending / Applied</span>
            {loading ? (
              <div className="skeleton-box" style={{ height: "32px", width: "40px", margin: "4px 0" }} />
            ) : (
              <span className="metric-card-value">{stats.applied}</span>
            )}
            <span className="metric-card-sub">Awaiting response</span>
          </div>
          <div className="metric-card-icon warning">
            <Clock size={24} />
          </div>
        </div>

        <div
          className="metric-card interactive"
          onClick={() => navigate("/user/applications?status=REJECTED")}
          title="Click to view rejected applications"
          role="button"
          tabIndex={0}
          onKeyDown={(e) => e.key === "Enter" && navigate("/user/applications?status=REJECTED")}
        >
          <div className="metric-card-info">
            <span className="metric-card-label">Rejections</span>
            {loading ? (
              <div className="skeleton-box" style={{ height: "32px", width: "40px", margin: "4px 0" }} />
            ) : (
              <span className="metric-card-value">{stats.rejected}</span>
            )}
            <span className="metric-card-sub">Closed / Archived</span>
          </div>
          <div className="metric-card-icon danger">
            <XCircle size={24} />
          </div>
        </div>
      </div>

      {/* Upcoming Interviews Alert Card */}
      {todayInterviews.length > 0 && (
        <div className="content-card" style={{ borderLeft: "4px solid var(--info)" }}>
          <div className="card-header" style={{ borderBottom: "none", marginBottom: "12px", paddingBottom: 0 }}>
            <h3>
              <Calendar size={20} color="var(--info)" />
              Scheduled Interviews Today ({todayInterviews.length})
            </h3>
            <Link to="/user/applications?status=INTERVIEW" className="secondary-button" style={{ padding: "6px 12px", fontSize: "13px" }}>
              View Interviews
            </Link>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: "12px" }}>
            {todayInterviews.map((job) => (
              <div
                key={job.id}
                style={{
                  background: "var(--bg-surface-subtle)",
                  padding: "14px 16px",
                  borderRadius: "var(--radius-md)",
                  border: "1px solid var(--border-subtle)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                }}
              >
                <div>
                  <strong>{job.companyName}</strong>
                  <div style={{ fontSize: "13px", color: "var(--text-muted)", marginTop: "2px" }}>
                    {job.jobRole} • {job.location || "Online"}
                  </div>
                </div>
                <span className="status-pill interview">
                  <Clock size={12} />
                  {job.interviewTime || "Scheduled"}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Analytics Charts Grid */}
      <div className="dashboard-grid-2">
        {/* Status Distribution Donut */}
        <div className="content-card">
          <div className="card-header">
            <h3>
              <TrendingUp size={20} color="var(--primary-600)" />
              Application Status Overview
            </h3>
          </div>

          {loading ? (
            <div style={{ height: "260px", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <div className="skeleton-box" style={{ width: "190px", height: "190px", borderRadius: "50%" }} />
            </div>
          ) : statusChartData.length > 0 ? (
            <div style={{ height: "260px", width: "100%" }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={statusChartData}
                    cx="50%"
                    cy="50%"
                    innerRadius={65}
                    outerRadius={95}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {statusChartData.map((entry) => (
                      <Cell
                        key={`cell-${entry.name}`}
                        fill={STATUS_COLORS[entry.name.toUpperCase()] || "#6366F1"}
                      />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#0B132B",
                      border: "none",
                      borderRadius: "8px",
                      color: "#FFFFFF",
                      fontSize: "13px",
                    }}
                  />
                </PieChart>
              </ResponsiveContainer>
              <div
                style={{
                  display: "flex",
                  flexWrap: "wrap",
                  justifyContent: "center",
                  gap: "14px",
                  marginTop: "8px",
                }}
              >
                {statusChartData.map((item) => (
                  <div key={item.name} style={{ display: "flex", alignItems: "center", gap: "6px", fontSize: "13px" }}>
                    <span
                      style={{
                        width: "10px",
                        height: "10px",
                        borderRadius: "50%",
                        backgroundColor: STATUS_COLORS[item.name.toUpperCase()] || "#6366F1",
                      }}
                    />
                    <span style={{ color: "var(--text-muted)", fontWeight: 600 }}>{item.name}:</span>
                    <strong style={{ color: "var(--text-heading)" }}>{item.value}</strong>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="empty-state-box" style={{ padding: "36px 0" }}>
              <div className="empty-state-icon">
                <Briefcase size={24} />
              </div>
              <h4>No applications yet</h4>
              <p>Start tracking your job search by adding your first application.</p>
              <Link to="/user/applications?new=true" className="primary-button">
                Add Application
              </Link>
            </div>
          )}
        </div>

        {/* Monthly Activity Trend */}
        <div className="content-card">
          <div className="card-header">
            <h3>
              <Calendar size={20} color="var(--primary-600)" />
              Application Trend
            </h3>
          </div>

          {loading ? (
            <div style={{ height: "290px", display: "flex", alignItems: "flex-end", gap: "14px", padding: "20px" }}>
              <div className="skeleton-box" style={{ height: "40%", flex: 1, borderRadius: "6px 6px 0 0" }} />
              <div className="skeleton-box" style={{ height: "70%", flex: 1, borderRadius: "6px 6px 0 0" }} />
              <div className="skeleton-box" style={{ height: "55%", flex: 1, borderRadius: "6px 6px 0 0" }} />
              <div className="skeleton-box" style={{ height: "85%", flex: 1, borderRadius: "6px 6px 0 0" }} />
              <div className="skeleton-box" style={{ height: "60%", flex: 1, borderRadius: "6px 6px 0 0" }} />
            </div>
          ) : monthlyChartData.length > 0 ? (
            <div style={{ height: "290px", width: "100%" }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={monthlyChartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" vertical={false} />
                  <XAxis dataKey="month" stroke="#94A3B8" fontSize={12} tickLine={false} />
                  <YAxis stroke="#94A3B8" fontSize={12} tickLine={false} allowDecimals={false} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#0B132B",
                      border: "none",
                      borderRadius: "8px",
                      color: "#FFFFFF",
                      fontSize: "13px",
                    }}
                  />
                  <Bar dataKey="applications" fill="#6366F1" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="empty-state-box" style={{ padding: "36px 0" }}>
              <div className="empty-state-icon">
                <TrendingUp size={24} />
              </div>
              <h4>Not enough application data yet</h4>
              <p>Monthly trends will visualize here as you submit and record job applications.</p>
            </div>
          )}
        </div>
      </div>

      {/* Recent Applications Table */}
      <div className="content-card">
        <div className="card-header">
          <h3>
            <Briefcase size={20} color="var(--primary-600)" />
            Recent Applications
          </h3>
          <Link to="/user/applications" className="secondary-button" style={{ padding: "6px 14px", fontSize: "13px" }}>
            View All Applications
            <ArrowRight size={14} />
          </Link>
        </div>

        {loading ? (
          <div style={{ display: "flex", flexDirection: "column", gap: "12px", padding: "12px 0" }}>
            <div className="skeleton-box" style={{ height: "48px", width: "100%" }} />
            <div className="skeleton-box" style={{ height: "48px", width: "100%" }} />
            <div className="skeleton-box" style={{ height: "48px", width: "100%" }} />
          </div>
        ) : recentJobs.length > 0 ? (
          <div className="table-responsive">
            <table className="saas-table">
              <thead>
                <tr>
                  <th>Job Title &amp; Company</th>
                  <th>Status</th>
                  <th>Applied Date</th>
                  <th>Location / Type</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {recentJobs.map((job) => (
                  <tr key={job.id}>
                    <td>
                      <div className="company-cell">
                        <div className="company-logo-placeholder">
                          {job.companyName?.charAt(0)?.toUpperCase() || "J"}
                        </div>
                        <div className="company-details">
                          <strong>{job.jobRole || "Software Position"}</strong>
                          <span>{job.companyName}</span>
                        </div>
                      </div>
                    </td>
                    <td>{getStatusBadge(job.status)}</td>
                    <td>{formatDate(job.appliedDate)}</td>
                    <td style={{ color: "var(--text-muted)" }}>
                      {job.location || "Remote"} • {job.jobType || "Full Time"}
                    </td>
                    <td style={{ textAlign: "right" }}>
                      <button
                        onClick={() => navigate(`/user/applications?view=${job.id}`)}
                        className="secondary-button"
                        style={{ padding: "5px 12px", fontSize: "12px" }}
                      >
                        Manage
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state-box">
            <div className="empty-state-icon">
              <Building size={24} />
            </div>
            <h4>No recent applications</h4>
            <p>Ready to apply? Click below to record your first job application.</p>
            <Link to="/user/applications?new=true" className="primary-button">
              <Plus size={15} />
              Add Application
            </Link>
          </div>
        )}
      </div>

      {/* AI Career Toolkit Quick Launch */}
      <div className="content-card">
        <div className="card-header">
          <div>
            <h3>
              <Sparkles size={20} color="var(--primary-600)" />
              AI Career Toolkit
            </h3>
            <p style={{ fontSize: "13px", color: "var(--text-muted)", marginTop: "2px" }}>
              Intelligent generative tools to supercharge your interview preparation and job matching
            </p>
          </div>
          <Link to="/user/ai" className="secondary-button" style={{ padding: "6px 14px", fontSize: "13px" }}>
            Open Workspace
            <ArrowRight size={14} />
          </Link>
        </div>

        <div className="ai-grid">
          {aiTools.map((tool) => {
            const Icon = tool.icon;
            return (
              <div key={tool.title} className="ai-card">
                <div className="ai-card-top">
                  <div className={`ai-card-icon ${tool.color}`}>
                    <Icon size={22} />
                  </div>
                  <div className="ai-card-text">
                    <h4>{tool.title}</h4>
                    <p>{tool.desc}</p>
                  </div>
                </div>
                <Link to={`/user/ai?tab=${tool.tab}`} className="ai-card-cta">
                  <span>Open Tool</span>
                  <ArrowRight size={14} />
                </Link>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

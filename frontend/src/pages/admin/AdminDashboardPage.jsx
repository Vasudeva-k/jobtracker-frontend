import { useEffect, useState, useMemo } from "react";
import { api } from "../../services/api";
import {
  Users,
  Briefcase,
  Clock,
  Calendar,
  CheckCircle2,
  XCircle,
  RefreshCw,
  AlertCircle,
  Building,
  TrendingUp,
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

export default function AdminDashboardPage() {
  const [summary, setSummary] = useState({
    totalUsers: 0,
    totalApplications: 0,
    applied: 0,
    interviews: 0,
    offers: 0,
    rejected: 0,
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [monthlyJobs, setMonthlyJobs] = useState([]);
  const [monthlyUsers, setMonthlyUsers] = useState([]);
  const [topCompanies, setTopCompanies] = useState([]);
  const [topJobRoles, setTopJobRoles] = useState([]);
  const [jobStatus, setJobStatus] = useState([]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError("");

      const [
        summaryData,
        monthlyJobsData,
        monthlyUsersData,
        companiesData,
        rolesData,
        statusData,
      ] = await Promise.all([
        api.getAdminAnalyticsSummary().catch(() => ({})),
        api.getAdminMonthlyJobs().catch(() => []),
        api.getAdminMonthlyUsers().catch(() => []),
        api.getAdminTopCompanies().catch(() => []),
        api.getAdminTopJobRoles().catch(() => []),
        api.getAdminJobStatus().catch(() => []),
      ]);

      setSummary({
        totalUsers: summaryData.totalUsers ?? 0,
        totalApplications: summaryData.totalApplications ?? 0,
        applied: summaryData.applied ?? 0,
        interviews: summaryData.interviews ?? 0,
        offers: summaryData.offers ?? 0,
        rejected: summaryData.rejected ?? 0,
      });

      setMonthlyJobs(Array.isArray(monthlyJobsData) ? monthlyJobsData : []);
      setMonthlyUsers(Array.isArray(monthlyUsersData) ? monthlyUsersData : []);
      setTopCompanies(Array.isArray(companiesData) ? companiesData : []);
      setTopJobRoles(Array.isArray(rolesData) ? rolesData : []);
      setJobStatus(Array.isArray(statusData) ? statusData : []);
    } catch (err) {
      console.error("Admin dashboard error:", err);
      setError(err instanceof Error ? err.message : "Failed to load dashboard data");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  const statusChartData = useMemo(() => {
    return jobStatus
      .filter((item) => item && item.status)
      .map((item) => ({
        name: String(item.status),
        value: Number(item.count) || 0,
      }))
      .filter((item) => item.value > 0);
  }, [jobStatus]);

  const companyChartData = useMemo(() => {
    return topCompanies
      .filter((item) => item && item.company)
      .map((item) => ({
        company: String(item.company),
        totalApplications: Number(item.totalApplications) || 0,
      }))
      .filter((item) => item.totalApplications > 0);
  }, [topCompanies]);

  const roleChartData = useMemo(() => {
    return topJobRoles
      .filter((item) => item && (item.jobRole || item.role))
      .map((item) => ({
        role: String(item.jobRole || item.role),
        totalApplications: Number(item.totalApplications || item.count) || 0,
      }))
      .filter((item) => item.totalApplications > 0);
  }, [topJobRoles]);

  const monthlyChartData = useMemo(() => {
    const monthNames = [
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    ];
    return monthlyJobs.map((item) => ({
      month:
        typeof item.month === "number"
          ? monthNames[item.month - 1] || `M${item.month}`
          : item.month,
      applications: Number(item.totalApplications || item.count || 0),
    }));
  }, [monthlyJobs]);

  const monthlyUsersChartData = useMemo(() => {
    const monthNames = [
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    ];
    return monthlyUsers.map((item) => ({
      month:
        typeof item.month === "number"
          ? monthNames[item.month - 1] || `M${item.month}`
          : item.month,
      users: Number(item.totalUsers || item.count || 0),
    }));
  }, [monthlyUsers]);

  return (
    <div className="page-container">
      <div className="page-heading">
        <div>
          <h2>System Overview & Platform Telemetry</h2>
          <p>Live administrative analytics, user acquisition, and application metrics</p>
        </div>
        <div className="heading-actions">
          <button
            className="secondary-button"
            onClick={loadDashboardData}
            disabled={loading}
          >
            <RefreshCw size={15} className={loading ? "spinning" : ""} />
            <span>Refresh Analytics</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="dashboard-alert error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* 6 KPI Cards */}
      <div className="metric-cards-grid">
        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">Total Users</span>
            <span className="metric-card-value">{loading ? "—" : summary.totalUsers}</span>
            <span className="metric-card-sub">Registered accounts</span>
          </div>
          <div className="metric-card-icon primary">
            <Users size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">Total Applications</span>
            <span className="metric-card-value">{loading ? "—" : summary.totalApplications}</span>
            <span className="metric-card-sub">Platform submissions</span>
          </div>
          <div className="metric-card-icon info">
            <Briefcase size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">Pending / Applied</span>
            <span className="metric-card-value">{loading ? "—" : summary.applied}</span>
            <span className="metric-card-sub">In review</span>
          </div>
          <div className="metric-card-icon warning">
            <Clock size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">Interviews</span>
            <span className="metric-card-value">{loading ? "—" : summary.interviews}</span>
            <span className="metric-card-sub">Scheduled rounds</span>
          </div>
          <div className="metric-card-icon info">
            <Calendar size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">Offers</span>
            <span className="metric-card-value">{loading ? "—" : summary.offers}</span>
            <span className="metric-card-sub">Job offers</span>
          </div>
          <div className="metric-card-icon success">
            <CheckCircle2 size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-card-info">
            <span className="metric-card-label">Rejections</span>
            <span className="metric-card-value">{loading ? "—" : summary.rejected}</span>
            <span className="metric-card-sub">Archived</span>
          </div>
          <div className="metric-card-icon danger">
            <XCircle size={24} />
          </div>
        </div>
      </div>

      {/* Analytics Charts Grid */}
      <div className="dashboard-grid-2">
        {/* Status Distribution */}
        <div className="content-card">
          <div className="card-header">
            <h3>
              <TrendingUp size={20} color="var(--primary-600)" />
              Application Status Distribution
            </h3>
          </div>

          {statusChartData.length > 0 ? (
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
            <div className="empty-state-box">
              <p>No status telemetry available.</p>
            </div>
          )}
        </div>

        {/* Monthly Applications */}
        <div className="content-card">
          <div className="card-header">
            <h3>
              <Calendar size={20} color="var(--primary-600)" />
              Monthly Application Growth
            </h3>
          </div>

          {monthlyChartData.length > 0 ? (
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
                  <Bar dataKey="applications" fill="#4F46E5" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="empty-state-box">
              <p>No monthly activity recorded yet.</p>
            </div>
          )}
        </div>
      </div>

      {/* Second Analytics Row: Top Companies & Top Job Roles */}
      <div className="dashboard-grid-2">
        {/* Top Companies */}
        <div className="content-card">
          <div className="card-header">
            <h3>
              <Building size={20} color="var(--primary-600)" />
              Top Applied Companies
            </h3>
          </div>

          {companyChartData.length > 0 ? (
            <div style={{ height: "260px", width: "100%" }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={companyChartData} layout="vertical" margin={{ top: 10, right: 10, left: 20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" horizontal={false} />
                  <XAxis type="number" stroke="#94A3B8" fontSize={12} allowDecimals={false} />
                  <YAxis type="category" dataKey="company" stroke="#94A3B8" fontSize={12} width={100} tickLine={false} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#0B132B",
                      border: "none",
                      borderRadius: "8px",
                      color: "#FFFFFF",
                      fontSize: "13px",
                    }}
                  />
                  <Bar dataKey="totalApplications" fill="#10B981" radius={[0, 6, 6, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="empty-state-box">
              <p>No company application statistics yet.</p>
            </div>
          )}
        </div>

        {/* User Registration Trend */}
        <div className="content-card">
          <div className="card-header">
            <h3>
              <Users size={20} color="var(--primary-600)" />
              User Registration Trend
            </h3>
          </div>

          {monthlyUsersChartData.length > 0 ? (
            <div style={{ height: "260px", width: "100%" }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={monthlyUsersChartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
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
                  <Bar dataKey="users" fill="#8B5CF6" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="empty-state-box">
              <p>No user registration trend data available.</p>
            </div>
          )}
        </div>
      </div>

      {/* Top Job Roles Section */}
      {roleChartData.length > 0 && (
        <div className="content-card" style={{ marginTop: "24px" }}>
          <div className="card-header">
            <h3>
              <Briefcase size={20} color="var(--primary-600)" />
              Top In-Demand Job Titles
            </h3>
          </div>
          <div style={{ height: "260px", width: "100%" }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={roleChartData} layout="vertical" margin={{ top: 10, right: 10, left: 20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" horizontal={false} />
                <XAxis type="number" stroke="#94A3B8" fontSize={12} allowDecimals={false} />
                <YAxis type="category" dataKey="role" stroke="#94A3B8" fontSize={12} width={140} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "#0B132B",
                    border: "none",
                    borderRadius: "8px",
                    color: "#FFFFFF",
                    fontSize: "13px",
                  }}
                />
                <Bar dataKey="totalApplications" fill="#6366F1" radius={[0, 6, 6, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </div>
  );
}

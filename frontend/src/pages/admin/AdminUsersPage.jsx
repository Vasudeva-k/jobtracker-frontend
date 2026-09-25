import { useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import { api } from "../../services/api";
import {
  Users,
  Search,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Shield,
  UserCheck,
  UserX,
  Trash2,
  X,
} from "lucide-react";

export default function AdminUsersPage() {
  const { user: currentUser } = useAuth();
  const toast = useToast();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [actionLoading, setActionLoading] = useState(null);

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await api.getAdminUsers();
      setUsers(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Failed to load users:", err);
      setError(err instanceof Error ? err.message : "Failed to load user accounts");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleBlockUser = async (user) => {
    if (user.id === currentUser?.id || user.email === currentUser?.email) {
      toast.warning("Self-protection: You cannot block your own active administrative account.");
      return;
    }

    try {
      setActionLoading(user.id);
      setError("");
      setSuccess("");
      await api.blockAdminUser(user.id);
      const msg = `Account for ${user.firstName} ${user.lastName} blocked successfully.`;
      setSuccess(msg);
      toast.success(msg);
      await loadUsers();
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Failed to block user account";
      setError(msg);
      toast.error(msg);
    } finally {
      setActionLoading(null);
    }
  };

  const handleUnblockUser = async (user) => {
    try {
      setActionLoading(user.id);
      setError("");
      setSuccess("");
      await api.unblockAdminUser(user.id);
      const msg = `Account for ${user.firstName} ${user.lastName} unblocked successfully.`;
      setSuccess(msg);
      toast.success(msg);
      await loadUsers();
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Failed to unblock user account";
      setError(msg);
      toast.error(msg);
    } finally {
      setActionLoading(null);
    }
  };

  const handleDeactivateUser = async (user) => {
    if (user.id === currentUser?.id || user.email === currentUser?.email) {
      toast.warning("Self-protection: You cannot deactivate or delete your own active administrative account.");
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to deactivate account #${user.id} (${user.email})? The user will not be able to log in.`
    );
    if (!confirmed) return;

    try {
      setActionLoading(user.id);
      setError("");
      setSuccess("");
      await api.deleteAdminUser(user.id);
      const msg = `Account #${user.id} deactivated successfully.`;
      setSuccess(msg);
      toast.success(msg);
      await loadUsers();
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Failed to deactivate user account";
      setError(msg);
      toast.error(msg);
    } finally {
      setActionLoading(null);
    }
  };

  const filteredUsers = users.filter((u) => {
    const q = search.toLowerCase().trim();
    const fullName = `${u.firstName || ""} ${u.lastName || ""}`.toLowerCase().trim();
    const email = (u.email || "").toLowerCase();
    const phone = (u.phone || "").toLowerCase();

    const matchesSearch =
      q === "" ||
      fullName.includes(q) ||
      email.includes(q) ||
      phone.includes(q) ||
      String(u.id || "").includes(q);

    const matchesStatus =
      statusFilter === "ALL" ||
      (statusFilter === "ACTIVE" && u.active) ||
      (statusFilter === "BLOCKED" && !u.active);

    return matchesSearch && matchesStatus;
  });

  const formatDate = (dateStr) => {
    if (!dateStr) return "N/A";
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
          <h2>User Directory & Access Control</h2>
          <p>Manage registered candidates, administrative roles, and account security</p>
        </div>
        <div className="heading-actions">
          <button className="secondary-button" onClick={loadUsers} disabled={loading}>
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

      {/* Search & Filters */}
      <div className="content-card" style={{ padding: "16px 20px" }}>
        <div style={{ display: "flex", flexWrap: "wrap", gap: "16px", alignItems: "center", justifyContent: "space-between" }}>
          <div className="header-search" style={{ width: "320px", display: "flex" }}>
            <Search size={16} />
            <input
              type="text"
              placeholder="Search by name, email, or ID..."
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
              <option value="ALL">All Statuses</option>
              <option value="ACTIVE">Active Only</option>
              <option value="BLOCKED">Blocked Only</option>
            </select>
            <span style={{ fontSize: "13px", color: "var(--text-muted)", fontWeight: 600 }}>
              Showing {filteredUsers.length} user(s)
            </span>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="content-card">
        {loading ? (
          <div className="empty-state-box">
            <RefreshCw size={28} className="spinning" color="var(--primary-600)" />
            <h4>Loading user directory...</h4>
          </div>
        ) : filteredUsers.length > 0 ? (
          <div className="table-responsive">
            <table className="saas-table">
              <thead>
                <tr>
                  <th>User Details</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Created Date</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((u) => (
                  <tr key={u.id}>
                    <td>
                      <div className="company-cell">
                        <div
                          className="company-logo-placeholder"
                          style={{
                            background: u.role === "ADMIN" ? "#FEF2F2" : "var(--primary-50)",
                            color: u.role === "ADMIN" ? "var(--danger)" : "var(--primary-600)",
                          }}
                        >
                          {u.firstName?.charAt(0)?.toUpperCase() || "U"}
                        </div>
                        <div className="company-details">
                          <strong>{u.firstName} {u.lastName}</strong>
                          <span>{u.email}</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className={`user-role-badge ${u.role === "ADMIN" ? "admin" : ""}`}>
                        {u.role === "ADMIN" && <Shield size={12} />}
                        {u.role || "USER"}
                      </span>
                    </td>
                    <td>
                      <span className={`status-pill ${u.active ? "offered" : "rejected"}`}>
                        {u.active ? "Active" : "Blocked"}
                      </span>
                    </td>
                    <td style={{ color: "var(--text-muted)", fontSize: "13px" }}>
                      {formatDate(u.createdAt)}
                    </td>
                    <td style={{ textAlign: "right" }}>
                      <div style={{ display: "inline-flex", gap: "8px" }}>
                        {u.active ? (
                          <button
                            className="secondary-button"
                            style={{ padding: "5px 10px", fontSize: "12px", color: "var(--danger)" }}
                            onClick={() => handleBlockUser(u)}
                            disabled={actionLoading === u.id}
                            title="Block User"
                          >
                            <UserX size={14} />
                            <span>Block</span>
                          </button>
                        ) : (
                          <button
                            className="secondary-button"
                            style={{ padding: "5px 10px", fontSize: "12px", color: "var(--success)" }}
                            onClick={() => handleUnblockUser(u)}
                            disabled={actionLoading === u.id}
                            title="Unblock User"
                          >
                            <UserCheck size={14} />
                            <span>Unblock</span>
                          </button>
                        )}
                        <button
                          className="danger-button"
                          style={{ padding: "5px 10px", fontSize: "12px" }}
                          onClick={() => handleDeactivateUser(u)}
                          disabled={actionLoading === u.id}
                          title="Deactivate Account"
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
              <Users size={24} />
            </div>
            <h4>No user accounts found</h4>
            <p>Try resetting your search query or filter selection.</p>
          </div>
        )}
      </div>
    </div>
  );
}

import { useState, useRef, useEffect } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import {
  LayoutDashboard,
  Briefcase,
  FileText,
  Sparkles,
  User,
  LogOut,
  Menu,
  X,
  ShieldAlert,
  Search,
  Bell,
  CheckCircle2,
  Clock,
  Info,
} from "lucide-react";

export default function UserLayout() {
  const { user, logout, isAdmin } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const notifRef = useRef(null);

  const [notifications, setNotifications] = useState([
    {
      id: 1,
      title: "Gemini AI Connected",
      message: "gemini-3.5-flash model is active and ready for resume analysis.",
      time: "Just now",
      read: false,
      type: "success",
    },
    {
      id: 2,
      title: "Interview Reminder",
      message: "Check your upcoming interview schedule in the Dashboard.",
      time: "1 hour ago",
      read: false,
      type: "info",
    },
  ]);

  // Close notifications on outside click
  useEffect(() => {
    function handleClickOutside(event) {
      if (notifRef.current && !notifRef.current.contains(event.target)) {
        setNotificationsOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    toast.info("You have been signed out.");
    navigate("/login");
  };

  const handleMarkAllRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    toast.success("All notifications marked as read.");
  };

  const handleClearNotifications = () => {
    setNotifications([]);
    setNotificationsOpen(false);
    toast.info("Notifications cleared.");
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  const navItems = [
    { to: "/user/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { to: "/user/applications", label: "Applications", icon: Briefcase },
    { to: "/user/resume", label: "Resume Hub", icon: FileText },
    { to: "/user/ai", label: "AI Toolkit", icon: Sparkles },
    { to: "/user/profile", label: "Profile", icon: User },
  ];

  return (
    <div className="dashboard-shell">
      {/* Mobile Backdrop */}
      {mobileMenuOpen && (
        <div
          className="mobile-backdrop"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={`sidebar ${mobileMenuOpen ? "open" : ""}`}>
        <div className="sidebar-brand">
          <div className="sidebar-brand-mark">JT</div>
          <div className="sidebar-brand-text">
            <h2>JobTracker</h2>
            <span>Career Portal</span>
          </div>
          <button
            className="mobile-close-btn"
            onClick={() => setMobileMenuOpen(false)}
            aria-label="Close sidebar menu"
          >
            <X size={20} />
          </button>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-section-label">WORKSPACE</div>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `nav-item ${isActive ? "active" : ""}`
                }
                onClick={() => setMobileMenuOpen(false)}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}

          {isAdmin && (
            <>
              <div className="nav-section-label" style={{ marginTop: "16px" }}>
                ADMINISTRATION
              </div>
              <NavLink
                to="/admin/dashboard"
                className="nav-item admin-switch-nav"
                onClick={() => setMobileMenuOpen(false)}
              >
                <ShieldAlert size={18} />
                <span>Admin Console</span>
              </NavLink>
            </>
          )}
        </nav>

        <div className="sidebar-bottom">
          <div className="sidebar-user">
            <div className="sidebar-user-avatar">
              {user?.firstName?.charAt(0)?.toUpperCase() || "U"}
            </div>
            <div className="sidebar-user-info">
              <strong>
                {user?.firstName} {user?.lastName}
              </strong>
              <span>{user?.email}</span>
            </div>
          </div>

          <button className="logout-button" onClick={handleLogout}>
            <LogOut size={16} />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="main-area">
        <header className="dashboard-header">
          <div className="header-left">
            <button
              className="mobile-menu-toggle"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              aria-label="Open sidebar menu"
            >
              <Menu size={22} />
            </button>
            <div className="header-title-group">
              <h1>JobTracker Workspace</h1>
              <p>Welcome back, {user?.firstName || "Job Seeker"}</p>
            </div>
          </div>

          <div className="header-right">
            <div className="header-search">
              <Search size={16} />
              <input
                type="text"
                placeholder="Search jobs, skills, companies..."
                onKeyDown={(e) => {
                  if (e.key === "Enter" && e.target.value.trim()) {
                    navigate(
                      `/user/applications?search=${encodeURIComponent(
                        e.target.value.trim()
                      )}`
                    );
                  }
                }}
              />
            </div>

            {/* Notifications Dropdown Container */}
            <div className="notifications-wrapper" ref={notifRef}>
              <button
                className={`icon-btn notif-btn ${unreadCount > 0 ? "has-unread" : ""}`}
                title="Notifications"
                onClick={() => setNotificationsOpen((prev) => !prev)}
                aria-label="View notifications"
                aria-expanded={notificationsOpen}
              >
                <Bell size={18} />
                {unreadCount > 0 && (
                  <span className="notif-badge">{unreadCount}</span>
                )}
              </button>

              {notificationsOpen && (
                <div className="notifications-popover">
                  <div className="notif-popover-header">
                    <div className="notif-popover-title">
                      <strong>Notifications</strong>
                      {unreadCount > 0 && (
                        <span className="notif-count-pill">{unreadCount} new</span>
                      )}
                    </div>
                    {notifications.length > 0 && (
                      <button
                        className="notif-action-btn"
                        onClick={handleMarkAllRead}
                      >
                        Mark all read
                      </button>
                    )}
                  </div>

                  <div className="notif-popover-list">
                    {notifications.length === 0 ? (
                      <div className="notif-empty">
                        <CheckCircle2 size={24} className="notif-empty-icon" />
                        <p>You&apos;re all caught up!</p>
                        <span>No new alerts or updates</span>
                      </div>
                    ) : (
                      notifications.map((item) => (
                        <div
                          key={item.id}
                          className={`notif-item ${item.read ? "read" : "unread"}`}
                          onClick={() => {
                            setNotifications((prev) =>
                              prev.map((n) =>
                                n.id === item.id ? { ...n, read: true } : n
                              )
                            );
                          }}
                        >
                          <div className={`notif-item-icon notif-${item.type}`}>
                            {item.type === "success" ? (
                              <CheckCircle2 size={14} />
                            ) : (
                              <Info size={14} />
                            )}
                          </div>
                          <div className="notif-item-content">
                            <div className="notif-item-top">
                              <h4>{item.title}</h4>
                              <span className="notif-time">
                                <Clock size={11} />
                                {item.time}
                              </span>
                            </div>
                            <p>{item.message}</p>
                          </div>
                        </div>
                      ))
                    )}
                  </div>

                  {notifications.length > 0 && (
                    <div className="notif-popover-footer">
                      <button
                        className="notif-clear-btn"
                        onClick={handleClearNotifications}
                      >
                        Clear all notifications
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="user-role-badge">
              <span className="online-dot" />
              <span>{user?.role || "USER"}</span>
            </div>
          </div>
        </header>

        <main className="dashboard-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { ToastProvider } from "./context/ToastContext";
import ToastContainer from "./components/ToastContainer";
import "./App.css";

// Layouts & Guards
import ProtectedRoute from "./components/ProtectedRoute";
import AdminRoute from "./components/AdminRoute";
import AuthLayout from "./layouts/AuthLayout";
import UserLayout from "./layouts/UserLayout";
import AdminLayout from "./layouts/AdminLayout";

// Auth Pages
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";

// User Pages
import UserDashboardPage from "./pages/user/UserDashboardPage";
import JobTrackerPage from "./pages/user/JobTrackerPage";
import ResumeHubPage from "./pages/user/ResumeHubPage";
import AIToolkitPage from "./pages/user/AIToolkitPage";
import ProfilePage from "./pages/user/ProfilePage";

// Admin Pages
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";
import AdminUsersPage from "./pages/admin/AdminUsersPage";
import AdminApplicationsPage from "./pages/admin/AdminApplicationsPage";

/**
 * Intelligent Root Redirect based on authentication & role
 */
function RootRedirect() {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) {
    return (
      <div className="dashboard-loading" style={{ minHeight: "100vh" }}>
        <div className="spinner" />
        <p>Loading Job Tracker...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (isAdmin) {
    return <Navigate to="/admin/dashboard" replace />;
  }

  return <Navigate to="/user/dashboard" replace />;
}

export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <BrowserRouter>
          <ToastContainer />
          <Routes>
            {/* Public Auth Routes */}
            <Route element={<AuthLayout />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
            </Route>

            {/* Protected User Routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/user" element={<UserLayout />}>
                <Route index element={<Navigate to="/user/dashboard" replace />} />
                <Route path="dashboard" element={<UserDashboardPage />} />
                <Route path="applications" element={<JobTrackerPage />} />
                <Route path="resume" element={<ResumeHubPage />} />
                <Route path="resumes" element={<Navigate to="/user/resume" replace />} />
                <Route path="ai" element={<AIToolkitPage />} />
                <Route path="ai-tools" element={<Navigate to="/user/ai" replace />} />
                <Route path="profile" element={<ProfilePage />} />
              </Route>
            </Route>

            {/* Protected Admin Routes */}
            <Route element={<AdminRoute />}>
              <Route path="/admin" element={<AdminLayout />}>
                <Route index element={<Navigate to="/admin/dashboard" replace />} />
                <Route path="dashboard" element={<AdminDashboardPage />} />
                <Route path="users" element={<AdminUsersPage />} />
                <Route path="applications" element={<AdminApplicationsPage />} />
              </Route>
            </Route>

            {/* Root and Fallback Catch-All */}
            <Route path="/" element={<RootRedirect />} />
            <Route path="*" element={<RootRedirect />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ToastProvider>
  );
}
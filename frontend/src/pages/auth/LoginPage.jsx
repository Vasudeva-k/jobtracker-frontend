import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import {
  LogIn,
  AlertCircle,
  Eye,
  EyeOff,
  Sparkles,
  Briefcase,
  FileCheck,
  TrendingUp,
  Mail,
  Lock,
  RefreshCw,
} from "lucide-react";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email.trim() || !password.trim()) {
      const msg = "Please enter both your email and password.";
      setError(msg);
      toast.warning(msg);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const loggedUser = await login(email.trim(), password);
      toast.success(`Welcome back, ${loggedUser.firstName || "User"}!`);

      if (loggedUser.role === "ADMIN") {
        navigate("/admin/dashboard");
      } else {
        navigate("/user/dashboard");
      }
    } catch (err) {
      const msg = err.message || "Failed to sign in. Please check your credentials.";
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {/* Left SaaS Hero Presentation */}
      <div className="auth-hero-panel">
        <div className="auth-hero-brand">
          <div className="auth-brand-logo">JT</div>
          <span className="auth-brand-title">JobTracker</span>
        </div>

        <div className="auth-hero-content">
          <div className="auth-hero-badge">
            <Sparkles size={14} />
            <span>AI-Powered Career & Application Platform</span>
          </div>

          <h1 className="auth-hero-heading">
            Welcome back to your career command center.
          </h1>

          <p className="auth-hero-subheading">
            Review your application pipeline, check upcoming interview schedules, and leverage AI to prepare for your next opportunity.
          </p>

          <div className="auth-benefits-list">
            <div className="auth-benefit-item">
              <div className="auth-benefit-icon">
                <Briefcase size={16} />
              </div>
              <div className="auth-benefit-text">
                <h4>Pipeline Overview</h4>
                <p>Stay on top of active applications, recruiter follow-ups, and offer deadlines.</p>
              </div>
            </div>

            <div className="auth-benefit-item">
              <div className="auth-benefit-icon">
                <FileCheck size={16} />
              </div>
              <div className="auth-benefit-text">
                <h4>Resume Hub & Insights</h4>
                <p>Track ATS match percentages and keyword optimizations for targeted roles.</p>
              </div>
            </div>

            <div className="auth-benefit-item">
              <div className="auth-benefit-icon">
                <TrendingUp size={16} />
              </div>
              <div className="auth-benefit-text">
                <h4>AI Prep & Analytics</h4>
                <p>Generate company-specific interview questions and salary projections.</p>
              </div>
            </div>
          </div>
        </div>

        <div className="auth-hero-footer">
          <p>© {new Date().getFullYear()} JobTracker Inc. All rights reserved.</p>
        </div>
      </div>

      {/* Right Sign-in Form */}
      <div className="auth-form-panel">
        <div className="auth-card-container">
          <div className="auth-header">
            <h2>Welcome back</h2>
            <p>Enter your credentials to access your JobTracker account.</p>
          </div>

          {error && (
            <div className="app-alert error" style={{ marginBottom: "16px" }}>
              <AlertCircle size={18} style={{ flexShrink: 0, marginTop: "2px" }} />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <div className="input-wrapper">
                <Mail size={16} className="input-icon" />
                <input
                  id="email"
                  type="email"
                  className="auth-input has-left-icon"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={loading}
                  required
                  autoFocus
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="password">Password</label>
              <div className="input-wrapper">
                <Lock size={16} className="input-icon" />
                <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  className="auth-input has-left-icon has-right-icon"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={loading}
                  required
                />
                <button
                  type="button"
                  className="input-icon-right"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                  disabled={loading}
                  tabIndex={0}
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="auth-submit-btn"
              disabled={loading}
              aria-busy={loading}
            >
              {loading ? (
                <>
                  <RefreshCw size={16} className="spinning" />
                  <span>Signing In...</span>
                </>
              ) : (
                <>
                  <LogIn size={18} />
                  <span>Sign In</span>
                </>
              )}
            </button>
          </form>

          <div className="auth-footer">
            <span>Don&apos;t have an account yet?</span>
            <Link to="/register">Create an Account</Link>
          </div>
        </div>
      </div>
    </>
  );
}

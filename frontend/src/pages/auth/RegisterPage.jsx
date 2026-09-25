import { useState, useMemo } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import {
  UserPlus,
  AlertCircle,
  CheckCircle,
  Eye,
  EyeOff,
  Briefcase,
  Sparkles,
  FileCheck,
  TrendingUp,
  User,
  Mail,
  Phone,
  Lock,
  RefreshCw,
} from "lucide-react";

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const { register } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  // Password strength calculator
  const passwordStrength = useMemo(() => {
    const pwd = formData.password;
    if (!pwd) return { score: 0, label: "", class: "" };
    let score = 0;
    if (pwd.length >= 6) score += 1;
    if (pwd.length >= 10) score += 1;
    if (/[A-Z]/.test(pwd) && /[0-9]/.test(pwd)) score += 1;
    if (/[^A-Za-z0-9]/.test(pwd)) score += 1;

    switch (score) {
      case 1:
        return { score: 1, label: "Weak password", class: "weak" };
      case 2:
        return { score: 2, label: "Fair password", class: "fair" };
      case 3:
        return { score: 3, label: "Good password", class: "good" };
      case 4:
        return { score: 4, label: "Strong password", class: "strong" };
      default:
        return { score: 1, label: "Weak password", class: "weak" };
    }
  }, [formData.password]);

  const handleChange = (e) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (
      !formData.firstName.trim() ||
      !formData.lastName.trim() ||
      !formData.email.trim() ||
      !formData.phone.trim() ||
      !formData.password.trim()
    ) {
      const msg = "Please fill in all required fields.";
      setError(msg);
      toast.warning(msg);
      return;
    }

    if (formData.password.length < 6) {
      const msg = "Password must be at least 6 characters long.";
      setError(msg);
      toast.warning(msg);
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      const msg = "Passwords do not match. Please verify your password confirmation.";
      setError(msg);
      toast.error(msg);
      return;
    }

    try {
      setLoading(true);
      await register({
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim(),
        phone: formData.phone.trim(),
        password: formData.password,
      });

      const successMsg = "Account registered successfully! Redirecting to login...";
      setSuccess(successMsg);
      toast.success(successMsg);
      setTimeout(() => {
        navigate("/login");
      }, 1200);
    } catch (err) {
      const msg = err.message || "Registration failed. Please try again.";
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
            Accelerate your career search with intelligent tracking.
          </h1>

          <p className="auth-hero-subheading">
            Join thousands of ambitious professionals organizing applications, optimizing resumes with AI, and landing their dream job offers.
          </p>

          <div className="auth-benefits-list">
            <div className="auth-benefit-item">
              <div className="auth-benefit-icon">
                <Briefcase size={16} />
              </div>
              <div className="auth-benefit-text">
                <h4>Application Tracking & Kanban</h4>
                <p>Manage applications across interview pipelines with smart reminders.</p>
              </div>
            </div>

            <div className="auth-benefit-item">
              <div className="auth-benefit-icon">
                <FileCheck size={16} />
              </div>
              <div className="auth-benefit-text">
                <h4>AI ATS Resume Scoring</h4>
                <p>Instant keyword coverage and formatting recommendations.</p>
              </div>
            </div>

            <div className="auth-benefit-item">
              <div className="auth-benefit-icon">
                <TrendingUp size={16} />
              </div>
              <div className="auth-benefit-text">
                <h4>Career Roadmaps & Mock Interviews</h4>
                <p>Step-by-step skill growth timelines and tailored interview prep.</p>
              </div>
            </div>
          </div>
        </div>

        <div className="auth-hero-footer">
          <p>© {new Date().getFullYear()} JobTracker Inc. All rights reserved.</p>
        </div>
      </div>

      {/* Right Registration Form */}
      <div className="auth-form-panel">
        <div className="auth-card-container">
          <div className="auth-header">
            <h2>Create your account</h2>
            <p>Start tracking your applications and boosting your interview rate.</p>
          </div>

          {error && (
            <div className="app-alert error" style={{ marginBottom: "16px" }}>
              <AlertCircle size={18} style={{ flexShrink: 0, marginTop: "2px" }} />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="app-alert success" style={{ marginBottom: "16px" }}>
              <CheckCircle size={18} style={{ flexShrink: 0, marginTop: "2px" }} />
              <span>{success}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="firstName">First Name</label>
                <div className="input-wrapper">
                  <User size={16} className="input-icon" />
                  <input
                    id="firstName"
                    name="firstName"
                    type="text"
                    className="auth-input has-left-icon"
                    placeholder="Jane"
                    value={formData.firstName}
                    onChange={handleChange}
                    disabled={loading}
                    required
                    autoFocus
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="lastName">Last Name</label>
                <div className="input-wrapper">
                  <User size={16} className="input-icon" />
                  <input
                    id="lastName"
                    name="lastName"
                    type="text"
                    className="auth-input has-left-icon"
                    placeholder="Doe"
                    value={formData.lastName}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <div className="input-wrapper">
                <Mail size={16} className="input-icon" />
                <input
                  id="email"
                  name="email"
                  type="email"
                  className="auth-input has-left-icon"
                  placeholder="jane.doe@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  disabled={loading}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="phone">Phone Number</label>
              <div className="input-wrapper">
                <Phone size={16} className="input-icon" />
                <input
                  id="phone"
                  name="phone"
                  type="tel"
                  className="auth-input has-left-icon"
                  placeholder="+1 (555) 019-2834"
                  value={formData.phone}
                  onChange={handleChange}
                  disabled={loading}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="password">Password</label>
                <div className="input-wrapper">
                  <Lock size={16} className="input-icon" />
                  <input
                    id="password"
                    name="password"
                    type={showPassword ? "text" : "password"}
                    className="auth-input has-left-icon has-right-icon"
                    placeholder="••••••••"
                    value={formData.password}
                    onChange={handleChange}
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

              <div className="form-group">
                <label htmlFor="confirmPassword">Confirm Password</label>
                <div className="input-wrapper">
                  <Lock size={16} className="input-icon" />
                  <input
                    id="confirmPassword"
                    name="confirmPassword"
                    type={showConfirmPassword ? "text" : "password"}
                    className="auth-input has-left-icon has-right-icon"
                    placeholder="••••••••"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                  <button
                    type="button"
                    className="input-icon-right"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    aria-label={showConfirmPassword ? "Hide password" : "Show password"}
                    disabled={loading}
                    tabIndex={0}
                  >
                    {showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>
            </div>

            {formData.password && (
              <div className="password-strength-container">
                <div className="password-strength-bar">
                  <div className={`password-strength-fill ${passwordStrength.class}`} />
                </div>
                <div className="password-strength-label">
                  <span>Password strength: </span>
                  <span className={passwordStrength.class}>{passwordStrength.label}</span>
                </div>
              </div>
            )}

            <button
              type="submit"
              className="auth-submit-btn"
              disabled={loading}
              aria-busy={loading}
            >
              {loading ? (
                <>
                  <RefreshCw size={16} className="spinning" />
                  <span>Creating Account...</span>
                </>
              ) : (
                <>
                  <UserPlus size={18} />
                  <span>Create Account</span>
                </>
              )}
            </button>
          </form>

          <div className="auth-footer">
            <span>Already have an account?</span>
            <Link to="/login">Sign In</Link>
          </div>
        </div>
      </div>
    </>
  );
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

/**
 * Helper to get the JWT authorization headers
 */
const getAuthHeaders = (isMultipart = false) => {
  const token = localStorage.getItem("token");
  const headers = {};

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  if (!isMultipart) {
    headers["Content-Type"] = "application/json";
  }

  return headers;
};

/**
 * Universal fetch handler with clean error throwing
 */
async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const isMultipart = options.body instanceof FormData;
  const headers = {
    ...getAuthHeaders(isMultipart),
    ...(options.headers || {}),
  };

  const config = {
    ...options,
    headers,
  };

  const response = await fetch(url, config);

  if (response.status === 401) {
    // If unauthorized, clean invalid token
    if (!endpoint.includes("/api/auth/login")) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
  }

  const contentType = response.headers.get("content-type");
  let data;

  if (contentType && contentType.includes("application/pdf")) {
    if (!response.ok) {
      throw new Error("Failed to download PDF");
    }
    return await response.blob();
  }

  if (contentType && contentType.includes("application/json")) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    const errorMsg =
      (typeof data === "object" && data !== null && (data.message || data.error)) ||
      (typeof data === "string" && data) ||
      `Request failed with status ${response.status}`;
    throw new Error(errorMsg);
  }

  return data;
}

export const api = {
  // ==========================================
  // AUTH
  // ==========================================
  login: (email, password) =>
    request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),

  register: (userData) =>
    request("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(userData),
    }),

  // ==========================================
  // USER DASHBOARD & ANALYTICS
  // ==========================================
  getDashboardStats: () =>
    request("/api/dashboard", { method: "GET" }),

  getDashboardAnalytics: () =>
    request("/api/dashboard/analytics", { method: "GET" }),

  // ==========================================
  // JOBS / APPLICATIONS (USER)
  // ==========================================
  getJobs: () =>
    request("/api/jobs", { method: "GET" }),

  getJobById: (id) =>
    request(`/api/jobs/${id}`, { method: "GET" }),

  addJob: (jobData) =>
    request("/api/jobs", {
      method: "POST",
      body: JSON.stringify(jobData),
    }),

  updateJob: (id, jobData) =>
    request(`/api/jobs/${id}`, {
      method: "PUT",
      body: JSON.stringify(jobData),
    }),

  deleteJob: (id) =>
    request(`/api/jobs/${id}`, { method: "DELETE" }),

  getTodayInterviews: () =>
    request("/api/jobs/interviews/today", { method: "GET" }),

  getInterviewsByDate: (dateStr) =>
    request(`/api/jobs/interviews/date/${dateStr}`, { method: "GET" }),

  // ==========================================
  // RESUME
  // ==========================================
  uploadResume: (file) => {
    const formData = new FormData();
    formData.append("file", file);
    return request("/api/resume/analyze", {
      method: "POST",
      body: formData,
    });
  },

  getMyResume: () =>
    request("/api/resume", { method: "GET" }),

  downloadMyResume: () =>
    request("/api/resume/download", { method: "GET" }),

  generateResumePdf: (resumeData) =>
    request("/api/resume/pdf", {
      method: "POST",
      body: JSON.stringify(resumeData),
    }),

  // ==========================================
  // USER PROFILE
  // ==========================================
  getProfile: () =>
    request("/api/profile", { method: "GET" }),

  saveProfile: (profileData) =>
    request("/api/profile", {
      method: "POST",
      body: JSON.stringify(profileData),
    }),

  // ==========================================
  // AI TOOLKIT
  // ==========================================
  generateCoverLetter: (data) =>
    request("/api/ai/cover-letter", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  generateInterviewQuestions: (data) =>
    request("/api/ai/interview-questions", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  analyzeResumeText: (resumeText) =>
    request("/api/ai/resume-analyzer", {
      method: "POST",
      body: JSON.stringify({ resumeText }),
    }),

  predictATSScore: (data) =>
    request("/api/ai/ats-score", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  matchJob: (data) =>
    request("/api/ai/job-match", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  generateCareerRoadmap: (data) =>
    request("/api/ai/career-roadmap", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  predictSalary: (data) =>
    request("/api/ai/salary-predictor", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  evaluateInterview: (data) =>
    request("/api/ai/mock-interview", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  analyzeJobMarket: (data) =>
    request("/api/ai/job-market-trends", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  optimizeResumeKeywords: (data) =>
    request("/api/ai/resume-keyword-optimizer", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  generateCompanyInterview: (data) =>
    request("/api/ai/company-interview", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  analyzeSkillGap: (data) =>
    request("/api/ai/skill-gap", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  predictJobSuccess: (data) =>
    request("/api/ai/job-success-predictor", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  predictJobSuccessFromProfile: () =>
    request("/api/ai/job-success-predictor/profile", {
      method: "POST",
    }),

  chatAI: (prompt) =>
    request("/api/ai/chat", {
      method: "POST",
      body: prompt,
      headers: { "Content-Type": "text/plain" },
    }),

  // ==========================================
  // ADMIN ANALYTICS & STATS
  // ==========================================
  getAdminDashboard: () =>
    request("/api/admin/dashboard", { method: "GET" }),

  getAdminSummary: () =>
    request("/api/admin/analytics/summary", { method: "GET" }),

  getAdminAnalyticsSummary: () =>
    request("/api/admin/analytics/summary", { method: "GET" }),

  getAdminMonthlyJobs: () =>
    request("/api/admin/analytics/monthly-jobs", { method: "GET" }),

  getAdminJobStatus: () =>
    request("/api/admin/analytics/job-status", { method: "GET" }),

  getAdminTopCompanies: () =>
    request("/api/admin/analytics/top-companies", { method: "GET" }),

  getAdminMonthlyUsers: () =>
    request("/api/admin/analytics/monthly-users", { method: "GET" }),

  getAdminTopJobRoles: () =>
    request("/api/admin/analytics/top-job-roles", { method: "GET" }),

  // ==========================================
  // ADMIN USERS
  // ==========================================
  getAdminUsers: () =>
    request("/api/admin/users", { method: "GET" }),

  searchAdminUsers: (keyword) =>
    request(`/api/admin/users/search?keyword=${encodeURIComponent(keyword)}`, {
      method: "GET",
    }),

  blockAdminUser: (id) =>
    request(`/api/admin/users/${id}/block`, { method: "PUT" }),

  blockUser: (id) =>
    request(`/api/admin/users/${id}/block`, { method: "PUT" }),

  unblockAdminUser: (id) =>
    request(`/api/admin/users/${id}/unblock`, { method: "PUT" }),

  unblockUser: (id) =>
    request(`/api/admin/users/${id}/unblock`, { method: "PUT" }),

  deleteAdminUser: (id) =>
    request(`/api/admin/users/${id}`, { method: "DELETE" }),

  deactivateUser: (id) =>
    request(`/api/admin/users/${id}`, { method: "DELETE" }),

  // ==========================================
  // ADMIN APPLICATIONS
  // ==========================================
  getAdminApplications: (status) =>
    request(
      status && status !== "ALL"
        ? `/api/admin/applications?status=${encodeURIComponent(status)}`
        : "/api/admin/applications",
      { method: "GET" }
    ),

  searchAdminApplications: (keyword) =>
    request(`/api/admin/applications/search?keyword=${encodeURIComponent(keyword)}`, {
      method: "GET",
    }),

  getAdminApplicationById: (id) =>
    request(`/api/admin/applications/${id}`, { method: "GET" }),

  deleteAdminApplication: (id) =>
    request(`/api/admin/applications/${id}`, { method: "DELETE" }),
};


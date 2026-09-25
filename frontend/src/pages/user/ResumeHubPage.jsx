import { useState, useEffect, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { api } from "../../services/api";
import { useToast } from "../../context/ToastContext";
import {
  CheckCircle,
  AlertCircle,
} from "lucide-react";

import ResumeTabs from "../../components/resume/ResumeTabs";
import ResumeOverview from "../../components/resume/ResumeOverview";
import ResumeManager from "../../components/resume/ResumeManager";
import ResumeEditor from "../../components/resume/ResumeEditor";
import ResumePreview from "../../components/resume/ResumePreview";
import ResumePreviewModal from "../../components/resume/ResumePreviewModal";
import ResumeDeleteModal from "../../components/resume/ResumeDeleteModal";
import ResumeUploadModal from "../../components/resume/ResumeUploadModal";

import "./ResumeHub.css";

const DRAFT_STORAGE_KEY = "jobtracker_resume_builder_draft_v1";

const SAMPLE_BUILDER_DATA = {
  fullName: "Jane Doe",
  title: "Full Stack Software Engineer",
  email: "jane.doe@example.com",
  phone: "+1 (555) 345-6789",
  location: "San Francisco, CA",
  linkedin: "linkedin.com/in/janedoe-dev",
  github: "github.com/janedoe",
  summary:
    "Dedicated Full Stack Engineer with 3+ years of experience developing high-concurrency cloud architectures in Java Spring Boot and responsive frontends in React. Adept at database tuning, microservices, and AI-driven ATS workflows.",
  skillsList: [
    "Java",
    "Spring Boot",
    "React",
    "TypeScript",
    "PostgreSQL",
    "Docker",
    "Kubernetes",
    "AWS",
    "RESTful APIs",
    "Git",
  ],
  experienceList: [
    {
      company: "TechNova Solutions",
      role: "Software Engineer",
      location: "San Francisco, CA",
      startDate: "Jan 2022 – Present",
      description:
        "• Designed and deployed microservices supporting 100k+ daily queries with 99.98% uptime.\n• Optimized PostgreSQL indexes, reducing query execution time by 42% across core endpoints.",
    },
    {
      company: "Apex Innovations",
      role: "Junior Developer Intern",
      location: "San Jose, CA",
      startDate: "Jun 2021 – Dec 2021",
      description:
        "• Developed reusable UI component libraries in React with modern accessibility standards.\n• Integrated automated CI/CD unit testing pipelines with GitHub Actions.",
    },
  ],
  educationList: [
    {
      institution: "University of California, Berkeley",
      degree: "B.S. in Computer Science",
      fieldOfStudy: "Computer Science",
      startDate: "2017 – 2021",
      grade: "CGPA: 3.85 / 4.0",
    },
  ],
  projectList: [
    {
      name: "JobTracker SaaS Platform",
      techStack: "React, Java Spring Boot, MySQL, Gemini AI",
      link: "github.com/janedoe/jobtracker",
      description:
        "• Architected an intelligent career workflow portal with automated ATS scoring and analytics.\n• Implemented secure JWT authorization and real-time dashboard visualizations.",
    },
  ],
  certifications:
    "AWS Certified Solutions Architect – Associate (2023)\nOracle Certified Professional: Java SE 17 Developer",
};

export default function ResumeHubPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentTab = searchParams.get("tab") || "overview";

  const [activeTab, setActiveTab] = useState(
    ["overview", "manager", "builder"].includes(currentTab) ? currentTab : "overview"
  );

  const [resumeInfo, setResumeInfo] = useState(null);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [generatingPdf, setGeneratingPdf] = useState(false);
  const [loadingProfile, setLoadingProfile] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Modals state
  const [previewModalOpen, setPreviewModalOpen] = useState(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [selectedResumeForAction, setSelectedResumeForAction] = useState(null);

  // Resume Builder state
  const [selectedTemplate, setSelectedTemplate] = useState("modern");
  const [builderData, setBuilderData] = useState(() => {
    try {
      const saved = localStorage.getItem(DRAFT_STORAGE_KEY);
      if (saved) {
        return JSON.parse(saved);
      }
    } catch {
      // Fallback
    }
    return SAMPLE_BUILDER_DATA;
  });

  const toast = useToast();

  // Keep activeTab in sync with query parameter
  useEffect(() => {
    const tabParam = searchParams.get("tab");
    if (tabParam && ["overview", "manager", "builder"].includes(tabParam) && tabParam !== activeTab) {
      setActiveTab(tabParam);
    }
  }, [searchParams, activeTab]);

  const handleTabChange = (tabId) => {
    setActiveTab(tabId);
    setSearchParams({ tab: tabId });
    setError("");
  };

  // Fetch active resume data
  const loadResumeDetails = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const data = await api.getMyResume();
      setResumeInfo(data);
    } catch {
      // If 404/400 (no resume uploaded yet), clean state
      setResumeInfo(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadResumeDetails();
  }, [loadResumeDetails]);

  // Handle file upload & analysis
  const handleFileUpload = async (file) => {
    if (!file) return false;

    if (!file.name.toLowerCase().endsWith(".pdf")) {
      const msg = "Please upload a valid PDF format document (.pdf).";
      setError(msg);
      toast.warning(msg);
      return false;
    }

    if (file.size > 5 * 1024 * 1024) {
      const msg = "File size exceeds the 5 MB limit.";
      setError(msg);
      toast.warning(msg);
      return false;
    }

    try {
      setUploading(true);
      setError("");
      setSuccess("");

      const result = await api.uploadResume(file);
      setAnalysisResult(result);
      const okMsg = "Resume uploaded and analyzed successfully!";
      setSuccess(okMsg);
      toast.success(okMsg);
      await loadResumeDetails();
      return true;
    } catch (err) {
      console.error("Resume upload error:", err);
      const msg = err instanceof Error ? err.message : "Failed to upload and analyze resume";
      setError(msg);
      toast.error(msg);
      return false;
    } finally {
      setUploading(false);
    }
  };

  // Handle download of user's active resume
  const handleDownload = async () => {
    try {
      setDownloading(true);
      const blob = await api.downloadMyResume();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = resumeInfo?.fileName || "Resume.pdf";
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast.success("Resume downloaded successfully.");
    } catch (err) {
      console.error("Download error:", err);
      toast.error(err instanceof Error ? err.message : "Failed to download resume");
    } finally {
      setDownloading(false);
    }
  };

  // Pre-fill builder from candidate profile
  const handleLoadFromProfile = async () => {
    try {
      setLoadingProfile(true);
      const profile = await api.getProfile();
      if (!profile) {
        toast.info("No saved profile data found. You can set it in the Profile page.");
        return;
      }

      setBuilderData((prev) => {
        const nextSkills = Array.isArray(profile.skills) && profile.skills.length > 0
          ? profile.skills
          : prev.skillsList;

        return {
          ...prev,
          fullName: profile.userName || prev.fullName,
          email: profile.userEmail || prev.email,
          skillsList: nextSkills,
        };
      });

      toast.success("Auto-filled candidate profile details!");
    } catch (err) {
      console.error("Profile load error:", err);
      toast.warning("Could not auto-fill profile details.");
    } finally {
      setLoadingProfile(false);
    }
  };

  // Save builder draft to localStorage
  const handleSaveDraft = () => {
    try {
      localStorage.setItem(DRAFT_STORAGE_KEY, JSON.stringify(builderData));
      toast.success("Resume draft saved to your browser storage.");
    } catch (err) {
      console.error("Draft save error:", err);
      toast.error("Failed to save draft.");
    }
  };

  // Reset builder to sample template
  const handleResetSample = () => {
    setBuilderData(SAMPLE_BUILDER_DATA);
    toast.info("Reset resume builder to sample data.");
  };

  // Generate & Download PDF using backend endpoint
  const handleGeneratePdf = async () => {
    if (!builderData.fullName.trim() || !builderData.email.trim()) {
      toast.warning("Full Name and Email Address are required to generate your PDF resume.");
      return;
    }

    try {
      setGeneratingPdf(true);

      // Format education multi-line text for backend service
      const educationText = builderData.educationList
        .map((edu) => {
          let str = `${edu.degree || ""} – ${edu.institution || ""}`;
          if (edu.startDate) str += ` (${edu.startDate})`;
          if (edu.grade) str += `\n${edu.grade}`;
          return str.trim();
        })
        .filter(Boolean)
        .join("\n\n");

      // Format experience multi-line text for backend service
      const experienceText = builderData.experienceList
        .map((exp) => {
          let header = `${exp.role || ""} at ${exp.company || ""}`;
          if (exp.startDate) header += ` (${exp.startDate})`;
          if (exp.location) header += ` – ${exp.location}`;
          return `${header}\n${exp.description || ""}`.trim();
        })
        .filter(Boolean)
        .join("\n\n");

      // Format projects multi-line text for backend service
      const projectsText = builderData.projectList
        .map((proj) => {
          let header = proj.name || "";
          if (proj.techStack) header += ` [${proj.techStack}]`;
          if (proj.link) header += ` (${proj.link})`;
          return `${header}\n${proj.description || ""}`.trim();
        })
        .filter(Boolean)
        .join("\n\n");

      const payload = {
        fullName: builderData.fullName,
        email: builderData.email,
        phone: builderData.phone || "",
        summary: builderData.summary || "",
        skills: builderData.skillsList,
        education: educationText,
        experience: experienceText,
        projects: projectsText,
      };

      const blob = await api.generateResumePdf(payload);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${builderData.fullName.replace(/\s+/g, "_")}_Resume.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast.success("ATS PDF resume generated and downloaded!");
    } catch (err) {
      console.error("PDF generation error:", err);
      toast.error(err instanceof Error ? err.message : "Failed to generate PDF resume");
    } finally {
      setGeneratingPdf(false);
    }
  };

  // Preview Modal trigger
  const handleOpenPreview = (resume) => {
    setSelectedResumeForAction(resume || resumeInfo);
    setPreviewModalOpen(true);
  };

  // Delete / Replace confirmation
  const handleOpenDelete = (resume) => {
    setSelectedResumeForAction(resume || resumeInfo);
    setDeleteModalOpen(true);
  };

  const handleConfirmDelete = async () => {
    try {
      setDeleting(true);
      // Backend replaces resume on upload. Setting local state to null acknowledges removal
      setResumeInfo(null);
      setAnalysisResult(null);
      setDeleteModalOpen(false);
      toast.info("Active resume reference cleared. Upload a new resume at any time.");
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="page-container">
      {/* Top Header Strip with Title & Navigation Tabs */}
      <div className="resume-nav-header">
        <div>
          <h2 style={{ fontSize: "22px", fontWeight: 800, color: "var(--text-heading)", margin: "0 0 4px 0" }}>
            Resume Hub
          </h2>
          <p style={{ fontSize: "13.5px", color: "var(--text-muted)", margin: 0 }}>
            Manage, analyze, build, and optimize your ATS-ready professional resumes.
          </p>
        </div>

        <div className="resume-nav-actions">
          <ResumeTabs activeTab={activeTab} onTabChange={handleTabChange} />
        </div>
      </div>

      {/* Global Alerts */}
      {error && (
        <div className="dashboard-alert error" style={{ margin: "16px 0 0 0" }}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="dashboard-alert success" style={{ margin: "16px 0 0 0" }}>
          <CheckCircle size={18} />
          <span>{success}</span>
        </div>
      )}

      {/* Dynamic Content Views based on activeTab */}
      <div style={{ marginTop: "20px" }}>
        {activeTab === "overview" && (
          <ResumeOverview
            resumeInfo={resumeInfo}
            analysisResult={analysisResult}
            loading={loading}
            uploading={uploading}
            downloading={downloading}
            successMessage={success}
            onUpload={handleFileUpload}
            onDownload={handleDownload}
            onPreview={handleOpenPreview}
            onDelete={handleOpenDelete}
            onNavigateTab={handleTabChange}
          />
        )}

        {activeTab === "manager" && (
          <ResumeManager
            resumeInfo={resumeInfo}
            loading={loading}
            downloading={downloading}
            analysisResult={analysisResult}
            onDownload={handleDownload}
            onPreview={handleOpenPreview}
            onOpenUploadModal={() => setUploadModalOpen(true)}
            onDelete={handleOpenDelete}
            onNavigateTab={handleTabChange}
          />
        )}

        {activeTab === "builder" && (
          <div className="resume-builder-workspace">
            {/* Left: Accordion Form Editor */}
            <ResumeEditor
              formData={builderData}
              setFormData={setBuilderData}
              selectedTemplate={selectedTemplate}
              setSelectedTemplate={setSelectedTemplate}
              onGeneratePdf={handleGeneratePdf}
              onSaveDraft={handleSaveDraft}
              onResetSample={handleResetSample}
              onLoadProfile={handleLoadFromProfile}
              generatingPdf={generatingPdf}
              loadingProfile={loadingProfile}
            />

            {/* Right: Real-time Live A4 Document Preview */}
            <ResumePreview
              formData={builderData}
              selectedTemplate={selectedTemplate}
            />
          </div>
        )}
      </div>

      {/* Modals */}
      <ResumePreviewModal
        isOpen={previewModalOpen}
        onClose={() => setPreviewModalOpen(false)}
        resume={selectedResumeForAction || resumeInfo}
        onDownload={handleDownload}
      />

      <ResumeDeleteModal
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        onConfirm={handleConfirmDelete}
        resume={selectedResumeForAction || resumeInfo}
        deleting={deleting}
      />

      <ResumeUploadModal
        isOpen={uploadModalOpen}
        onClose={() => setUploadModalOpen(false)}
        onUpload={handleFileUpload}
        uploading={uploading}
        successMessage={success}
      />
    </div>
  );
}

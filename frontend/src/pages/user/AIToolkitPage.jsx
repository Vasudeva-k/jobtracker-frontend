import { useState, useEffect, useRef } from "react";
import { useSearchParams } from "react-router-dom";
import { api } from "../../services/api";
import { useToast } from "../../context/ToastContext";
import AIResultCard from "../../components/ai/AIResultCard";
import AIFormattedText from "../../components/ai/AIFormattedText";
import {
  Sparkles,
  FileText,
  HelpCircle,
  Map,
  Target,
  DollarSign,
  Send,
  RefreshCw,
  Cpu,
  Award,
  TrendingUp,
  Key,
  BarChart2,
  Mic,
  RotateCcw,
  CheckCircle2,
  AlertTriangle,
} from "lucide-react";

export default function AIToolkitPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [activeTab, setActiveTab] = useState(
    searchParams.get("tab") || "cover-letter"
  );
  const [activeCategory, setActiveCategory] = useState("all");
  const chatBottomRef = useRef(null);
  const toast = useToast();

  useEffect(() => {
    const tab = searchParams.get("tab");
    if (tab) {
      setActiveTab(tab);
    }
  }, [searchParams]);

  const switchTab = (tab) => {
    setActiveTab(tab);
    setSearchParams({ tab });
  };

  // ========================================================
  // 1. Cover Letter State
  // ========================================================
  const [coverData, setCoverData] = useState({
    companyName: "",
    jobRole: "",
  });
  const [coverResult, setCoverResult] = useState(null);
  const [coverLoading, setCoverLoading] = useState(false);

  const handleCoverLetter = async (e) => {
    e.preventDefault();
    if (!coverData.companyName.trim() || !coverData.jobRole.trim()) {
      toast.warning("Please specify both company name and target role.");
      return;
    }

    try {
      setCoverLoading(true);
      const res = await api.generateCoverLetter(coverData);
      setCoverResult(
        typeof res === "object" ? res : { coverLetter: res, source: "FALLBACK" }
      );
      toast.success("Cover letter generated!");
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : "Failed to generate cover letter"
      );
    } finally {
      setCoverLoading(false);
    }
  };

  // ========================================================
  // 2. Interview Prep State
  // ========================================================
  const [interviewData, setInterviewData] = useState({
    jobRole: "",
    skills: "",
    company: "",
  });
  const [interviewQuestions, setInterviewQuestions] = useState([]);
  const [questionsResult, setQuestionsResult] = useState(null);
  const [companyInterview, setCompanyInterview] = useState(null);
  const [interviewLoading, setInterviewLoading] = useState(false);

  const handleInterviewQuestions = async (e) => {
    e.preventDefault();
    if (!interviewData.jobRole.trim()) {
      toast.warning("Job role is required.");
      return;
    }

    try {
      setInterviewLoading(true);
      const res = await api.generateInterviewQuestions({
        jobRole: interviewData.jobRole,
        skills: interviewData.skills,
      });

      setQuestionsResult(res);
      setInterviewQuestions(res.questions || []);

      if (interviewData.company?.trim()) {
        const compRes = await api.generateCompanyInterview({
          company: interviewData.company.trim(),
        });
        setCompanyInterview(compRes);
      } else {
        setCompanyInterview(null);
      }

      toast.success("Interview prep generated!");
    } catch (err) {
      toast.error(
        err instanceof Error
          ? err.message
          : "Failed to generate interview questions"
      );
    } finally {
      setInterviewLoading(false);
    }
  };

  // ========================================================
  // 3. Career Roadmap State
  // ========================================================
  const [roadmapRole, setRoadmapRole] = useState("");
  const [roadmapResult, setRoadmapResult] = useState(null);
  const [roadmapLoading, setRoadmapLoading] = useState(false);

  const handleRoadmap = async (e) => {
    e.preventDefault();
    if (!roadmapRole.trim()) {
      toast.warning("Target career role is required.");
      return;
    }

    try {
      setRoadmapLoading(true);
      const res = await api.generateCareerRoadmap({ targetRole: roadmapRole });
      setRoadmapResult(res);
      toast.success("Career roadmap generated!");
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : "Failed to generate career roadmap"
      );
    } finally {
      setRoadmapLoading(false);
    }
  };

  // ========================================================
  // 4. Skill Gap State
  // ========================================================
  const [skillGapData, setSkillGapData] = useState({
    targetRole: "",
    currentSkills: "",
  });
  const [skillGapResult, setSkillGapResult] = useState(null);
  const [skillGapLoading, setSkillGapLoading] = useState(false);

  const handleSkillGap = async (e) => {
    e.preventDefault();
    if (!skillGapData.targetRole.trim()) {
      toast.warning("Target role is required.");
      return;
    }

    try {
      setSkillGapLoading(true);
      const res = await api.analyzeSkillGap(skillGapData);
      setSkillGapResult(res);
      toast.success("Skill gap analysis complete!");
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : "Failed to analyze skill gap"
      );
    } finally {
      setSkillGapLoading(false);
    }
  };

  // ========================================================
  // 5. Salary Predictor State
  // ========================================================
  const [salaryData, setSalaryData] = useState({
    experience: 2,
    skills: "",
  });
  const [salaryResult, setSalaryResult] = useState(null);
  const [salaryLoading, setSalaryLoading] = useState(false);

  const handleSalaryPredictor = async (e) => {
    e.preventDefault();
    try {
      setSalaryLoading(true);
      const res = await api.predictSalary({
        experience: Number(salaryData.experience) || 0,
        skills: salaryData.skills
          .split(",")
          .map((s) => s.trim())
          .filter(Boolean),
      });
      setSalaryResult(res);
      toast.success("Salary prediction calculated!");
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : "Failed to predict salary"
      );
    } finally {
      setSalaryLoading(false);
    }
  };

  // ========================================================
  // 6. Job Matcher State
  // ========================================================
  const [matchData, setMatchData] = useState({
    resumeText: "",
    jobDescription: "",
  });
  const [matchResult, setMatchResult] = useState(null);
  const [matchLoading, setMatchLoading] = useState(false);

  const handleJobMatch = async (e) => {
    e.preventDefault();
    if (!matchData.resumeText.trim() || !matchData.jobDescription.trim()) {
      toast.warning("Please provide both Resume text and Job description.");
      return;
    }

    try {
      setMatchLoading(true);
      const res = await api.matchJob(matchData);
      setMatchResult(res);
      toast.success("Resume match analysis complete!");
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : "Failed to match resume"
      );
    } finally {
      setMatchLoading(false);
    }
  };

  // ========================================================
  // 7. AI Career Chat State
  // ========================================================
  const [chatMessages, setChatMessages] = useState([
    {
      role: "assistant",
      text: "Hello! I am your AI Career Advisor powered by Gemini. Ask me anything about resume improvements, system design interviews, compensation negotiation, or career roadmaps.",
    },
  ]);
  const [chatInput, setChatInput] = useState("");
  const [chatLoading, setChatLoading] = useState(false);

  useEffect(() => {
    if (activeTab === "chat" && chatBottomRef.current) {
      chatBottomRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [chatMessages, chatLoading, activeTab]);

  const handleSendChat = async (e) => {
    e.preventDefault();
    if (!chatInput.trim() || chatLoading) return;

    const userMessage = chatInput.trim();
    setChatInput("");
    setChatMessages((prev) => [...prev, { role: "user", text: userMessage }]);

    try {
      setChatLoading(true);
      const res = await api.chatAI(userMessage);
      const replyText =
        typeof res === "object" && res.response
          ? res.response
          : typeof res === "string"
          ? res
          : "I'm ready to assist with your next career question!";
      const replySource =
        typeof res === "object" && res.source ? res.source : "FALLBACK";
      setChatMessages((prev) => [
        ...prev,
        { role: "assistant", text: replyText, source: replySource },
      ]);
    } catch (err) {
      const errorMsg =
        err instanceof Error
          ? err.message
          : "Gemini AI has reached its usage limit. Please try again later.";
      setChatMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          text: errorMsg,
          source: "ERROR",
        },
      ]);
      toast.error(errorMsg);
    } finally {
      setChatLoading(false);
    }
  };

  // ========================================================
  // 8. Success Predictor State
  // ========================================================
  const [successData, setSuccessData] = useState({
    atsScore: 85,
    yearsOfExperience: 3,
    projects: 4,
    certifications: 2,
    skills: "Java, Spring Boot, MySQL, Docker, AWS, React",
  });
  const [successResult, setSuccessResult] = useState(null);
  const [successLoading, setSuccessLoading] = useState(false);

  const handlePredictSuccess = async (e) => {
    e.preventDefault();
    try {
      setSuccessLoading(true);
      const skillList = successData.skills
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean);

      const res = await api.predictJobSuccess({
        atsScore: Number(successData.atsScore) || 0,
        yearsOfExperience: Number(successData.yearsOfExperience) || 0,
        projects: Number(successData.projects) || 0,
        certifications: Number(successData.certifications) || 0,
        skills: skillList,
      });
      setSuccessResult(res);
      toast.success("Readiness assessment generated!");
    } catch (err) {
      toast.error(
        err instanceof Error
          ? err.message
          : "Failed to predict application success"
      );
    } finally {
      setSuccessLoading(false);
    }
  };

  // ========================================================
  // 9. Mock Interview Simulator State
  // ========================================================
  const [mockData, setMockData] = useState({
    role: "Full Stack Developer",
    answers:
      "I design scalable RESTful APIs with Spring Boot and Node.js. For databases, I use PostgreSQL with indexing and caching via Redis. On the frontend, I build modular components in React with state management and automated tests.",
  });
  const [mockResult, setMockResult] = useState(null);
  const [mockLoading, setMockLoading] = useState(false);

  const handleEvaluateMock = async (e) => {
    e.preventDefault();
    if (!mockData.answers.trim()) {
      toast.warning("Please provide your interview answers for evaluation.");
      return;
    }
    try {
      setMockLoading(true);
      const answersList = mockData.answers
        .split("\n")
        .map((a) => a.trim())
        .filter(Boolean);
      const res = await api.evaluateInterview({
        role: mockData.role,
        answers: answersList.length > 0 ? answersList : [mockData.answers],
      });
      setMockResult(res);
      toast.success("Mock interview evaluated!");
    } catch (err) {
      toast.error(
        err instanceof Error
          ? err.message
          : "Failed to evaluate mock interview"
      );
    } finally {
      setMockLoading(false);
    }
  };

  // ========================================================
  // 10. Job Market Trends State
  // ========================================================
  const [trendRole, setTrendRole] = useState("Software Engineer");
  const [trendResult, setTrendResult] = useState(null);
  const [trendLoading, setTrendLoading] = useState(false);

  const handleFetchTrends = async (e) => {
    e.preventDefault();
    if (!trendRole.trim()) {
      toast.warning("Target role is required.");
      return;
    }
    try {
      setTrendLoading(true);
      const res = await api.analyzeJobMarket({ role: trendRole.trim() });
      setTrendResult(res);
      toast.success("Job market trends loaded!");
    } catch (err) {
      toast.error(
        err instanceof Error
          ? err.message
          : "Failed to analyze job market trends"
      );
    } finally {
      setTrendLoading(false);
    }
  };

  // ========================================================
  // 11. Resume Keyword Optimizer State
  // ========================================================
  const [keywordData, setKeywordData] = useState({
    targetRole: "Senior Backend Engineer",
    resumeText:
      "Experienced Software Engineer proficient in Java, Spring Boot, REST APIs, Microservices, and SQL database design.",
  });
  const [keywordResult, setKeywordResult] = useState(null);
  const [keywordLoading, setKeywordLoading] = useState(false);

  const handleOptimizeKeywords = async (e) => {
    e.preventDefault();
    if (!keywordData.resumeText.trim()) {
      toast.warning("Resume content is required.");
      return;
    }
    try {
      setKeywordLoading(true);
      const res = await api.optimizeResumeKeywords({
        targetRole: keywordData.targetRole,
        resumeText: keywordData.resumeText,
      });
      setKeywordResult(res);
      toast.success("Keywords optimized!");
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : "Failed to optimize keywords"
      );
    } finally {
      setKeywordLoading(false);
    }
  };

  // ========================================================
  // 12. ATS Score Predictor State
  // ========================================================
  const [atsData, setAtsData] = useState({
    resumeText:
      "Software Engineer with 3 years experience building cloud services in Python, Docker, AWS, and PostgreSQL.",
    jobDescription:
      "Looking for a Python Backend Engineer with hands-on AWS, Docker, Kubernetes, CI/CD, and relational databases.",
  });
  const [atsResult, setAtsResult] = useState(null);
  const [atsLoading, setAtsLoading] = useState(false);

  const handlePredictATS = async (e) => {
    e.preventDefault();
    if (!atsData.resumeText.trim() || !atsData.jobDescription.trim()) {
      toast.warning(
        "Please provide both resume content and job description."
      );
      return;
    }
    try {
      setAtsLoading(true);
      const res = await api.predictATSScore({
        resumeText: atsData.resumeText,
        jobDescription: atsData.jobDescription,
      });
      setAtsResult(res);
      toast.success("ATS score calculated!");
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : "Failed to predict ATS score"
      );
    } finally {
      setAtsLoading(false);
    }
  };

  // Tool categories and definitions
  const categories = [
    { id: "all", label: "All Tools (12)" },
    { id: "application", label: "Resumes & Applications" },
    { id: "interview", label: "Interview & Coaching" },
    { id: "career", label: "Career & Compensation" },
  ];

  const tools = [
    {
      id: "cover-letter",
      label: "Cover Letter",
      icon: FileText,
      category: "application",
      desc: "Role-tailored letters",
    },
    {
      id: "interview",
      label: "Interview Prep",
      icon: HelpCircle,
      category: "interview",
      desc: "Technical & behavioral questions",
    },
    {
      id: "mock-interview",
      label: "Mock Simulator",
      icon: Mic,
      category: "interview",
      desc: "Answer evaluation & scoring",
    },
    {
      id: "roadmap",
      label: "Career Roadmap",
      icon: Map,
      category: "career",
      desc: "Step-by-step milestone path",
    },
    {
      id: "skill-gap",
      label: "Skill Gap",
      icon: Target,
      category: "career",
      desc: "Competency gap analyzer",
    },
    {
      id: "salary",
      label: "Salary Predictor",
      icon: DollarSign,
      category: "career",
      desc: "Compensation benchmarking",
    },
    {
      id: "job-match",
      label: "Resume Matcher",
      icon: Cpu,
      category: "application",
      desc: "Direct resume-to-JD match",
    },
    {
      id: "ats-predictor",
      label: "ATS Predictor",
      icon: BarChart2,
      category: "application",
      desc: "ATS simulation score",
    },
    {
      id: "keywords",
      label: "Keyword Optimizer",
      icon: Key,
      category: "application",
      desc: "Extract & inject keywords",
    },
    {
      id: "trends",
      label: "Market Trends",
      icon: TrendingUp,
      category: "career",
      desc: "Industry tech demand trends",
    },
    {
      id: "success",
      label: "Success Predictor",
      icon: Award,
      category: "career",
      desc: "Application readiness score",
    },
    {
      id: "chat",
      label: "Career Mentor",
      icon: Sparkles,
      category: "interview",
      desc: "Interactive Gemini mentor",
    },
  ];

  const filteredTools =
    activeCategory === "all"
      ? tools
      : tools.filter((t) => t.category === activeCategory);

  return (
    <div className="page-container">
      {/* Page Heading */}
      <div className="page-heading">
        <div>
          <h2>AI Career Toolkit</h2>
          <p>
            Accelerate your career trajectory with 12 specialized Gemini AI
            intelligence tools
          </p>
        </div>
      </div>

      {/* Navigation Card: Category Filters & Tool Selector */}
      <div className="content-card ai-nav-card">
        {/* Category Pill Filters */}
        <div className="ai-category-tabs">
          {categories.map((cat) => (
            <button
              key={cat.id}
              className={`ai-category-btn ${
                activeCategory === cat.id ? "active" : ""
              }`}
              onClick={() => setActiveCategory(cat.id)}
            >
              {cat.label}
            </button>
          ))}
        </div>

        {/* Tools Pill List */}
        <div className="ai-tools-pill-group">
          {filteredTools.map((tool) => {
            const Icon = tool.icon;
            const isActive = activeTab === tool.id;
            return (
              <button
                key={tool.id}
                className={`ai-tool-btn ${isActive ? "active" : ""}`}
                onClick={() => switchTab(tool.id)}
              >
                <Icon size={15} />
                <span>{tool.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* ========================================================
          TOOL 1: COVER LETTER GENERATOR
          ======================================================== */}
      {activeTab === "cover-letter" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <FileText size={20} color="var(--primary-600)" />
                Tailored Cover Letter Generator
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Craft a tailored, ATS-friendly cover letter highlighting your
              relevant strengths for any company and role.
            </p>

            <form onSubmit={handleCoverLetter} className="app-form">
              <div className="form-group">
                <label>Target Company *</label>
                <input
                  type="text"
                  required
                  className="app-input"
                  placeholder="e.g. Stripe, Amazon, Google"
                  value={coverData.companyName}
                  onChange={(e) =>
                    setCoverData({
                      ...coverData,
                      companyName: e.target.value,
                    })
                  }
                />
              </div>

              <div className="form-group">
                <label>Target Job Role *</label>
                <input
                  type="text"
                  required
                  className="app-input"
                  placeholder="e.g. Senior Full Stack Engineer"
                  value={coverData.jobRole}
                  onChange={(e) =>
                    setCoverData({ ...coverData, jobRole: e.target.value })
                  }
                />
              </div>

              <div
                style={{
                  display: "flex",
                  gap: "10px",
                  marginTop: "8px",
                }}
              >
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={coverLoading}
                >
                  {coverLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <Sparkles size={16} />
                  )}
                  <span>
                    {coverLoading
                      ? "Drafting Cover Letter..."
                      : "Generate Cover Letter"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setCoverData({ companyName: "", jobRole: "" });
                    setCoverResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title="Generated Cover Letter"
            source={coverResult?.source}
            loading={coverLoading}
            loadingText="Drafting personalized cover letter with Gemini AI..."
            hasResult={Boolean(coverResult)}
            copyContent={coverResult?.coverLetter || coverResult}
            onReset={() => setCoverResult(null)}
            emptyTitle="No cover letter generated yet"
            emptyText="Fill in the target company and role on the left to generate your custom letter."
            emptyIcon={FileText}
          >
            {coverResult && (
              <div
                style={{
                  background: "var(--bg-app)",
                  padding: "20px",
                  borderRadius: "var(--radius-lg)",
                  border: "1px solid var(--border-subtle)",
                  maxHeight: "520px",
                  overflowY: "auto",
                }}
              >
                <AIFormattedText
                  content={coverResult.coverLetter || coverResult}
                />
              </div>
            )}
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 2: INTERVIEW PREPARATION
          ======================================================== */}
      {activeTab === "interview" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <HelpCircle size={20} color="var(--primary-600)" />
                Interview Questions Generator
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Generate high-yield technical, architectural, and behavioral
              interview questions tailored to your exact tech stack.
            </p>

            <form onSubmit={handleInterviewQuestions} className="app-form">
              <div className="form-group">
                <label>Job Role *</label>
                <input
                  type="text"
                  required
                  className="app-input"
                  placeholder="e.g. Backend Engineer, Systems Architect"
                  value={interviewData.jobRole}
                  onChange={(e) =>
                    setInterviewData({
                      ...interviewData,
                      jobRole: e.target.value,
                    })
                  }
                />
              </div>

              <div className="form-group">
                <label>Core Skills & Technologies</label>
                <input
                  type="text"
                  className="app-input"
                  placeholder="e.g. Java, Spring Boot, MySQL, Microservices, Redis"
                  value={interviewData.skills}
                  onChange={(e) =>
                    setInterviewData({
                      ...interviewData,
                      skills: e.target.value,
                    })
                  }
                />
              </div>

              <div className="form-group">
                <label>Target Company (Optional)</label>
                <input
                  type="text"
                  className="app-input"
                  placeholder="e.g. Netflix, Uber, Stripe"
                  value={interviewData.company}
                  onChange={(e) =>
                    setInterviewData({
                      ...interviewData,
                      company: e.target.value,
                    })
                  }
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={interviewLoading}
                >
                  {interviewLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <HelpCircle size={16} />
                  )}
                  <span>
                    {interviewLoading
                      ? "Synthesizing Questions..."
                      : "Generate Interview Questions"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setInterviewData({ jobRole: "", skills: "", company: "" });
                    setQuestionsResult(null);
                    setInterviewQuestions([]);
                    setCompanyInterview(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title="Role-Targeted Questions"
            source={questionsResult?.source}
            loading={interviewLoading}
            loadingText="Preparing role-specific technical questions..."
            hasResult={interviewQuestions.length > 0}
            copyContent={interviewQuestions.join("\n\n")}
            onReset={() => {
              setQuestionsResult(null);
              setInterviewQuestions([]);
              setCompanyInterview(null);
            }}
            emptyTitle="No interview questions generated yet"
            emptyText="Enter your job role and core tech stack on the left to synthesize interview questions."
            emptyIcon={HelpCircle}
          >
            <div
              style={{
                display: "flex",
                flexDirection: "column",
                gap: "12px",
                maxHeight: "520px",
                overflowY: "auto",
              }}
            >
              {interviewQuestions.map((q, idx) => (
                <div key={idx} className="ai-interview-item">
                  <span className="ai-interview-badge">{idx + 1}</span>
                  <div style={{ flex: 1 }}>
                    <AIFormattedText content={q} />
                  </div>
                </div>
              ))}

              {companyInterview && (
                <div
                  style={{
                    marginTop: "12px",
                    padding: "16px 18px",
                    background: "var(--info-bg)",
                    border: "1px solid var(--info-border)",
                    borderRadius: "var(--radius-lg)",
                  }}
                >
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "space-between",
                      marginBottom: "8px",
                    }}
                  >
                    <h4
                      style={{
                        color: "var(--info-dark)",
                        margin: 0,
                        fontSize: "14px",
                        fontWeight: 700,
                      }}
                    >
                      {companyInterview.company} Key Coding Topics
                    </h4>
                  </div>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {companyInterview.codingTopics?.map((t, i) => (
                      <span key={i} className="status-pill interview">
                        {t}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 3: CAREER ROADMAP
          ======================================================== */}
      {activeTab === "roadmap" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <Map size={20} color="var(--primary-600)" />
                Career Progression Roadmap
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Get a structured milestone roadmap mapping out competencies,
              architecture topics, and project milestones to achieve your target
              role.
            </p>

            <form onSubmit={handleRoadmap} className="app-form">
              <div className="form-group">
                <label>Target Career Role *</label>
                <input
                  type="text"
                  required
                  className="app-input"
                  placeholder="e.g. Lead Software Engineer, DevOps Architect"
                  value={roadmapRole}
                  onChange={(e) => setRoadmapRole(e.target.value)}
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={roadmapLoading}
                >
                  {roadmapLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <Map size={16} />
                  )}
                  <span>
                    {roadmapLoading
                      ? "Constructing Pathway..."
                      : "Generate Career Roadmap"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setRoadmapRole("");
                    setRoadmapResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title={`Roadmap: ${
              roadmapResult?.targetRole || roadmapRole || "Milestones"
            }`}
            source={roadmapResult?.source}
            loading={roadmapLoading}
            loadingText="Constructing structured milestone trajectory..."
            hasResult={Boolean(roadmapResult)}
            copyContent={roadmapResult?.roadmap?.join("\n\n")}
            onReset={() => setRoadmapResult(null)}
            emptyTitle="No roadmap generated yet"
            emptyText="Provide your target career milestone on the left to chart a learning roadmap."
            emptyIcon={Map}
          >
            {roadmapResult && (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "14px",
                  maxHeight: "520px",
                  overflowY: "auto",
                }}
              >
                {roadmapResult.roadmap?.map((step, idx) => (
                  <div key={idx} className="ai-milestone-item">
                    <div className="ai-milestone-badge">{idx + 1}</div>
                    <div style={{ flex: 1 }}>
                      <strong
                        style={{
                          color: "var(--text-heading)",
                          display: "block",
                          marginBottom: "4px",
                          fontSize: "14px",
                        }}
                      >
                        Milestone {idx + 1}
                      </strong>
                      <AIFormattedText content={step} />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 4: SKILL GAP ANALYZER
          ======================================================== */}
      {activeTab === "skill-gap" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <Target size={20} color="var(--warning)" />
                Skill Gap Analyzer
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Analyze the gap between your current skill set and current
              industry requirements, with an estimated learning timeline.
            </p>

            <form onSubmit={handleSkillGap} className="app-form">
              <div className="form-group">
                <label>Target Role *</label>
                <input
                  type="text"
                  required
                  className="app-input"
                  placeholder="e.g. Senior Java Backend Engineer"
                  value={skillGapData.targetRole}
                  onChange={(e) =>
                    setSkillGapData({
                      ...skillGapData,
                      targetRole: e.target.value,
                    })
                  }
                />
              </div>

              <div className="form-group">
                <label>Current Skills (Comma-separated)</label>
                <textarea
                  rows={3}
                  className="app-textarea"
                  placeholder="e.g. Java, Spring Boot, MySQL, Git, Docker"
                  value={skillGapData.currentSkills}
                  onChange={(e) =>
                    setSkillGapData({
                      ...skillGapData,
                      currentSkills: e.target.value,
                    })
                  }
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={skillGapLoading}
                >
                  {skillGapLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <Target size={16} />
                  )}
                  <span>
                    {skillGapLoading
                      ? "Evaluating Skills..."
                      : "Analyze Skill Gap"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setSkillGapData({ targetRole: "", currentSkills: "" });
                    setSkillGapResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title="Skill Gap Assessment"
            source={skillGapResult?.source}
            loading={skillGapLoading}
            loadingText="Analyzing skill overlap and learning roadmap..."
            hasResult={Boolean(skillGapResult)}
            copyContent={JSON.stringify(skillGapResult, null, 2)}
            onReset={() => setSkillGapResult(null)}
            emptyTitle="No skill gap analysis performed yet"
            emptyText="Enter your current skill set and target role on the left to analyze gaps."
            emptyIcon={Target}
          >
            {skillGapResult && (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "18px",
                  maxHeight: "520px",
                  overflowY: "auto",
                }}
              >
                <div
                  style={{
                    background: "var(--warning-bg)",
                    border: "1px solid var(--warning-border)",
                    padding: "14px 18px",
                    borderRadius: "var(--radius-md)",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <strong style={{ color: "var(--warning-dark)" }}>
                    Estimated Learning Timeline:
                  </strong>
                  <span
                    style={{ fontWeight: 800, color: "var(--warning-dark)" }}
                  >
                    {skillGapResult.estimatedDuration || "4 - 8 Weeks"}
                  </span>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Existing Verified Skills:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {skillGapResult.existingSkills?.map((s, idx) => (
                      <span key={idx} className="status-pill offered">
                        ✓ {s}
                      </span>
                    ))}
                  </div>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Missing Target Competencies:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {skillGapResult.missingSkills?.map((s, idx) => (
                      <span key={idx} className="status-pill applied">
                        + {s}
                      </span>
                    ))}
                  </div>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Recommended Action Plan:
                  </h4>
                  <ul className="suggestions-list">
                    {skillGapResult.learningPlan?.map((plan, idx) => (
                      <li key={idx}>
                        <AIFormattedText content={plan} />
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 5: SALARY PREDICTOR
          ======================================================== */}
      {activeTab === "salary" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <DollarSign size={20} color="var(--success)" />
                Salary Predictor & Benchmarks
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Benchmark your compensation potential and industry demand
              according to your experience and skills.
            </p>

            <form onSubmit={handleSalaryPredictor} className="app-form">
              <div className="form-group">
                <label>Years of Professional Experience</label>
                <input
                  type="number"
                  min="0"
                  max="30"
                  className="app-input"
                  value={salaryData.experience}
                  onChange={(e) =>
                    setSalaryData({
                      ...salaryData,
                      experience: e.target.value,
                    })
                  }
                />
              </div>

              <div className="form-group">
                <label>Skills & Technologies (Comma-separated)</label>
                <textarea
                  rows={3}
                  className="app-textarea"
                  placeholder="e.g. Python, React, PostgreSQL, Docker, AWS, Node.js"
                  value={salaryData.skills}
                  onChange={(e) =>
                    setSalaryData({ ...salaryData, skills: e.target.value })
                  }
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={salaryLoading}
                >
                  {salaryLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <DollarSign size={16} />
                  )}
                  <span>
                    {salaryLoading
                      ? "Benchmarking..."
                      : "Estimate Compensation"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setSalaryData({ experience: 2, skills: "" });
                    setSalaryResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title="Compensation Projection"
            source={salaryResult?.source}
            loading={salaryLoading}
            loadingText="Synthesizing compensation benchmarks..."
            hasResult={Boolean(salaryResult)}
            copyContent={JSON.stringify(salaryResult, null, 2)}
            onReset={() => setSalaryResult(null)}
            emptyTitle="No compensation data calculated yet"
            emptyText="Enter your experience and skills on the left to calculate compensation."
            emptyIcon={DollarSign}
          >
            {salaryResult && (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "18px",
                }}
              >
                <div className="ai-score-banner">
                  <span className="banner-label">
                    Estimated Compensation Range
                  </span>
                  <div className="banner-value">
                    {salaryResult.estimatedSalary}
                  </div>
                  <span className="banner-subtext">
                    Level: <strong>{salaryResult.experienceLevel}</strong> •
                    Market Demand: <strong>{salaryResult.marketDemand}</strong>
                  </span>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Tips to Maximize Offer:
                  </h4>
                  <ul className="suggestions-list">
                    {salaryResult.suggestions?.map((sug, idx) => (
                      <li key={idx}>
                        <AIFormattedText content={sug} />
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 6: JOB MATCHER
          ======================================================== */}
      {activeTab === "job-match" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <Cpu size={20} color="var(--primary-600)" />
                Resume vs Job Description Matcher
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Compare your resume against specific job requirements for an
              instant match alignment score.
            </p>

            <form onSubmit={handleJobMatch} className="app-form">
              <div className="form-group">
                <label>Resume Text *</label>
                <textarea
                  rows={4}
                  required
                  className="app-textarea"
                  placeholder="Paste your resume content, experience, or skills..."
                  value={matchData.resumeText}
                  onChange={(e) =>
                    setMatchData({
                      ...matchData,
                      resumeText: e.target.value,
                    })
                  }
                />
              </div>

              <div className="form-group">
                <label>Job Description *</label>
                <textarea
                  rows={4}
                  required
                  className="app-textarea"
                  placeholder="Paste the target job description..."
                  value={matchData.jobDescription}
                  onChange={(e) =>
                    setMatchData({
                      ...matchData,
                      jobDescription: e.target.value,
                    })
                  }
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={matchLoading}
                >
                  {matchLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <Cpu size={16} />
                  )}
                  <span>
                    {matchLoading ? "Comparing..." : "Calculate Job Match"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setMatchData({ resumeText: "", jobDescription: "" });
                    setMatchResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title="Match Score & Keywords"
            source={matchResult?.source}
            loading={matchLoading}
            loadingText="Evaluating keyword overlap and alignment..."
            hasResult={Boolean(matchResult)}
            copyContent={JSON.stringify(matchResult, null, 2)}
            onReset={() => setMatchResult(null)}
            emptyTitle="No match computed yet"
            emptyText="Paste your resume and target JD on the left to evaluate compatibility."
            emptyIcon={Cpu}
          >
            {matchResult && (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "18px",
                }}
              >
                <div className="ai-score-banner">
                  <span className="banner-label">Job Match Percentage</span>
                  <div className="banner-value">
                    {matchResult.atsScore || 0}%
                  </div>
                  <span className="banner-subtext">
                    {(Number(matchResult.atsScore) || 0) >= 80
                      ? "High Candidate Alignment"
                      : "Moderate Alignment — Review Missing Keywords"}
                  </span>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Matched Skills:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {matchResult.matchedSkills?.map((s, idx) => (
                      <span key={idx} className="status-pill offered">
                        ✓ {s}
                      </span>
                    ))}
                  </div>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Missing Target Keywords:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {matchResult.missingSkills?.map((s, idx) => (
                      <span key={idx} className="status-pill applied">
                        + {s}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 7: CAREER CHAT
          ======================================================== */}
      {activeTab === "chat" && (
        <div className="chat-card">
          <div
            className="card-header"
            style={{ padding: "18px 24px", marginBottom: 0 }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
              <Sparkles size={20} color="var(--primary-600)" />
              <h3 style={{ margin: 0 }}>AI Career Mentor</h3>
            </div>
            <button
              type="button"
              className="secondary-button"
              style={{ fontSize: "12px", padding: "4px 10px" }}
              onClick={() =>
                setChatMessages([
                  {
                    role: "assistant",
                    text: "Conversation reset. How can I help you today?",
                  },
                ])
              }
            >
              Clear Chat
            </button>
          </div>

          <div className="chat-messages-box">
            {chatMessages.map((msg, idx) => (
              <div
                key={idx}
                className={`chat-message-row ${
                  msg.role === "user" ? "user" : "ai"
                }`}
              >
                <div
                  className={`chat-avatar ${
                    msg.role === "user" ? "user" : "ai"
                  }`}
                >
                  {msg.role === "user" ? "You" : "AI"}
                </div>
                <div
                  className={`chat-bubble ${
                    msg.role === "user" ? "user" : "ai"
                  }`}
                >
                  <AIFormattedText content={msg.text} />
                </div>
              </div>
            ))}
            {chatLoading && (
              <div className="chat-message-row ai">
                <div className="chat-avatar ai">AI</div>
                <div
                  className="chat-bubble ai"
                  style={{ display: "flex", alignItems: "center", gap: "8px" }}
                >
                  <RefreshCw size={14} className="spinning" />
                  <span>Synthesizing response with Gemini...</span>
                </div>
              </div>
            )}
            <div ref={chatBottomRef} />
          </div>

          <form onSubmit={handleSendChat} className="chat-input-bar">
            <input
              type="text"
              className="app-input"
              placeholder="Ask anything about resumes, negotiation, or interviews..."
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              disabled={chatLoading}
            />
            <button
              type="submit"
              className="primary-button"
              disabled={chatLoading || !chatInput.trim()}
              aria-label="Send message"
            >
              <Send size={16} />
            </button>
          </form>
        </div>
      )}

      {/* ========================================================
          TOOL 8: SUCCESS PREDICTOR
          ======================================================== */}
      {activeTab === "success" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <Award size={20} color="var(--primary-600)" />
                Application Readiness Assessment
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Evaluate your application readiness and interview potential based
              on your ATS score, experience, and project portfolio.
            </p>

            <form onSubmit={handlePredictSuccess} className="app-form">
              <div className="form-grid-2">
                <div className="form-group">
                  <label>ATS Score (0 - 100)</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    className="app-input"
                    value={successData.atsScore}
                    onChange={(e) =>
                      setSuccessData({
                        ...successData,
                        atsScore: e.target.value,
                      })
                    }
                  />
                </div>

                <div className="form-group">
                  <label>Years of Experience</label>
                  <input
                    type="number"
                    min="0"
                    max="30"
                    className="app-input"
                    value={successData.yearsOfExperience}
                    onChange={(e) =>
                      setSuccessData({
                        ...successData,
                        yearsOfExperience: e.target.value,
                      })
                    }
                  />
                </div>
              </div>

              <div className="form-grid-2">
                <div className="form-group">
                  <label>Completed Projects</label>
                  <input
                    type="number"
                    min="0"
                    max="50"
                    className="app-input"
                    value={successData.projects}
                    onChange={(e) =>
                      setSuccessData({
                        ...successData,
                        projects: e.target.value,
                      })
                    }
                  />
                </div>

                <div className="form-group">
                  <label>Certifications</label>
                  <input
                    type="number"
                    min="0"
                    max="20"
                    className="app-input"
                    value={successData.certifications}
                    onChange={(e) =>
                      setSuccessData({
                        ...successData,
                        certifications: e.target.value,
                      })
                    }
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Technical Skills (Comma-separated)</label>
                <textarea
                  rows={3}
                  className="app-textarea"
                  placeholder="e.g. Python, React, PostgreSQL, Docker, AWS, Node.js"
                  value={successData.skills}
                  onChange={(e) =>
                    setSuccessData({
                      ...successData,
                      skills: e.target.value,
                    })
                  }
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={successLoading}
                >
                  {successLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <Award size={16} />
                  )}
                  <span>
                    {successLoading
                      ? "Assessing Readiness..."
                      : "Assess Readiness"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setSuccessData({
                      atsScore: 85,
                      yearsOfExperience: 3,
                      projects: 4,
                      certifications: 2,
                      skills: "Java, Spring Boot, MySQL, Docker, AWS, React",
                    });
                    setSuccessResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title="Readiness Assessment"
            source={successResult?.source}
            loading={successLoading}
            loadingText="Evaluating candidate profile strength..."
            hasResult={Boolean(successResult)}
            copyContent={JSON.stringify(successResult, null, 2)}
            onReset={() => setSuccessResult(null)}
            emptyTitle="No assessment performed yet"
            emptyText="Adjust your parameters on the left to compute your readiness score."
            emptyIcon={Award}
          >
            {successResult && (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "18px",
                }}
              >
                <div className="ai-score-banner">
                  <span className="banner-label">
                    Application Readiness Score
                  </span>
                  <div className="banner-value">
                    {successResult.successProbability || 0}%
                  </div>
                  <span className="banner-subtext">
                    Profile Level:{" "}
                    <strong>
                      {successResult.experienceLevel || "Mid-Level"}
                    </strong>
                  </span>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Key Profile Strengths:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {successResult.strengths?.map((s, idx) => (
                      <span key={idx} className="status-pill offered">
                        ✓ {s}
                      </span>
                    ))}
                  </div>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Recommendations to Boost Callbacks:
                  </h4>
                  <ul className="suggestions-list">
                    {successResult.recommendations?.map((r, idx) => (
                      <li key={idx}>
                        <AIFormattedText content={r} />
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 9: MOCK INTERVIEW SIMULATOR
          ======================================================== */}
      {activeTab === "mock-interview" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <Mic size={20} color="var(--primary-600)" />
                AI Mock Interview Simulator
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Submit your answers to technical and behavioral questions for
              qualitative scoring and actionable feedback.
            </p>

            <form onSubmit={handleEvaluateMock} className="app-form">
              <div className="form-group">
                <label>Target Role *</label>
                <input
                  type="text"
                  required
                  className="app-input"
                  placeholder="e.g. Senior Frontend Engineer"
                  value={mockData.role}
                  onChange={(e) =>
                    setMockData({ ...mockData, role: e.target.value })
                  }
                />
              </div>

              <div className="form-group">
                <label>Your Interview Answers *</label>
                <textarea
                  rows={6}
                  required
                  className="app-textarea"
                  placeholder="Enter one or more interview answers (separated by newlines)..."
                  value={mockData.answers}
                  onChange={(e) =>
                    setMockData({ ...mockData, answers: e.target.value })
                  }
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={mockLoading}
                >
                  {mockLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <Mic size={16} />
                  )}
                  <span>
                    {mockLoading
                      ? "Evaluating Answers..."
                      : "Evaluate Answers"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setMockData({ role: "", answers: "" });
                    setMockResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title="Interview Performance Verdict"
            source={mockResult?.source}
            loading={mockLoading}
            loadingText="Analyzing answer quality with Gemini AI..."
            hasResult={Boolean(mockResult)}
            copyContent={JSON.stringify(mockResult, null, 2)}
            onReset={() => setMockResult(null)}
            emptyTitle="No interview answers evaluated yet"
            emptyText="Enter your target role and sample answers on the left to evaluate performance."
            emptyIcon={Mic}
          >
            {mockResult && (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "18px",
                  maxHeight: "520px",
                  overflowY: "auto",
                }}
              >
                <div className="ai-score-banner">
                  <span className="banner-label">Performance Score</span>
                  <div className="banner-value">
                    {mockResult.score || 0} / 100
                  </div>
                  <span className="banner-subtext">
                    {mockResult.recommendation}
                  </span>
                </div>

                {mockResult.strengths?.length > 0 && (
                  <div>
                    <h4
                      style={{
                        fontSize: "13.5px",
                        fontWeight: 700,
                        color: "var(--text-heading)",
                        marginBottom: "8px",
                        display: "flex",
                        alignItems: "center",
                        gap: "6px",
                      }}
                    >
                      <CheckCircle2 size={16} color="var(--success)" />
                      Strengths:
                    </h4>
                    <div
                      style={{
                        display: "flex",
                        flexWrap: "wrap",
                        gap: "6px",
                      }}
                    >
                      {mockResult.strengths.map((s, idx) => (
                        <span key={idx} className="status-pill offered">
                          ✓ {s}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {mockResult.feedback?.length > 0 && (
                  <div>
                    <h4
                      style={{
                        fontSize: "13.5px",
                        fontWeight: 700,
                        color: "var(--text-heading)",
                        marginBottom: "8px",
                      }}
                    >
                      Evaluation Feedback:
                    </h4>
                    <ul className="suggestions-list">
                      {mockResult.feedback.map((f, idx) => (
                        <li key={idx}>
                          <AIFormattedText content={f} />
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                {mockResult.improvements?.length > 0 && (
                  <div>
                    <h4
                      style={{
                        fontSize: "13.5px",
                        fontWeight: 700,
                        color: "var(--text-heading)",
                        marginBottom: "8px",
                        display: "flex",
                        alignItems: "center",
                        gap: "6px",
                      }}
                    >
                      <AlertTriangle size={16} color="var(--warning)" />
                      Suggested Improvements:
                    </h4>
                    <ul className="suggestions-list">
                      {mockResult.improvements.map((imp, idx) => (
                        <li key={idx}>
                          <AIFormattedText content={imp} />
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 10: JOB MARKET TRENDS
          ======================================================== */}
      {activeTab === "trends" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <TrendingUp size={20} color="var(--primary-600)" />
                Job Market & Tech Trends
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Analyze hiring demand, emerging technologies, and compensation
              ranges for any target tech discipline.
            </p>

            <form onSubmit={handleFetchTrends} className="app-form">
              <div className="form-group">
                <label>Target Role *</label>
                <input
                  type="text"
                  required
                  className="app-input"
                  placeholder="e.g. Data Engineer, DevOps Specialist, React Developer"
                  value={trendRole}
                  onChange={(e) => setTrendRole(e.target.value)}
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={trendLoading}
                >
                  {trendLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <TrendingUp size={16} />
                  )}
                  <span>
                    {trendLoading
                      ? "Synthesizing Trends..."
                      : "Analyze Market Trends"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setTrendRole("");
                    setTrendResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title={`Market Intelligence: ${trendResult?.role || trendRole || "Overview"}`}
            source={trendResult?.source}
            loading={trendLoading}
            loadingText="Synthesizing industry trends with Gemini AI..."
            hasResult={Boolean(trendResult)}
            copyContent={JSON.stringify(trendResult, null, 2)}
            onReset={() => setTrendResult(null)}
            emptyTitle="No trend analysis run yet"
            emptyText="Enter any tech or corporate job role on the left to analyze market dynamics."
            emptyIcon={TrendingUp}
          >
            {trendResult && (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "18px",
                }}
              >
                <div className="ai-stat-grid-3">
                  <div>
                    <span
                      style={{
                        fontSize: "11px",
                        fontWeight: 700,
                        color: "var(--text-muted)",
                        textTransform: "uppercase",
                      }}
                    >
                      Market Demand
                    </span>
                    <div
                      style={{
                        fontSize: "16px",
                        fontWeight: 800,
                        color: "var(--primary-700)",
                        marginTop: "4px",
                      }}
                    >
                      {trendResult.marketDemand || "High"}
                    </div>
                  </div>
                  <div>
                    <span
                      style={{
                        fontSize: "11px",
                        fontWeight: 700,
                        color: "var(--text-muted)",
                        textTransform: "uppercase",
                      }}
                    >
                      Hiring Growth
                    </span>
                    <div
                      style={{
                        fontSize: "16px",
                        fontWeight: 800,
                        color: "var(--success-dark, #16a34a)",
                        marginTop: "4px",
                      }}
                    >
                      {trendResult.hiringGrowth || "Positive"}
                    </div>
                  </div>
                  <div>
                    <span
                      style={{
                        fontSize: "11px",
                        fontWeight: 700,
                        color: "var(--text-muted)",
                        textTransform: "uppercase",
                      }}
                    >
                      Salary Range
                    </span>
                    <div
                      style={{
                        fontSize: "14px",
                        fontWeight: 700,
                        color: "var(--text-heading)",
                        marginTop: "4px",
                      }}
                    >
                      {trendResult.averageSalaryRange || "Competitive"}
                    </div>
                  </div>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Top In-Demand Skills for {trendResult.role}:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {trendResult.topSkills?.map((s, idx) => (
                      <span key={idx} className="status-pill applied">
                        {s}
                      </span>
                    ))}
                  </div>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Emerging Technologies:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {trendResult.trendingTechnologies?.map((t, idx) => (
                      <span key={idx} className="status-pill offered">
                        🔥 {t}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 11: RESUME KEYWORD OPTIMIZER
          ======================================================== */}
      {activeTab === "keywords" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <Key size={20} color="var(--primary-600)" />
                Resume Keyword Optimizer
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Scan your resume against target role requirements to extract
              present terms and identify missing strategic keywords.
            </p>

            <form onSubmit={handleOptimizeKeywords} className="app-form">
              <div className="form-group">
                <label>Target Role *</label>
                <input
                  type="text"
                  required
                  className="app-input"
                  placeholder="e.g. Senior Backend Engineer"
                  value={keywordData.targetRole}
                  onChange={(e) =>
                    setKeywordData({
                      ...keywordData,
                      targetRole: e.target.value,
                    })
                  }
                />
              </div>

              <div className="form-group">
                <label>Resume Content / Bullet Points *</label>
                <textarea
                  rows={6}
                  required
                  className="app-textarea"
                  placeholder="Paste your resume text or experience section..."
                  value={keywordData.resumeText}
                  onChange={(e) =>
                    setKeywordData({
                      ...keywordData,
                      resumeText: e.target.value,
                    })
                  }
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={keywordLoading}
                >
                  {keywordLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <Key size={16} />
                  )}
                  <span>
                    {keywordLoading
                      ? "Optimizing Keywords..."
                      : "Optimize Keywords"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setKeywordData({ targetRole: "", resumeText: "" });
                    setKeywordResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title="Keyword Intelligence & Score"
            source={keywordResult?.source}
            loading={keywordLoading}
            loadingText="Extracting high-impact ATS keywords with Gemini..."
            hasResult={Boolean(keywordResult)}
            copyContent={JSON.stringify(keywordResult, null, 2)}
            onReset={() => setKeywordResult(null)}
            emptyTitle="No keywords analyzed yet"
            emptyText="Paste your resume text on the left to extract strategic keywords."
            emptyIcon={Key}
          >
            {keywordResult && (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "18px",
                }}
              >
                <div className="ai-score-banner">
                  <span className="banner-label">
                    Keyword Optimization Score
                  </span>
                  <div className="banner-value">
                    {keywordResult.optimizationScore || 0}%
                  </div>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Identified In Resume:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {keywordResult.presentKeywords?.map((k, idx) => (
                      <span key={idx} className="status-pill offered">
                        ✓ {k}
                      </span>
                    ))}
                  </div>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Missing Strategic Keywords:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {keywordResult.missingKeywords?.map((k, idx) => (
                      <span key={idx} className="status-pill applied">
                        + {k}
                      </span>
                    ))}
                  </div>
                </div>

                {keywordResult.recommendedKeywords?.length > 0 && (
                  <div>
                    <h4
                      style={{
                        fontSize: "13.5px",
                        fontWeight: 700,
                        color: "var(--text-heading)",
                        marginBottom: "8px",
                      }}
                    >
                      Recommendations:
                    </h4>
                    <ul className="suggestions-list">
                      {keywordResult.recommendedKeywords.map((rec, idx) => (
                        <li key={idx}>
                          <AIFormattedText content={rec} />
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}
          </AIResultCard>
        </div>
      )}

      {/* ========================================================
          TOOL 12: ATS SCORE PREDICTOR
          ======================================================== */}
      {activeTab === "ats-predictor" && (
        <div className="dashboard-grid-2">
          <div className="content-card">
            <div className="card-header">
              <h3>
                <BarChart2 size={20} color="var(--primary-600)" />
                ATS Compatibility Predictor
              </h3>
            </div>
            <p
              style={{
                color: "var(--text-muted)",
                fontSize: "13.5px",
                marginBottom: "20px",
              }}
            >
              Simulate enterprise ATS applicant tracking scoring by analyzing
              your resume text against any job posting.
            </p>

            <form onSubmit={handlePredictATS} className="app-form">
              <div className="form-group">
                <label>Resume Content *</label>
                <textarea
                  rows={4}
                  required
                  className="app-textarea"
                  placeholder="Paste your resume content..."
                  value={atsData.resumeText}
                  onChange={(e) =>
                    setAtsData({ ...atsData, resumeText: e.target.value })
                  }
                />
              </div>

              <div className="form-group">
                <label>Target Job Description *</label>
                <textarea
                  rows={4}
                  required
                  className="app-textarea"
                  placeholder="Paste the job description or requirements..."
                  value={atsData.jobDescription}
                  onChange={(e) =>
                    setAtsData({
                      ...atsData,
                      jobDescription: e.target.value,
                    })
                  }
                />
              </div>

              <div style={{ display: "flex", gap: "10px", marginTop: "8px" }}>
                <button
                  type="submit"
                  className="primary-button"
                  style={{ flex: 1 }}
                  disabled={atsLoading}
                >
                  {atsLoading ? (
                    <RefreshCw size={16} className="spinning" />
                  ) : (
                    <BarChart2 size={16} />
                  )}
                  <span>
                    {atsLoading
                      ? "Simulating ATS Parser..."
                      : "Predict ATS Match"}
                  </span>
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setAtsData({ resumeText: "", jobDescription: "" });
                    setAtsResult(null);
                  }}
                  title="Reset fields"
                >
                  <RotateCcw size={15} />
                </button>
              </div>
            </form>
          </div>

          <AIResultCard
            title="ATS Compatibility Score"
            source={atsResult?.source}
            loading={atsLoading}
            loadingText="Simulating ATS parser with Gemini AI..."
            hasResult={Boolean(atsResult)}
            copyContent={JSON.stringify(atsResult, null, 2)}
            onReset={() => setAtsResult(null)}
            emptyTitle="No ATS score calculated yet"
            emptyText="Paste your resume text and target job description on the left to compute ATS match."
            emptyIcon={BarChart2}
          >
            {atsResult && (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "18px",
                }}
              >
                <div className="ai-score-banner">
                  <span className="banner-label">ATS Compatibility Score</span>
                  <div className="banner-value">{atsResult.atsScore || 0}%</div>
                  <span className="banner-subtext">{atsResult.suggestion}</span>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Matched ATS Keywords:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {atsResult.matchedKeywords?.map((k, idx) => (
                      <span key={idx} className="status-pill offered">
                        ✓ {k}
                      </span>
                    ))}
                  </div>
                </div>

                <div>
                  <h4
                    style={{
                      fontSize: "13.5px",
                      fontWeight: 700,
                      color: "var(--text-heading)",
                      marginBottom: "8px",
                    }}
                  >
                    Missing ATS Keywords:
                  </h4>
                  <div
                    style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}
                  >
                    {atsResult.missingKeywords?.map((k, idx) => (
                      <span key={idx} className="status-pill applied">
                        + {k}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </AIResultCard>
        </div>
      )}
    </div>
  );
}

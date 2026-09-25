import { LayoutDashboard, FolderKanban, Wand2 } from "lucide-react";

export default function ResumeTabs({ activeTab, onTabChange }) {
  const tabs = [
    {
      id: "overview",
      label: "Resume Overview",
      icon: LayoutDashboard,
    },
    {
      id: "manager",
      label: "My Resumes",
      icon: FolderKanban,
    },
    {
      id: "builder",
      label: "PDF Resume Builder",
      icon: Wand2,
    },
  ];

  return (
    <div className="resume-tabs-nav" role="tablist" aria-label="Resume Hub Navigation">
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const isActive = activeTab === tab.id;
        return (
          <button
            key={tab.id}
            role="tab"
            aria-selected={isActive}
            className={`resume-tab-btn ${isActive ? "active" : ""}`}
            onClick={() => onTabChange(tab.id)}
          >
            <Icon size={16} />
            <span>{tab.label}</span>
          </button>
        );
      })}
    </div>
  );
}

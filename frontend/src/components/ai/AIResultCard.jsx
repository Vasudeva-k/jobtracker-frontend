import { useState } from "react";
import { Copy, Check, RefreshCw } from "lucide-react";
import { useToast } from "../../context/ToastContext";
import AISourceBadge from "./AISourceBadge";

export default function AIResultCard({
  title,
  source,
  loading,
  loadingText = "Analyzing with Gemini AI...",
  emptyTitle = "No results yet",
  emptyText = "Fill out the parameters on the left and submit to generate AI intelligence.",
  emptyIcon: EmptyIcon,
  copyContent,
  onReset,
  children,
  hasResult,
}) {
  const [copied, setCopied] = useState(false);
  const toast = useToast();

  const handleCopy = () => {
    if (!copyContent) return;
    const textToCopy = typeof copyContent === "string" ? copyContent : JSON.stringify(copyContent, null, 2);
    navigator.clipboard.writeText(textToCopy);
    setCopied(true);
    toast.success("Result copied to clipboard!");
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="content-card ai-result-card">
      <div className="card-header" style={{ marginBottom: hasResult ? "16px" : "8px" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "10px", flexWrap: "wrap" }}>
          <h3 style={{ margin: 0 }}>{title}</h3>
          {hasResult && source && <AISourceBadge source={source} />}
        </div>

        {hasResult && (
          <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
            {copyContent && (
              <button
                type="button"
                className="secondary-button"
                style={{ padding: "6px 12px", fontSize: "13px" }}
                onClick={handleCopy}
                title="Copy result"
              >
                {copied ? <Check size={14} color="var(--success)" /> : <Copy size={14} />}
                <span>{copied ? "Copied!" : "Copy"}</span>
              </button>
            )}

            {onReset && (
              <button
                type="button"
                className="secondary-button"
                style={{ padding: "6px 10px", fontSize: "13px" }}
                onClick={onReset}
                title="Clear result"
              >
                Clear
              </button>
            )}
          </div>
        )}
      </div>

      {loading ? (
        <div className="empty-state-box">
          <RefreshCw size={30} className="spinning" color="var(--primary-600)" />
          <h4>{loadingText}</h4>
          <p style={{ fontSize: "13px", color: "var(--text-muted)" }}>
            Our Gemini AI model is synthesizing your parameters.
          </p>
        </div>
      ) : hasResult ? (
        <div className="ai-result-body animate-fade-in">{children}</div>
      ) : (
        <div className="empty-state-box">
          <div className="empty-state-icon">
            {EmptyIcon ? <EmptyIcon size={26} /> : <RefreshCw size={26} />}
          </div>
          <h4>{emptyTitle}</h4>
          <p>{emptyText}</p>
        </div>
      )}
    </div>
  );
}

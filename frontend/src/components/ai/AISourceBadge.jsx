import { Sparkles } from "lucide-react";

export default function AISourceBadge({ source }) {
  const isGemini = source === "GEMINI";
  return (
    <span
      className={`status-pill ${isGemini ? "offered" : "interview"}`}
      style={{
        fontSize: "0.75rem",
        padding: "3px 8px",
        display: "inline-flex",
        alignItems: "center",
        gap: "4px",
        fontWeight: "600",
      }}
      title={isGemini ? "Processed via live Google Gemini AI" : "Processed via intelligent fallback engine"}
    >
      <Sparkles size={12} />
      {isGemini ? "🟢 Gemini AI" : "🟡 Fallback Analysis"}
    </span>
  );
}

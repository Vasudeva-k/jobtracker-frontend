/**
 * Safely parse inline bold (**text**) and code (`code`) within a line of text.
 */
function parseInlineFormatting(text) {
  if (!text) return "";

  // Split by inline markdown tokens: **bold** or `code`
  const parts = [];
  const regex = /(\*\*[^*]+\*\*|`[^`]+`)/g;
  let lastIndex = 0;
  let match;

  while ((match = regex.exec(text)) !== null) {
    // Add text before match
    if (match.index > lastIndex) {
      parts.push(text.substring(lastIndex, match.index));
    }

    const token = match[0];
    if (token.startsWith("**") && token.endsWith("**")) {
      parts.push(
        <strong key={match.index} style={{ color: "var(--text-heading)", fontWeight: 700 }}>
          {token.slice(2, -2)}
        </strong>
      );
    } else if (token.startsWith("`") && token.endsWith("`")) {
      parts.push(
        <code
          key={match.index}
          style={{
            background: "var(--bg-surface-subtle)",
            padding: "2px 6px",
            borderRadius: "4px",
            fontSize: "0.85em",
            fontFamily: "monospace",
            color: "var(--primary-700)",
            border: "1px solid var(--border-subtle)",
          }}
        >
          {token.slice(1, -1)}
        </code>
      );
    }

    lastIndex = regex.lastIndex;
  }

  if (lastIndex < text.length) {
    parts.push(text.substring(lastIndex));
  }

  return parts.length > 0 ? parts : text;
}

export default function AIFormattedText({ content, className = "" }) {
  if (!content) return null;

  if (typeof content !== "string") {
    return <div className={className}>{JSON.stringify(content, null, 2)}</div>;
  }

  // Split into lines
  const lines = content.split("\n");
  const elements = [];
  let currentList = [];
  let listType = null; // 'ul' | 'ol'

  const flushList = () => {
    if (currentList.length > 0) {
      if (listType === "ul") {
        elements.push(
          <ul key={`ul-${elements.length}`} className="ai-formatted-list">
            {currentList.map((item, idx) => (
              <li key={idx}>{parseInlineFormatting(item)}</li>
            ))}
          </ul>
        );
      } else if (listType === "ol") {
        elements.push(
          <ol key={`ol-${elements.length}`} className="ai-formatted-numbered-list">
            {currentList.map((item, idx) => (
              <li key={idx}>{parseInlineFormatting(item)}</li>
            ))}
          </ol>
        );
      }
      currentList = [];
      listType = null;
    }
  };

  for (let i = 0; i < lines.length; i++) {
    const rawLine = lines[i];
    const trimmed = rawLine.trim();

    if (!trimmed) {
      flushList();
      continue;
    }

    // Check for H1, H2, H3
    if (trimmed.startsWith("### ")) {
      flushList();
      elements.push(
        <h4 key={`h3-${i}`} className="ai-formatted-h3">
          {parseInlineFormatting(trimmed.substring(4))}
        </h4>
      );
    } else if (trimmed.startsWith("## ")) {
      flushList();
      elements.push(
        <h3 key={`h2-${i}`} className="ai-formatted-h2">
          {parseInlineFormatting(trimmed.substring(3))}
        </h3>
      );
    } else if (trimmed.startsWith("# ")) {
      flushList();
      elements.push(
        <h2 key={`h1-${i}`} className="ai-formatted-h1">
          {parseInlineFormatting(trimmed.substring(2))}
        </h2>
      );
    }
    // Check for unordered list item
    else if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
      if (listType && listType !== "ul") flushList();
      listType = "ul";
      currentList.push(trimmed.substring(2));
    }
    // Check for ordered list item (e.g. 1. or 1))
    else if (/^\d+[.)]\s+/.test(trimmed)) {
      if (listType && listType !== "ol") flushList();
      listType = "ol";
      const itemText = trimmed.replace(/^\d+[.)]\s+/, "");
      currentList.push(itemText);
    }
    // Regular paragraph
    else {
      flushList();
      elements.push(
        <p key={`p-${i}`} className="ai-formatted-p">
          {parseInlineFormatting(trimmed)}
        </p>
      );
    }
  }

  flushList();

  return <div className={`ai-formatted-content ${className}`}>{elements}</div>;
}

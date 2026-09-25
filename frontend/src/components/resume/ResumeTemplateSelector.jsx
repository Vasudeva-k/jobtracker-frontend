export default function ResumeTemplateSelector({ selectedTemplate, onSelectTemplate }) {
  const templates = [
    {
      id: "modern",
      name: "Modern Clean",
      desc: "Indigo header banner, clear dividers, structured modern typography.",
    },
    {
      id: "classic",
      name: "Classic Elegance",
      desc: "Traditional centered layout, serif styling, standard ATS rules.",
    },
    {
      id: "minimal",
      name: "Minimalist Tech",
      desc: "Streamlined single-column format, high information density.",
    },
  ];

  return (
    <div className="template-selector-grid" role="radiogroup" aria-label="Resume Template Selection">
      {templates.map((tpl) => {
        const isSelected = selectedTemplate === tpl.id;
        return (
          <button
            key={tpl.id}
            type="button"
            role="radio"
            aria-checked={isSelected}
            className={`template-card-btn ${isSelected ? "selected" : ""}`}
            onClick={() => onSelectTemplate(tpl.id)}
          >
            <div className="template-card-name">{tpl.name}</div>
            <div className="template-card-desc">{tpl.desc}</div>
          </button>
        );
      })}
    </div>
  );
}

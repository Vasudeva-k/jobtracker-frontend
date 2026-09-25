import { useToast } from "../context/ToastContext";
import { CheckCircle2, AlertCircle, AlertTriangle, Info, X } from "lucide-react";
import "./Toast.css";

const ICONS = {
  success: CheckCircle2,
  error: AlertCircle,
  warning: AlertTriangle,
  info: Info,
};

export default function ToastContainer() {
  const { toasts, removeToast } = useToast();

  if (!toasts || toasts.length === 0) return null;

  return (
    <div className="toast-portal" aria-live="polite" aria-atomic="true">
      {toasts.map((item) => {
        const IconComponent = ICONS[item.type] || Info;
        return (
          <div
            key={item.id}
            className={`toast-item toast-${item.type}`}
            role="status"
          >
            <div className="toast-icon-wrap">
              <IconComponent size={18} className="toast-icon" />
            </div>
            <div className="toast-message">{item.message}</div>
            <button
              className="toast-close-btn"
              onClick={() => removeToast(item.id)}
              aria-label="Close notification"
            >
              <X size={15} />
            </button>
            {item.duration > 0 && (
              <div
                className="toast-progress-bar"
                style={{ animationDuration: `${item.duration}ms` }}
              />
            )}
          </div>
        );
      })}
    </div>
  );
}

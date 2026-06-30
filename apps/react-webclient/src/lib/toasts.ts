import { toast } from "sonner";
import * as React from "react";
import { type ReactNode } from "react";
import "sonner/dist/styles.css";

const successStyle = {
  // Sonner uses these CSS variables for styling
  "--toast-background":
    "color-mix(in oklab, light-dark(var(--color-green-600), var(--color-green-400)) 10%, var(--background))",
  "--toast-color": "light-dark(var(--color-green-600), var(--color-green-400))",
  "--toast-border":
    "light-dark(var(--color-green-600), var(--color-green-400))",
  // Or use direct styles if variables don't work
  background:
    "color-mix(in oklab, light-dark(var(--color-green-600), var(--color-green-400)) 10%, var(--background))",
  color: "light-dark(var(--color-green-600), var(--color-green-400))",
  border:
    "1px solid light-dark(var(--color-green-600), var(--color-green-400))",
} as React.CSSProperties;

export function successToast(message: string, description?: string) {
  toast.success(message, {
    description,
    style: successStyle,
  });
}

const failureStyle = {
  "--normal-bg":
    "color-mix(in oklab, var(--destructive) 10%, var(--background))",
  "--normal-text": "var(--destructive)",
  "--normal-border": "var(--destructive)",
} as React.CSSProperties;

export function failureToast(
  message: string,
  description?: string,
  action?: ReactNode,
) {
  toast.error(message, {
    description,
    style: failureStyle,
    action: action,
  });
}

export function failureToastAction(message: string, action?: ReactNode) {
  toast.error(message, {
    style: failureStyle,
    action: action,
  });
}

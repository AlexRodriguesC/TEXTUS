import { toast } from "sonner";
import { type ReactNode } from "react";

export function successToast(message: string, description?: string) {
  toast.success(message, {
    description,
  });
}

export function failureToast(
  message: string,
  description?: string,
  action?: ReactNode,
) {
  toast.error(message, {
    description,
    action: action,
  });
}

export function failureToastAction(message: string, action?: ReactNode) {
  toast.error(message, {
    action: action,
  });
}

import type { LucideIcon } from "lucide-react";

export type TaskVariant = "default" | "success" | "error";

export interface Task {
  id: string;
  label: string;
  icon: LucideIcon;
  progress: number;
  maxProgress: number;
  variant?: TaskVariant;
}

export interface TaskProgressProps {
  task: Task;
  variant?: TaskVariant;
}

export interface ResourceProcessingStatusProps {
  tasks: Task[];
  variant?: TaskVariant;
}

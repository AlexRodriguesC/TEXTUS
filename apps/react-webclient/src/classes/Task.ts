import type { TaskTypeType } from "@/classes/TaskType.ts";
import type { TaskVariantType } from "@/classes/TaskVariant.ts";

class Task {
  id: string;
  type: TaskTypeType;
  progress: number;
  maxProgress: number;
  variant: TaskVariantType;

  constructor(
    id: string,
    type: TaskTypeType,
    progress: number,
    maxProgress: number,
    variant: TaskVariantType,
  ) {
    this.id = id;
    this.type = type;
    this.progress = progress;
    this.maxProgress = maxProgress;
    this.variant = variant;
  }
}

export default Task;

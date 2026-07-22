import type { TaskDto } from "@/client/model";
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

  static from(dto: TaskDto): Task {
    return new Task(
      dto.id,
      dto.type as TaskTypeType,
      dto.progress,
      dto.maxProgress,
      dto.variant as TaskVariantType,
    );
  }
}

export default Task;

export type TaskTypeType = (typeof TaskType)[keyof typeof TaskType];

const TaskType = {
  OCR: "OCR",
  SEGMENTATION: "SEGMENTATION",
  SUMMARIZATION: "SUMMARIZATION",
  TOPIC: "TOPIC",
} as const;

export default TaskType;

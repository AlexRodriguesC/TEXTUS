export type LearningPathJobStatusType =
  (typeof LearningPathJobStatus)[keyof typeof LearningPathJobStatus];

const LearningPathJobStatus = {
  NOT_CREATED: "NOT_CREATED",
  GENERATING: "GENERATING",
  DONE: "DONE",
  FAILED: "FAILED",
} as const;

export default LearningPathJobStatus;

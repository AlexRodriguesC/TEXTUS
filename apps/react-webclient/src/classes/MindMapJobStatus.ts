export type MindMapJobStatusType =
  (typeof MindMapJobStatus)[keyof typeof MindMapJobStatus];

const MindMapJobStatus = {
  NOT_CREATED: "NOT_CREATED",
  GENERATING: "GENERATING",
  DONE: "DONE",
  FAILED: "FAILED",
} as const;

export default MindMapJobStatus;

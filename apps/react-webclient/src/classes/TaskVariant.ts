export type TaskVariantType = (typeof TaskVariant)[keyof typeof TaskVariant];

const TaskVariant = {
  DEFAULT: "DEFAULT",
  SUCCESS: "SUCCESS",
  ERROR: "ERROR",
} as const;

export default TaskVariant;

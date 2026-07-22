export type ExerciseTypeType = (typeof ExerciseType)[keyof typeof ExerciseType];

const ExerciseType = {
  LIST: "LIST",
  GROUP: "GROUP",
  QUESTION: "QUESTION",
  PAIR: "PAIR",
} as const;

export default ExerciseType;

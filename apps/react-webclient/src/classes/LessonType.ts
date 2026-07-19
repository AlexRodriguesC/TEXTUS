
export type LessonTypeType = typeof LessonType[keyof typeof LessonType];


const LessonType = {
    LESSON: 0,
    CHECKPOINT: 1,
} as const;

export default LessonType;

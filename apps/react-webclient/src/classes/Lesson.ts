import LessonType, { type LessonTypeType } from "@/classes/LessonType.ts";

class Lesson {
  id: number;
  attemptsLeft: number;
  chapterId: number;
  completed: boolean;
  currentTopic: number;
  index: number;
  mastery: number;
  maxAttempts: number;
  topics: number[];
  type: LessonTypeType;

  constructor(
    attemptsLeft: number,
    chapterId: number,
    completed: boolean,
    currentTopic: number,
    id: number,
    index: number,
    mastery: number,
    maxAttempts: number,
    topics: number[],
    type: LessonTypeType,
  ) {
    this.attemptsLeft = attemptsLeft;
    this.chapterId = chapterId;
    this.completed = completed;
    this.currentTopic = currentTopic;
    this.id = id;
    this.index = index;
    this.mastery = mastery;
    this.maxAttempts = maxAttempts;
    this.topics = topics;
    this.type = type;
  }

  isLesson(): boolean {
    return this.type === LessonType.LESSON;
  }

  isCheckpoint(): boolean {
    return this.type === LessonType.CHECKPOINT;
  }

  isMastered(): boolean {
    return this.completed;
  }

  shouldFetchNextExercise(): boolean {
    if (this.completed) return false;
    if (this.isCheckpoint()) return this.topics.length > 0;
    return true;
  }
}

export default Lesson;

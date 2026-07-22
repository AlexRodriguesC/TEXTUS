import type { ExerciseTypeType } from "@/classes/ExerciseType.ts";

class Question {
  id: number;
  topicId: number;
  request: string;
  completed: boolean;
  type: ExerciseTypeType;
  answer: string;
  distractors: string[];

  constructor(
    id: number,
    topicId: number,
    request: string,
    completed: boolean,
    type: ExerciseTypeType,
    answer: string,
    distractors: string[],
  ) {
    this.id = id;
    this.topicId = topicId;
    this.request = request;
    this.completed = completed;
    this.type = type;
    this.answer = answer;
    this.distractors = distractors;
  }
}

export default Question;

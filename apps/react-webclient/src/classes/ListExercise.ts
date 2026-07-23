import ExerciseType from "@/classes/ExerciseType.ts";

class ListExercise {
  id: number;
  topicId: number;
  request: string;
  completed: boolean;
  type: typeof ExerciseType.LIST;
  answer: string[];

  constructor(
    id: number,
    topicId: number,
    request: string,
    completed: boolean,
    type: typeof ExerciseType.LIST,
    answer: string[],
  ) {
    this.id = id;
    this.topicId = topicId;
    this.request = request;
    this.completed = completed;
    this.type = type;
    this.answer = answer;
  }
}

export default ListExercise;

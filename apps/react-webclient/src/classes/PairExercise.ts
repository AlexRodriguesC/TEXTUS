import ExerciseType from "@/classes/ExerciseType.ts";
import type LeftRight from "@/classes/LeftRight.ts";

class PairExercise {
  id: number;
  topicId: number;
  request: string;
  completed: boolean;
  type: typeof ExerciseType.PAIR;
  pairs: LeftRight[];

  constructor(
    id: number,
    topicId: number,
    request: string,
    completed: boolean,
    type: typeof ExerciseType.PAIR,
    pairs: LeftRight[],
  ) {
    this.id = id;
    this.topicId = topicId;
    this.request = request;
    this.completed = completed;
    this.type = type;
    this.pairs = pairs;
  }
}

export default PairExercise;

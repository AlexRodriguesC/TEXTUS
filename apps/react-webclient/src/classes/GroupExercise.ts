import ExerciseType from "@/classes/ExerciseType.ts";

class GroupExercise {
  id: number;
  topicId: number;
  request: string;
  completed: boolean;
  type: typeof ExerciseType.GROUP;
  groups: Record<string, string[]>;

  constructor(
    id: number,
    topicId: number,
    request: string,
    completed: boolean,
    type: typeof ExerciseType.GROUP,
    groups: Record<string, string[]>,
  ) {
    this.id = id;
    this.topicId = topicId;
    this.request = request;
    this.completed = completed;
    this.type = type;
    this.groups = groups;
  }
}

export default GroupExercise;

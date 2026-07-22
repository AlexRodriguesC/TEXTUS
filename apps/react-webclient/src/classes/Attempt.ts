import type Lesson from "@/classes/Lesson.ts";

class Attempt {
  exerciseId: number;
  correct: boolean;
  lesson: Lesson;

  constructor(exerciseId: number, correct: boolean, lesson: Lesson) {
    this.exerciseId = exerciseId;
    this.correct = correct;
    this.lesson = lesson;
  }
}

export default Attempt;

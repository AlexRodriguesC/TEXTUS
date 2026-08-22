import type { Exercise } from "@/classes/Exercise.ts";
import ExerciseType from "@/classes/ExerciseType.ts";
import type LeftRight from "@/classes/LeftRight.ts";

function isStringArray(value: unknown): value is string[] {
  return (
    Array.isArray(value) && value.every((item) => typeof item === "string")
  );
}

function isGroupAnswer(value: unknown): value is Record<string, string[]> {
  if (typeof value !== "object" || value === null || Array.isArray(value)) {
    return false;
  }
  return Object.values(value).every(isStringArray);
}

function isPairList(value: unknown): value is LeftRight[] {
  return (
    Array.isArray(value) &&
    value.every(
      (item) =>
        typeof item === "object" &&
        item !== null &&
        "left" in item &&
        "right" in item &&
        typeof (item as LeftRight).left === "string" &&
        typeof (item as LeftRight).right === "string",
    )
  );
}

function sameSortedStrings(expected: string[], actual: string[]): boolean {
  if (expected.length !== actual.length) return false;
  const sortedExpected = [...expected].sort();
  const sortedActual = [...actual].sort();
  return sortedExpected.every((value, index) => value === sortedActual[index]);
}

function sameSortedPairs(
  expected: LeftRight[],
  actual: LeftRight[],
): boolean {
  if (expected.length !== actual.length) return false;
  const compare = (a: LeftRight, b: LeftRight) => {
    const leftCmp = a.left.localeCompare(b.left);
    return leftCmp !== 0 ? leftCmp : a.right.localeCompare(b.right);
  };
  const sortedExpected = [...expected].sort(compare);
  const sortedActual = [...actual].sort(compare);
  return sortedExpected.every(
    (pair, index) =>
      pair.left === sortedActual[index].left &&
      pair.right === sortedActual[index].right,
  );
}

function evaluateQuestionAnswer(
  correctAnswer: string,
  userResponse: unknown,
): boolean {
  if (typeof userResponse !== "string") return false;
  return correctAnswer === userResponse;
}

function evaluateListAnswer(
  correctAnswer: string[],
  userResponse: unknown,
): boolean {
  if (!isStringArray(userResponse)) return false;
  if (correctAnswer.length !== userResponse.length) return false;
  return correctAnswer.every((value, index) => value === userResponse[index]);
}

function evaluateGroupAnswer(
  correctGroups: Record<string, string[]>,
  userResponse: unknown,
): boolean {
  if (!isGroupAnswer(userResponse)) return false;

  const expectedKeys = Object.keys(correctGroups);
  const actualKeys = Object.keys(userResponse);
  if (expectedKeys.length !== actualKeys.length) return false;
  if (!expectedKeys.every((key) => key in userResponse)) return false;

  return expectedKeys.every((key) =>
    sameSortedStrings(correctGroups[key] ?? [], userResponse[key] ?? []),
  );
}

function evaluatePairAnswer(
  correctPairs: LeftRight[],
  userResponse: unknown,
): boolean {
  if (!isPairList(userResponse)) return false;
  return sameSortedPairs(correctPairs, userResponse);
}

/**
 * Client-side grading mirroring backend `isAnswerCorrect` for immediate UI feedback.
 * Backend remains the source of truth for scoring and mastery.
 */
export function evaluateExerciseAnswer(
  exercise: Exercise,
  userResponse: unknown,
): boolean {
  if (userResponse == null) return false;

  switch (exercise.type) {
    case ExerciseType.QUESTION:
      return evaluateQuestionAnswer(exercise.answer, userResponse);
    case ExerciseType.LIST:
      return evaluateListAnswer(exercise.answer, userResponse);
    case ExerciseType.GROUP:
      return evaluateGroupAnswer(exercise.groups, userResponse);
    case ExerciseType.PAIR:
      return evaluatePairAnswer(exercise.pairs, userResponse);
    default:
      return false;
  }
}

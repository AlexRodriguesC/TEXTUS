import type { Exercise } from "@/classes/Exercise.ts";
import type LeftRight from "@/classes/LeftRight.ts";
import ExerciseType from "@/classes/ExerciseType.ts";
import type { TFunction } from "i18next";

export function buildExerciseHelpMessage(
  t: TFunction,
  exercise: Exercise,
  userAnswer: unknown,
): string {
  const request = exercise.request;

  if (userAnswer === null || userAnswer === undefined) {
    return t("exercise.help.message.skipped", {
      request,
      options: formatOptions(exercise),
      solution: formatCorrectSolution(exercise),
    });
  }

  switch (exercise.type) {
    case ExerciseType.LIST:
      return t("exercise.help.message.list", {
        request,
        sequence: formatListSequence(userAnswer),
      });
    case ExerciseType.GROUP:
      return t("exercise.help.message.group", {
        request,
        assignments: formatMisplacedGroupAssignments(exercise.groups, userAnswer),
      });
    case ExerciseType.PAIR:
      return t("exercise.help.message.pair", {
        request,
        pairs: formatIncorrectPairs(exercise.pairs, userAnswer),
      });
    case ExerciseType.QUESTION:
      return t("exercise.help.message.question", {
        request,
        answer: typeof userAnswer === "string" ? userAnswer : String(userAnswer),
      });
  }
}

function formatOptions(exercise: Exercise): string {
  switch (exercise.type) {
    case ExerciseType.QUESTION:
      return [exercise.answer, ...exercise.distractors].join("\n");
    case ExerciseType.LIST:
      return exercise.answer.join("\n");
    case ExerciseType.GROUP: {
      const labels = Object.keys(exercise.groups);
      const items = Object.values(exercise.groups).flat();
      return [`Labels: ${labels.join(", ")}`, `Items: ${items.join(", ")}`].join(
        "\n",
      );
    }
    case ExerciseType.PAIR: {
      const lefts = exercise.pairs.map((pair) => pair.left);
      const rights = exercise.pairs.map((pair) => pair.right);
      return [`Left: ${lefts.join(", ")}`, `Right: ${rights.join(", ")}`].join(
        "\n",
      );
    }
  }
}

function formatCorrectSolution(exercise: Exercise): string {
  switch (exercise.type) {
    case ExerciseType.QUESTION:
      return exercise.answer;
    case ExerciseType.LIST:
      return exercise.answer.join(" → ");
    case ExerciseType.GROUP:
      return Object.entries(exercise.groups)
        .map(([label, keys]) => `${label}: ${keys.join(", ")}`)
        .join("\n");
    case ExerciseType.PAIR:
      return exercise.pairs
        .map((pair) => `${pair.left} → ${pair.right}`)
        .join("\n");
  }
}

function formatListSequence(userAnswer: unknown): string {
  if (Array.isArray(userAnswer)) {
    return userAnswer.map(String).join(" → ");
  }
  return String(userAnswer);
}

function formatMisplacedGroupAssignments(
  correctGroups: Record<string, string[]>,
  userAnswer: unknown,
): string {
  if (!isStringArrayRecord(userAnswer)) {
    return formatFallbackGroupAnswer(userAnswer);
  }

  const misplaced = Object.entries(userAnswer)
    .map(([label, items]) => {
      const correctItems = correctGroups[label] ?? [];
      const wrongItems = items.filter((item) => !correctItems.includes(item));
      return wrongItems.length > 0 ? `${label}: ${wrongItems.join(", ")}` : null;
    })
    .filter((line): line is string => line !== null);

  if (misplaced.length === 0) {
    return formatFallbackGroupAnswer(userAnswer);
  }

  return misplaced.join("\n");
}

function formatFallbackGroupAnswer(userAnswer: unknown): string {
  if (isStringArrayRecord(userAnswer)) {
    return Object.entries(userAnswer)
      .map(([label, keys]) => `${label}: ${keys.join(", ")}`)
      .join("\n");
  }
  return String(userAnswer);
}

function formatIncorrectPairs(
  correctPairs: LeftRight[],
  userAnswer: unknown,
): string {
  if (!Array.isArray(userAnswer) || !userAnswer.every(isLeftRight)) {
    return formatFallbackPairs(userAnswer);
  }

  const correctRightByLeft = new Map(
    correctPairs.map((pair) => [pair.left, pair.right]),
  );

  const incorrect = userAnswer.filter(
    (pair) => correctRightByLeft.get(pair.left) !== pair.right,
  );

  if (incorrect.length === 0) {
    return formatFallbackPairs(userAnswer);
  }

  return incorrect.map((pair) => `${pair.left} → ${pair.right}`).join("\n");
}

function formatFallbackPairs(userAnswer: unknown): string {
  if (Array.isArray(userAnswer) && userAnswer.every(isLeftRight)) {
    return userAnswer.map((pair) => `${pair.left} → ${pair.right}`).join("\n");
  }
  return String(userAnswer);
}

function isStringArrayRecord(
  value: unknown,
): value is Record<string, string[]> {
  if (typeof value !== "object" || value === null || Array.isArray(value)) {
    return false;
  }
  return Object.values(value).every(
    (entry) =>
      Array.isArray(entry) && entry.every((item) => typeof item === "string"),
  );
}

function isLeftRight(value: unknown): value is LeftRight {
  return (
    typeof value === "object" &&
    value !== null &&
    "left" in value &&
    "right" in value
  );
}

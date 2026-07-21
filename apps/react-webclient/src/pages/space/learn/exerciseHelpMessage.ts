import type {
  GroupResponseDTO,
  LeftRight,
  ListResponseDTO,
  PairResponseDTO,
  QuestionResponseDTO,
  TopicResponseDTO,
} from "@/client/model";
import type { TFunction } from "i18next";

type Exercise =
  | QuestionResponseDTO
  | ListResponseDTO
  | GroupResponseDTO
  | PairResponseDTO;

export function buildExerciseHelpMessage(
  t: TFunction,
  exercise: Exercise,
  userAnswer: unknown,
  topic: TopicResponseDTO | undefined,
): string {
  const topicSection = topic
    ? `${topic.title}\n${topic.summary}\n${topic.labels.join(", ")}`
    : t("exercise.help.unknownTopic");

  return t("exercise.help.message", {
    exerciseType: exercise.type,
    topic: topicSection,
    prompt: exercise.request,
    correctSolution: formatCorrectSolution(exercise),
    userResponse: formatUserResponse(userAnswer, t),
  });
}

function formatCorrectSolution(exercise: Exercise): string {
  switch (exercise.type) {
    case "QUESTION":
      return exercise.answer;
    case "LIST":
      return exercise.answer.join(" → ");
    case "GROUP":
      return Object.entries(exercise.groups)
        .map(([label, keys]) => `${label}: ${keys.join(", ")}`)
        .join("\n");
    case "PAIR":
      return exercise.pairs.map((pair) => `${pair.left} → ${pair.right}`).join("\n");
  }
}

function formatUserResponse(userAnswer: unknown, t: TFunction): string {
  if (userAnswer === null || userAnswer === undefined) {
    return t("exercise.help.skipped");
  }

  if (typeof userAnswer === "string") {
    return userAnswer;
  }

  if (Array.isArray(userAnswer)) {
    if (userAnswer.length > 0 && isLeftRight(userAnswer[0])) {
      return userAnswer.map((pair) => `${pair.left} → ${pair.right}`).join("\n");
    }
    return userAnswer.join(" → ");
  }

  if (typeof userAnswer === "object") {
    return Object.entries(userAnswer as Record<string, string[]>)
      .map(([label, keys]) => `${label}: ${keys.join(", ")}`)
      .join("\n");
  }

  return String(userAnswer);
}

function isLeftRight(value: unknown): value is LeftRight {
  return (
    typeof value === "object" &&
    value !== null &&
    "left" in value &&
    "right" in value
  );
}

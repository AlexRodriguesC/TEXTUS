import type Topic from "@/classes/Topic.ts";
import type { TFunction } from "i18next";

const SUGGESTION_KEYS = [
  "chat.empty.suggestions.askAbout",
  "chat.empty.suggestions.tellMeMore",
  "chat.empty.suggestions.helpUnderstand",
  "chat.empty.suggestions.explore",
] as const;

const MAX_SUGGESTIONS = 4;

export type TopicSuggestion = {
  message: string;
  topicIds: number[];
};

export function pickSuggestedTopics({topics, maxSuggestions = MAX_SUGGESTIONS} : {topics: Topic[], maxSuggestions?: number}) {
  const shuffled = [...topics];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled.slice(0, Math.min(maxSuggestions, shuffled.length));
}

export function buildTopicSuggestion(
  t: TFunction,
  topic: Topic,
  index: number,
): TopicSuggestion {
  const key = SUGGESTION_KEYS[index % SUGGESTION_KEYS.length];
  return {
    message: t(key, { title: topic.title }),
    topicIds: [topic.id],
  };
}

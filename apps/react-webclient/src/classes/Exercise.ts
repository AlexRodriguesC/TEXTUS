import type Topic from "@/classes/Topic.ts";
import type Page from "@/classes/Page.ts";
import type Node from "@/classes/Node.ts";
import type PathSettings from "@/classes/PathSettings.ts";
import type Chapter from "@/classes/Chapter.ts";
import type Question from "@/classes/Question.ts";
import type ListExercise from "@/classes/ListExercise.ts";
import type GroupExercise from "@/classes/GroupExercise.ts";
import type PairExercise from "@/classes/PairExercise.ts";
import type Chat from "@/classes/Chat.ts";
import type ChatMessage from "@/classes/ChatMessage.ts";
import type Space from "@/classes/Space.ts";
import type Attempt from "@/classes/Attempt.ts";
import type Document from "@/classes/Document.ts";
import type Task from "@/classes/Task.ts";
import type Lesson from "@/classes/Lesson.ts";
import type LearningPath from "@/classes/LearningPath.ts";
import type MindMap from "@/classes/MindMap.ts";
import type LeftRight from "@/classes/LeftRight.ts";

export type Exercise =
  | Question
  | ListExercise
  | GroupExercise
  | PairExercise;

export type {
  Topic,
  Page,
  Node,
  PathSettings,
  Chapter,
  Question,
  ListExercise,
  GroupExercise,
  PairExercise,
  Chat,
  ChatMessage,
  Space,
  Attempt,
  Document,
  Task,
  Lesson,
  LearningPath,
  MindMap,
  LeftRight,
};

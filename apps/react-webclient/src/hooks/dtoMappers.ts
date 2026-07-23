import Attempt from "@/classes/Attempt.ts";
import Chapter from "@/classes/Chapter.ts";
import Chat from "@/classes/Chat.ts";
import ChatMessage from "@/classes/ChatMessage.ts";
import Document from "@/classes/Document.ts";
import ExerciseType from "@/classes/ExerciseType.ts";
import GroupExercise from "@/classes/GroupExercise.ts";
import LearningPath from "@/classes/LearningPath.ts";
import LeftRight from "@/classes/LeftRight.ts";
import Lesson from "@/classes/Lesson.ts";
import LessonType from "@/classes/LessonType.ts";
import ListExercise from "@/classes/ListExercise.ts";
import MindMap from "@/classes/MindMap.ts";
import Node from "@/classes/Node.ts";
import Page from "@/classes/Page.ts";
import PairExercise from "@/classes/PairExercise.ts";
import PathSettings from "@/classes/PathSettings.ts";
import Question from "@/classes/Question.ts";
import Space from "@/classes/Space.ts";
import Task from "@/classes/Task.ts";
import Topic from "@/classes/Topic.ts";
import type { Exercise } from "@/classes/Exercise.ts";
import type { ChatMessageRole } from "@/classes/ChatMessage.ts";
import type { ExerciseTypeType } from "@/classes/ExerciseType.ts";
import type { TaskTypeType } from "@/classes/TaskType.ts";
import type { TaskVariantType } from "@/classes/TaskVariant.ts";
import type {
  AttemptResponseDTO,
  ChapterResponseDTO,
  ChatMessageResponseDTO,
  ChatResponseDTO,
  DocumentResponseDTO,
  GroupResponseDTO,
  LearningPathResponseDTO,
  LessonResponseDTO,
  ListResponseDTO,
  MindMapResponseDTO,
  NodeDTO,
  PageResponseDTO,
  PairResponseDTO,
  PathSettingsResponseDTO,
  QuestionResponseDTO,
  ReadSpaceResponseDTO,
  TaskDto,
  TopicResponseDTO,
} from "@/client/model";
import { LessonResponseDTOType } from "@/client/model/lessonResponseDTOType.ts";

export function mapTask(dto: TaskDto): Task {
  return new Task(
    dto.id,
    dto.type as TaskTypeType,
    dto.progress,
    dto.maxProgress,
    dto.variant as TaskVariantType,
  );
}

export function mapDocument(dto: DocumentResponseDTO): Document {
  return new Document(dto.name, dto.id, dto.tasks.map(mapTask));
}

export function mapLesson(dto: LessonResponseDTO): Lesson {
  if (
    dto.type !== LessonResponseDTOType.LESSON &&
    dto.type !== LessonResponseDTOType.CHECKPOINT
  ) {
    throw new Error(
      "Cannot create Lesson from dto of type " +
        dto.type +
        " reason: Lesson type is not supported",
    );
  }

  return new Lesson(
    dto.attemptsLeft,
    dto.chapterId,
    dto.completed,
    dto.currentTopic,
    dto.id,
    dto.index,
    dto.mastery,
    dto.maxAttempts,
    dto.topics ?? [],
    dto.type === LessonResponseDTOType.LESSON
      ? LessonType.LESSON
      : LessonType.CHECKPOINT,
  );
}

export function mapPathSettings(dto: PathSettingsResponseDTO): PathSettings {
  return new PathSettings(
    dto.questionPercentage,
    dto.listPercentage,
    dto.pairPercentage,
    dto.groupPercentage,
  );
}

export function mapChapter(dto: ChapterResponseDTO): Chapter {
  return new Chapter(
    dto.index,
    dto.title,
    dto.description,
    dto.lessons.map(mapLesson),
  );
}

export function mapLearningPath(dto: LearningPathResponseDTO): LearningPath {
  return new LearningPath(
    dto.chapters.map(mapChapter),
    mapPathSettings(dto.settings),
  );
}

export function isLearningPathDto(
  data: { chapters?: unknown; settings?: unknown; status?: unknown },
): data is LearningPathResponseDTO {
  return "chapters" in data && "settings" in data && "status" in data;
}

export function mapNode(dto: NodeDTO): Node {
  return new Node(
    dto.nodeType,
    dto.entityId,
    dto.parentNodeType,
    dto.parentEntityId,
  );
}

export function mapMindMap(dto: MindMapResponseDTO): MindMap {
  return new MindMap((dto.nodes ?? []).map(mapNode));
}

export function isMindMapDto(data: { nodes?: unknown }): boolean {
  return "nodes" in data;
}

export function mapTopic(dto: TopicResponseDTO): Topic {
  return new Topic(
    dto.id,
    dto.sourceFileName,
    dto.page,
    dto.title,
    dto.summary,
    dto.labels,
  );
}

export function mapPage(dto: PageResponseDTO): Page {
  return new Page(dto.id, dto.documentId, dto.title, dto.summary);
}

export function mapQuestion(dto: QuestionResponseDTO): Question {
  return new Question(
    dto.id,
    dto.topicId,
    dto.request,
    dto.completed ?? false,
    ExerciseType.QUESTION,
    dto.answer,
    dto.distractors,
  );
}

export function mapListExercise(dto: ListResponseDTO): ListExercise {
  return new ListExercise(
    dto.id,
    dto.topicId,
    dto.request,
    dto.completed ?? false,
    ExerciseType.LIST,
    dto.answer,
  );
}

export function mapGroupExercise(dto: GroupResponseDTO): GroupExercise {
  return new GroupExercise(
    dto.id,
    dto.topicId,
    dto.request,
    dto.completed ?? false,
    ExerciseType.GROUP,
    dto.groups,
  );
}

export function mapPairExercise(dto: PairResponseDTO): PairExercise {
  return new PairExercise(
    dto.id,
    dto.topicId,
    dto.request,
    dto.completed ?? false,
    ExerciseType.PAIR,
    dto.pairs.map((pair) => new LeftRight(pair.left, pair.right)),
  );
}

export function mapExercise(
  dto: QuestionResponseDTO | ListResponseDTO | GroupResponseDTO | PairResponseDTO,
): Exercise {
  switch (dto.type) {
    case ExerciseType.QUESTION:
      return mapQuestion(dto as QuestionResponseDTO);
    case ExerciseType.LIST:
      return mapListExercise(dto as ListResponseDTO);
    case ExerciseType.GROUP:
      return mapGroupExercise(dto as GroupResponseDTO);
    case ExerciseType.PAIR:
      return mapPairExercise(dto as PairResponseDTO);
    default:
      throw new Error("Unknown exercise type");
  }
}

export function isExerciseDto(
  data: unknown,
): data is
  | QuestionResponseDTO
  | ListResponseDTO
  | GroupResponseDTO
  | PairResponseDTO {
  return (
    typeof data === "object" &&
    data !== null &&
    "type" in data &&
    "id" in data &&
    "topicId" in data
  );
}

export function mapAttempt(dto: AttemptResponseDTO): Attempt {
  return new Attempt(
    dto.exerciseId ?? 0,
    dto.correct ?? false,
    dto.lesson ? mapLesson(dto.lesson) : new Lesson(0, 0, false, 0, 0, 0, 0, 0, [], LessonType.LESSON),
  );
}

export function mapChat(dto: ChatResponseDTO): Chat {
  return new Chat(dto.id, dto.title, dto.createdAt, dto.updatedAt);
}

export function mapChatMessage(dto: ChatMessageResponseDTO): ChatMessage {
  return new ChatMessage(
    dto.role as ChatMessageRole,
    dto.content,
    dto.timestamp,
  );
}

export function mapChatMessageFromCustom(dto: {
  role: ChatMessageRole;
  content: string;
  timestamp: string;
}): ChatMessage {
  return new ChatMessage(dto.role, dto.content, dto.timestamp);
}

export function mapSpace(dto: ReadSpaceResponseDTO): Space {
  return new Space(dto.spaceId, dto.icon, dto.name, dto.description);
}

export function toPathSettingsDto(
  settings: PathSettings,
): PathSettingsResponseDTO {
  return {
    questionPercentage: settings.questionPercentage,
    listPercentage: settings.listPercentage,
    pairPercentage: settings.pairPercentage,
    groupPercentage: settings.groupPercentage,
  };
}

export function toExerciseTypeDto(type: ExerciseTypeType) {
  return type;
}

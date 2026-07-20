// classes/Lesson.ts
import { type LessonResponseDTO, LessonResponseDTOType, type ErrorResponseDTO } from "@/client/model";
import LessonType, { type LessonTypeType } from "@/classes/LessonType.ts";
import { useDeleteExercises } from "@/client/generated/lessons/lessons.ts";

class Lesson {
    id: number;
    attemptsLeft: number;
    chapterId: number;
    completed: boolean;
    currentTopic: number;
    index: number;
    mastery: number;
    maxAttempts: number;
    topics: number[];
    type: LessonTypeType;

    private spaceId?: number;
    private deleteExercisesMutation?: ReturnType<typeof useDeleteExercises>["mutateAsync"];
    isDeleting: boolean = false;

    constructor(
        attemptsLeft: number,
        chapterId: number,
        completed: boolean,
        currentTopic: number,
        id: number,
        index: number,
        mastery: number,
        maxAttempts: number,
        topics: number[],
        type: LessonTypeType,
    ) {
        this.attemptsLeft = attemptsLeft;
        this.chapterId = chapterId;
        this.completed = completed;
        this.currentTopic = currentTopic;
        this.id = id;
        this.index = index;
        this.mastery = mastery;
        this.maxAttempts = maxAttempts;
        this.topics = topics;
        this.type = type;
    }

    static from(dto: LessonResponseDTO): Lesson {
        if (dto.type !== LessonResponseDTOType.LESSON && dto.type !== LessonResponseDTOType.CHECKPOINT) {
            throw new Error(
                "Cannot create Lesson from dto of type " + dto.type + " reason: Lesson type is not supported",
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
            dto.type === LessonResponseDTOType.LESSON ? LessonType.LESSON : LessonType.CHECKPOINT,
        );
    }

    withRuntime(
        spaceId: number,
        deleteExercisesMutation: ReturnType<typeof useDeleteExercises>["mutateAsync"],
        isDeleting: boolean = false,
    ): this {
        this.spaceId = spaceId;
        this.deleteExercisesMutation = deleteExercisesMutation;
        this.isDeleting = isDeleting;
        return this;
    }

    deleteExercises() {
        if (!this.deleteExercisesMutation || this.spaceId === undefined) {
            throw new Error("Lesson.deleteExercises called without runtime dependencies (call withRuntime first)");
        }
        return this.deleteExercisesMutation({ spaceId: this.spaceId, lessonId: this.id });
    }

    isLesson(): boolean {
        return this.type === LessonType.LESSON;
    }

    isCheckpoint(): boolean {
        return this.type === LessonType.CHECKPOINT;
    }

    isMastered(): boolean {
        return this.completed;
    }

    shouldFetchNextExercise(): boolean {
        if (this.completed) return false;
        if (this.isCheckpoint()) return this.topics.length > 0;
        return true;
    }

    static isLessonResponse(
        resp: ErrorResponseDTO | LessonResponseDTO | { status: number },
    ): resp is { status: 200; data: LessonResponseDTO } {
        return "status" in resp && resp.status === 200;
    }
}

export default Lesson;
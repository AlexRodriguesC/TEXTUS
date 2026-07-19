import {type LessonResponseDTO, LessonResponseDTOType} from "@/client/model";
import LessonType, {type LessonTypeType} from "@/classes/LessonType.ts";

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


    constructor(attemptsLeft: number, chapterId: number, completed: boolean, currentTopic: number, id: number, index: number, mastery: number, maxAttempts: number, topics: number[], type: LessonType) {
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
                "Cannot create Lesson from dto of type " + dto.type + " reason: Lesson type is not supported"
            )
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
            dto.type
        )
    }
}

export default Lesson;
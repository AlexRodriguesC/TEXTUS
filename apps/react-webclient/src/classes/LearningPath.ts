import type {
    ChapterResponseDTO,
    LearningPathResponseDTO,
    PathSettingsResponseDTO,
} from "@/client/model";
import {
    useDeleteLearningPath,
    useExtractLearningPath,
    useUpdatePathSettings,
} from "@/client/generated/paths/paths.ts";

export interface LearningPathSettings extends PathSettingsResponseDTO {}
export interface Chapter extends ChapterResponseDTO {}

class LearningPath {
    chapters: Chapter[];
    settings: LearningPathSettings;

    private spaceId?: number;
    private updateSettingsMutation?: ReturnType<typeof useUpdatePathSettings>["mutate"];
    private deleteMutation?: ReturnType<typeof useDeleteLearningPath>["mutate"];
    private generateMutation?: ReturnType<typeof useExtractLearningPath>["mutate"];

    isDeleting: boolean = false;
    isGenerating: boolean = false;

    constructor(chapters: Chapter[], settings: LearningPathSettings) {
        this.chapters = chapters;
        this.settings = settings;
    }

    static from(dto: LearningPathResponseDTO): LearningPath {
        return new LearningPath(dto.chapters, dto.settings);
    }

    // fluent setup, keeps constructor pure/DTO-only
    withRuntime(
        spaceId: number,
        updateSettingsMutation: ReturnType<typeof useUpdatePathSettings>["mutate"],
        deleteMutation: ReturnType<typeof useDeleteLearningPath>["mutate"],
        generateMutation: ReturnType<typeof useExtractLearningPath>["mutate"],
        isDeleting: boolean = false,
        isGenerating: boolean = false,
    ): this {
        this.spaceId = spaceId;
        this.updateSettingsMutation = updateSettingsMutation;
        this.deleteMutation = deleteMutation;
        this.generateMutation = generateMutation;
        this.isDeleting = isDeleting;
        this.isGenerating = isGenerating;
        return this;
    }

    updateSettings(settings: LearningPathSettings): void {
        this.assertRuntime();
        this.updateSettingsMutation!({ spaceId: this.spaceId!, data: settings });
    }

    delete(): void {
        this.assertRuntime();
        this.deleteMutation!({ spaceId: this.spaceId! });
    }

    regenerate(): void {
        this.assertRuntime();
        this.generateMutation!({ spaceId: this.spaceId! });
    }

    private assertRuntime(): void {
        if (this.spaceId === undefined || !this.updateSettingsMutation || !this.deleteMutation || !this.generateMutation) {
            throw new Error("LearningPath method called without runtime dependencies (call withRuntime first)");
        }
    }

    static isLearningPathDTO(
        data: { chapters?: unknown; settings?: unknown } | LearningPathResponseDTO,
    ): data is LearningPathResponseDTO {
        return "chapters" in data && "settings" in data;
    }
}

export default LearningPath;
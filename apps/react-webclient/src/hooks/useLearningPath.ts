import useSpaceId from "@/hooks/useSpaceId.ts";
import {
    getGetLearningPathJobQueryKey,
    getGetLearningPathQueryKey,
    useDeleteLearningPath,
    useExtractLearningPath,
    useGetLearningPath,
    useGetLearningPathJob, useUpdatePathSettings
} from "@/client/generated/paths/paths.ts";
import type {
    ChapterResponseDTO,
    ErrorResponseDTO,
    LearningPathJobDTO,
    LearningPathJobDTOStatus,
    LearningPathResponseDTO, NodeResponseDTO, PathSettingsResponseDTO,
} from "@/client/model";
import {useTranslation} from "react-i18next";
import {failureToast, successToast} from "@/lib/toasts.ts";
import {useQueryClient} from "@tanstack/react-query";

export interface LearningPath extends LearningPathResponseDTO {}
export interface LearningPathSettings extends PathSettingsResponseDTO {}
export interface Chapter extends ChapterResponseDTO {}
export interface Node extends NodeResponseDTO {}

type LearningPathStatus = LearningPathJobDTOStatus

function useLearningPath() {
    const { spaceId } = useSpaceId();
    const queryClient = useQueryClient();
    const { t } = useTranslation();

    const { data: jobData } = useGetLearningPathJob(spaceId ?? -1, {
        query: {
            enabled: spaceId !== undefined,
            refetchIntervalInBackground: true,
            refetchInterval: (query) => {
                const raw = query.state.data?.data;
                if (!raw || !isLearningPathJobDTO(raw)) return 2000;
                const done = raw.status === "DONE";
                const failed = raw.status === "FAILED";
                return done || failed ? false : 2000;
            },
        },
    });

    const { data: learningPathData, isLoading } = useGetLearningPath(spaceId ?? -1, {
        query: {
            enabled:
                spaceId !== undefined &&
                !!jobData &&
                isLearningPathJobDTO(jobData.data) &&
                jobData.data.status === "DONE",
        },
    });

    const maybePath = learningPathData?.data;
    let learningPath: LearningPath | undefined = undefined;
    if (maybePath && isLearningPathDTO(maybePath)) {
        learningPath = maybePath;
    }

    let learningPathStatus: LearningPathStatus = "NOT_CREATED";
    if (jobData && isLearningPathJobDTO(jobData.data)) {
        learningPathStatus = jobData.data.status;
    }

    const deleteLearningPath = useDeleteLearningPath({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: getGetLearningPathQueryKey(spaceId!),
                });
                await queryClient.invalidateQueries({
                    queryKey: getGetLearningPathJobQueryKey(spaceId!),
                });
            },
        },
    });

    const generateLearningPath = useExtractLearningPath({
        mutation: {
            onSuccess: () => {
                successToast(t("learning-path.creation.success"));
            },
            onError: () => {
                failureToast(t("learning-path.creation.error"));
            },
        },
    });

    const updateSettingsInternal = useUpdatePathSettings({
        mutation: {
            onSuccess: () => {
                successToast(t("learning-path.settings.success"));
                queryClient.invalidateQueries({
                    queryKey: getGetLearningPathQueryKey(spaceId!),
                })
            },
            onError: () => {
                failureToast(t("learning-path.settings.error"));
            }
        }
    })

    const updateSettings = (settings: LearningPathSettings) => {
        updateSettingsInternal.mutate({spaceId: spaceId!, data: settings})
    }

    return {
        path: learningPath,
        isPathLoading: isLoading,
        status: learningPathStatus,
        generateLearningPath,
        deleteLearningPath,
        updateSettings,
    };
}

export default useLearningPath;

function isLearningPathDTO(data: ErrorResponseDTO | LearningPathResponseDTO): data is LearningPathResponseDTO {
    return "chapters" in data && "settings" in data;
}

function isLearningPathJobDTO(data: ErrorResponseDTO | LearningPathJobDTO): data is LearningPathJobDTO {
    return "status" in data;
}
import useSpaceId from "@/hooks/useSpaceId.ts";
import {
    getGetLearningPathJobQueryKey,
    getGetLearningPathQueryKey,
    useDeleteLearningPath,
    useExtractLearningPath,
    useGetLearningPath,
    useGetLearningPathJob
} from "@/client/generated/paths/paths.ts";
import type {
    ErrorResponseDTO,
    LearningPathJobDTO,
    LearningPathJobDTOStatus,
    LearningPathResponseDTO,
} from "@/client/model";
import {useTranslation} from "react-i18next";
import {failureToast, successToast} from "@/lib/toasts.ts";
import {useQueryClient} from "@tanstack/react-query";

export interface LearningPath {
    chapters: Chapter[];
}

export interface Chapter {
    index: number;
    title: string;
    description: string;
    topics: number[];
}

type LearningPathStatus = LearningPathJobDTOStatus

function useLearningPath() {
    const { spaceId } = useSpaceId();
    const queryClient = useQueryClient();
    const { t } = useTranslation();

    const { data: learningPathData } = useGetLearningPath(spaceId ?? -1, {
        query: {
            enabled: spaceId !== undefined
        }
    });

    const maybePath = learningPathData?.data;
    let learningPath: LearningPath = { chapters: [] };
    if (maybePath && isLearningPathDTO(maybePath)) {
        learningPath = maybePath;
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
            }
        }
    })

    const { data } = useGetLearningPathJob(spaceId ?? -1, {
        query: {
            enabled: spaceId !== undefined,
            refetchIntervalInBackground: true,
            refetchInterval: (query) => {
                const jobData = query.state.data?.data;
                if (!jobData || !isLearningPathJobDTO(jobData)) return 2000;
                const done = jobData.status === "DONE";
                const hasChapters = learningPath.chapters.length > 0;
                return done || hasChapters ? false : 2000;
            }
        }
    })

    let learningPathStatus: LearningPathStatus = "NOT_CREATED";

    if (data && isLearningPathJobDTO(data.data)) {
        learningPathStatus = data.data.status;
    }

    const generateLearningPath = useExtractLearningPath({
        mutation: {
            onSuccess: () => {
                successToast(t("learning-path.creation.success"));
            },
            onError: () => {
                failureToast(t("learning-path.creation.error"));
            }
        }
    })

    return {
        path: learningPath,
        status: learningPathStatus,
        generateLearningPath,
        deleteLearningPath
    };
}

export default useLearningPath;

function isLearningPathDTO(data: ErrorResponseDTO | LearningPathResponseDTO): data is LearningPathResponseDTO {
    return "chapters" in data;
}

function isLearningPathJobDTO(data: ErrorResponseDTO | LearningPathJobDTO): data is LearningPathJobDTO {
    return "status" in data;
}
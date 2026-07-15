import { useNavigate } from "@tanstack/react-router";
import useSpaceId from "@/hooks/useSpaceId";
import PathConstants from "@/routing/pathConstants";
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import type {Chapter} from "@/hooks/useLearningPath.ts";
import {useTranslation} from "react-i18next";

interface ChapterGuideBreadcrumbsProps {
    chapter: Chapter;
}

function ChapterGuideBreadcrumbs({ chapter }: ChapterGuideBreadcrumbsProps) {
    const navigate = useNavigate();
    const { strSpaceId } = useSpaceId();
    const { t } = useTranslation();

    const handleLearnClick = () => {
        if (strSpaceId) {
            navigate({ to: PathConstants.SPACE_LEARN, params: { spaceId: strSpaceId } });
        }
    };

    return (
        <Breadcrumb>
            <BreadcrumbList>
                <BreadcrumbItem>
                    <BreadcrumbLink className={"cursor-pointer"} onClick={handleLearnClick}>{t("general.learn")}</BreadcrumbLink>
                </BreadcrumbItem>
                <BreadcrumbSeparator />
                <BreadcrumbItem>
                    <BreadcrumbPage className={"cursor-pointer"}>{chapter.title}</BreadcrumbPage>
                </BreadcrumbItem>
            </BreadcrumbList>
        </Breadcrumb>
    );
}

export default ChapterGuideBreadcrumbs;
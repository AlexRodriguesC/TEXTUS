import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { useTranslation } from "react-i18next";
import useViewMode from "@/hooks/useViewMode.ts";
import {Emoji} from "emoji-picker-react";
import type {ISpace} from "@/hooks/useSpaces.ts";

interface RightSidebarBreadcrumbsProps {
    space: ISpace;
}

const viewModeLabelKeys: Record<string, string> = {
    "chat": "sidebar.actions.chat.label",
    "mind-map": "sidebar.actions.map.label",
    "learning-path": "sidebar.actions.learn.label",
};

function RightSidebarBreadcrumbs({ space }: RightSidebarBreadcrumbsProps) {
    const { t } = useTranslation();
    const { viewMode, setViewMode } = useViewMode();
    const isMenu = viewMode === "menu";

    return (
        <Breadcrumb>
            <BreadcrumbList>
                <BreadcrumbItem>
                    {isMenu ? (
                        <BreadcrumbPage className="flex items-center gap-2">
                            <span>{t("sidebar.space", { spaceId: String(space.spaceId) })}</span>
                            <span className="flex items-center h-[2em] leading-none overflow-hidden">
                <Emoji unified={space.icon} size={16} />
              </span>
                        </BreadcrumbPage>
                    ) : (
                        <BreadcrumbLink asChild>
                            <button
                                type="button"
                                onClick={() => setViewMode("menu")}
                                className="flex items-center gap-2"
                            >
                                <span>{t("sidebar.space", { spaceId: String(space.spaceId) })}</span>
                                <span className="flex items-center h-[2em] leading-none overflow-hidden">
                  <Emoji unified={space.icon} size={16} />
                </span>
                            </button>
                        </BreadcrumbLink>
                    )}
                </BreadcrumbItem>

                {!isMenu && (
                    <>
                        <BreadcrumbSeparator />
                        <BreadcrumbItem>
                            <BreadcrumbPage>{t(viewModeLabelKeys[viewMode])}</BreadcrumbPage>
                        </BreadcrumbItem>
                    </>
                )}
            </BreadcrumbList>
        </Breadcrumb>
    );
}

export default RightSidebarBreadcrumbs;
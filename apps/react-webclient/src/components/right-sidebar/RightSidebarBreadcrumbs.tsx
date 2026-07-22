import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { useTranslation } from "react-i18next";
import { Emoji } from "emoji-picker-react";
import type Space from "@/classes/Space.ts";
import { Link, useMatches } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";

interface RightSidebarBreadcrumbsProps {
  space: Space;
}

const viewLabelKeys: Record<string, string> = {
  [PathConstants.SPACE_CHAT]: "sidebar.actions.chat.label",
  [PathConstants.SPACE_MIND_MAP]: "sidebar.actions.map.label",
  [PathConstants.SPACE_LEARN]: "sidebar.actions.learn.label",
};

function RightSidebarBreadcrumbs({ space }: RightSidebarBreadcrumbsProps) {
  const { t } = useTranslation();
  const matches = useMatches();

  const currentView = Object.keys(viewLabelKeys).find((path) =>
    matches.some((m) => m.routeId.endsWith(path)),
  );

  const isMenu = !currentView;

  return (
    <Breadcrumb>
      <BreadcrumbList>
        <BreadcrumbItem>
          {isMenu ? (
            <BreadcrumbPage className="flex items-center gap-2">
              <span>
                {t("sidebar.space", { spaceId: String(space.spaceId) })}
              </span>
              <span className="flex items-center h-[2em] leading-none overflow-hidden">
                <Emoji unified={space.icon} size={16} />
              </span>
            </BreadcrumbPage>
          ) : (
            <BreadcrumbLink asChild>
              <Link
                to={PathConstants.SPACE_MENU}
                params={{ spaceId: String(space.spaceId) }}
                className="flex items-center gap-2"
              >
                <span>
                  {t("sidebar.space", { spaceId: String(space.spaceId) })}
                </span>
                <span className="flex items-center h-[2em] leading-none overflow-hidden">
                  <Emoji unified={space.icon} size={16} />
                </span>
              </Link>
            </BreadcrumbLink>
          )}
        </BreadcrumbItem>

        {!isMenu && currentView && (
          <>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbPage>{t(viewLabelKeys[currentView])}</BreadcrumbPage>
            </BreadcrumbItem>
          </>
        )}
      </BreadcrumbList>
    </Breadcrumb>
  );
}

export default RightSidebarBreadcrumbs;

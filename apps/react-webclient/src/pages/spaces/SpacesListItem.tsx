import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import { Pencil } from "lucide-react";
import { Button } from "@/components/ui/button.tsx";
import { failureToastAction } from "@/lib/toasts.ts";
import { useTranslation } from "react-i18next";
import { useCallback } from "react";
import DestructiveActionButton from "@/components/DestructiveActionButton.tsx";
import EditSpace from "@/pages/spaces/EditSpace.tsx";
import { Emoji } from "emoji-picker-react";
import useDeleteSpaceMutation from "@/hooks/useDeleteSpaceMutation.ts";
import type Space from "@/classes/Space.ts";
import useTopics from "@/hooks/useTopics.tsx";

function SpacesListItem({
  spaceId,
  icon,
  name,
  description,
}: Space) {
  const { t } = useTranslation();
  const {prefetchTopics} = useTopics();
  const { deleteSpace } = useDeleteSpaceMutation();

  const submitDelete = useCallback(() => {
    deleteSpace(spaceId, {
      onError: () => {
        failureToastAction(
          t("spaces.delete.error"),
          <Button onClick={submitDelete}>{t("button.retry")}</Button>,
        );
      },
    });
  }, [deleteSpace, spaceId, t]);

  return (
    <div
      key={spaceId}
      className="flex flex-row items-center justify-between border border-border p-4 hover:bg-primary/10 transition-colors"
    >
      <Link
        to={PathConstants.SPACE_MENU}
        onClick={() => prefetchTopics(spaceId)}
        params={{ spaceId: String(spaceId) }}
        className="flex flex-1 flex-row items-center gap-4"
      >
        <Emoji unified={icon} />
        <div className="flex flex-col">
          <p className="font-medium">{name}</p>
          <p className="text-sm text-muted-foreground">{description}</p>
        </div>
      </Link>

      <div className="flex flex-row gap-2 items-center md:opacity-60 md:hover:opacity-100 md:transition-opacity">
        <EditSpace
          spaceId={spaceId}
          initialIcon={icon}
          initialName={name}
          initialDescription={description}
          trigger={<Pencil className="size-4 cursor-pointer" />}
        />
        <DestructiveActionButton
          title={t("spaces.delete.title")}
          onConfirm={submitDelete}
        />
      </div>
    </div>
  );
}

export default SpacesListItem;

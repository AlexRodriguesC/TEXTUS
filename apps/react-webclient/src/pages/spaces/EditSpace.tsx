import * as React from "react";
import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { useQueryClient } from "@tanstack/react-query";
import {
  getReadSpacesQueryKey,
  useUpdateSpace,
} from "@/api/generated/spaces/spaces.ts";
import { failureToastAction, successToast } from "@/lib/toasts.ts";
import { Button } from "@base-ui/react";
import SpaceDialogForm from "@/pages/spaces/SpaceDialogForm.tsx";

type EditSpaceProps = {
  spaceId: number;
  initialIcon: string;
  initialName: string;
  initialDescription: string;
  trigger: React.ReactNode;
};

function EditSpace({
  spaceId,
  initialIcon,
  initialName,
  initialDescription,
  trigger,
}: EditSpaceProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [icon, setIcon] = useState(initialIcon);
  const [name, setName] = useState(initialName);
  const [description, setDescription] = useState(initialDescription);

  const updateSpace = useUpdateSpace({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: getReadSpacesQueryKey(),
        });
        successToast(t("spaces.update.success"));
      },
      onError: async () => {
        failureToastAction(
          t("spaces.update.error"),
          <Button onClick={submitUpdate}>{t("button.retry")}</Button>,
        );
      },
    },
  });

  const submitUpdate = useCallback(() => {
    updateSpace.mutate({ spaceId: spaceId, data: { icon, name, description } });
  }, [updateSpace, spaceId, icon, name, description]);

  return (
    <SpaceDialogForm
      id={`edit${spaceId}`}
      trigger={trigger}
      title={t("spaces.update.title")}
      descriptionText={t("spaces.update.description")}
      submitLabel={t("button.update")}
      cancelLabel={t("button.cancel")}
      icon={icon}
      onIconChange={setIcon}
      name={name}
      onNameChange={setName}
      description={description}
      onDescriptionChange={setDescription}
      onSubmit={submitUpdate}
    />
  );
}

export default EditSpace;

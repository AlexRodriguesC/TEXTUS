import * as React from "react";
import { useCallback, useState } from "react";
import {
  getReadSpacesQueryKey,
  useCreateSpace,
} from "@/api/generated/spaces/spaces.ts";
import { failureToastAction, successToast } from "@/lib/toasts.ts";
import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";
import { useQueryClient } from "@tanstack/react-query";
import SpaceDialogForm from "@/pages/spaces/SpaceDialogForm.tsx";

type CreateSpaceProps = {
  trigger: React.ReactNode;
};

function CreateSpace({ trigger }: CreateSpaceProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const defaultName = t("spaces.add.defaultName");
  const [name, setName] = useState(defaultName);
  const defaultDescription = t("spaces.add.defaultDescription");
  const [description, setDescription] = useState(defaultDescription);
  const defaultIcon = "1f929";
  const [icon, setIcon] = useState<string>(defaultIcon);

  const createSpace = useCreateSpace({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: getReadSpacesQueryKey(),
        });
        successToast(t("spaces.add.success"));
        setIcon(defaultIcon);
        setName(defaultName);
        setDescription(defaultDescription);
      },
      onError: async () => {
        failureToastAction(
          t("spaces.add.error"),
          <Button onClick={submitCreate}>{t("button.retry")}</Button>,
        );
      },
    },
  });

  const submitCreate = useCallback(() => {
    createSpace.mutate({ data: { icon, name, description } });
  }, [createSpace, name, description]);

  return (
    <SpaceDialogForm
      id={"create"}
      trigger={trigger}
      title={t("spaces.add.title")}
      descriptionText={t("spaces.add.description")}
      submitLabel={t("button.add")}
      cancelLabel={t("button.cancel")}
      icon={icon}
      onIconChange={setIcon}
      name={name}
      onNameChange={setName}
      description={description}
      onDescriptionChange={setDescription}
      onSubmit={submitCreate}
    />
  );
}

export default CreateSpace;

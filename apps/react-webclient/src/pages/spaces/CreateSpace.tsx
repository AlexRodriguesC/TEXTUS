import * as React from "react";
import { useCallback, useState } from "react";
import { failureToastAction } from "@/lib/toasts.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";
import SpaceDialogForm from "@/pages/spaces/SpaceDialogForm.tsx";
import useCreateSpaceMutation from "@/hooks/useCreateSpaceMutation.ts";

type CreateSpaceProps = {
  trigger: React.ReactNode;
  onSuccess?: (spaceId: number) => void;
};

function CreateSpace({ trigger, onSuccess }: CreateSpaceProps) {
  const { t } = useTranslation();
  const defaultName = t("spaces.add.defaultName");
  const [name, setName] = useState(defaultName);
  const defaultDescription = t("spaces.add.defaultDescription");
  const [description, setDescription] = useState(defaultDescription);
  const defaultIcon = "1f929";
  const [icon, setIcon] = useState<string>(defaultIcon);

  const { createSpace } = useCreateSpaceMutation();

  const submitCreate = useCallback(() => {
    createSpace(
      { icon, name, description },
      {
        onSuccess: (spaceId) => {
          setIcon(defaultIcon);
          setName(defaultName);
          setDescription(defaultDescription);
          onSuccess?.(spaceId);
        },
        onError: () => {
          failureToastAction(
            t("spaces.add.error"),
            <Button onClick={submitCreate}>{t("button.retry")}</Button>,
          );
        },
      },
    );
  }, [createSpace, name, description, icon, defaultName, defaultDescription, onSuccess, t]);

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

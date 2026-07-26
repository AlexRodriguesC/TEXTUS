import * as React from "react";
import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { failureToastAction, successToast } from "@/lib/toasts.ts";
import { Button } from "@base-ui/react";
import SpaceDialogForm from "@/pages/spaces/SpaceDialogForm.tsx";
import useUpdateSpaceMutation from "@/hooks/useUpdateSpaceMutation.ts";

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
  const [icon, setIcon] = useState(initialIcon);
  const [name, setName] = useState(initialName);
  const [description, setDescription] = useState(initialDescription);

  const { updateSpace } = useUpdateSpaceMutation();

  const submitUpdate = useCallback(() => {
    const isUnchanged =
      icon === initialIcon &&
      name === initialName &&
      description === initialDescription;

    if (isUnchanged) {
      successToast(t("spaces.update.success"));
      return;
    }

    updateSpace(
      { spaceId, icon, name, description },
      {
        onError: () => {
          failureToastAction(
            t("spaces.update.error"),
            <Button onClick={submitUpdate}>{t("button.retry")}</Button>,
          );
        },
      },
    );
  }, [
    updateSpace,
    spaceId,
    icon,
    name,
    description,
    initialIcon,
    initialName,
    initialDescription,
    t,
  ]);

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

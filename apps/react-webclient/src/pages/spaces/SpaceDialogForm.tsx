import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import { Field, FieldGroup } from "@/components/ui/field";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input.tsx";
import { Button } from "@/components/ui/button.tsx";
import * as React from "react";
import { useTranslation } from "react-i18next";
import SpaceEmojiPicker from "@/components/spaces/SpaceEmojiPicker.tsx";

type SpaceDialogFormProps = {
  id: string;
  trigger?: React.ReactNode;
  title: string;
  descriptionText: string;
  submitLabel: string;
  cancelLabel: string;
  icon: string;
  onIconChange: (value: string) => void;
  name: string;
  onNameChange: (value: string) => void;
  description: string;
  onDescriptionChange: (value: string) => void;
  onCancel?: () => void;
  onSubmit: () => void;
  pending?: boolean;
};

function SpaceDialogForm({
  id,
  trigger,
  title,
  descriptionText,
  submitLabel,
  cancelLabel,
  icon,
  onIconChange,
  name,
  description,
  onNameChange,
  onDescriptionChange,
  onCancel,
  onSubmit,
}: SpaceDialogFormProps) {
  const { t } = useTranslation();

  return (
    <Dialog>
      <form
        id={id}
        onSubmit={(e) => {
          e.preventDefault();
          onSubmit();
        }}
      >
        {trigger ? <DialogTrigger asChild>{trigger}</DialogTrigger> : null}
        <DialogContent showCloseButton={false}>
          <DialogHeader>
            <DialogTitle>{title}</DialogTitle>
            <DialogDescription>{descriptionText}</DialogDescription>
          </DialogHeader>

          <FieldGroup className="grid grid-cols-1 gap-4">
            <Field>
              <Label htmlFor="space-icon">{t("space.icon")}</Label>
              <SpaceEmojiPicker icon={icon} onIconChange={onIconChange} />
            </Field>

            <Field>
              <Label htmlFor="space-name">{t("space.name")}</Label>
              <Input
                id="space-name"
                name="name"
                value={name}
                onChange={(e) => onNameChange(e.target.value)}
                className="h-10"
              />
            </Field>

            <Field>
              <Label htmlFor="space-description">
                {t("space.description")}
              </Label>
              <Input
                id="space-description"
                name="description"
                value={description}
                onChange={(e) => onDescriptionChange(e.target.value)}
                className="h-10"
              />
            </Field>
          </FieldGroup>

          <DialogFooter className={"flex flex-col md:flex-row gap-2"}>
            <DialogClose className={"order-0"} asChild>
              <Button type="button" onClick={() => onSubmit()}>
                {submitLabel}
              </Button>
            </DialogClose>

            <DialogClose className={"order-1"} asChild>
              <Button type="button" variant="outline" onClick={onCancel}>
                {cancelLabel}
              </Button>
            </DialogClose>
          </DialogFooter>
        </DialogContent>
      </form>
    </Dialog>
  );
}

export default SpaceDialogForm;

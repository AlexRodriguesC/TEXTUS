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
import { Field, FieldContent, FieldGroup } from "@/components/ui/field";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input.tsx";
import { Button } from "@/components/ui/button.tsx";
import * as React from "react";
import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import SpaceEmojiPicker from "@/components/spaces/SpaceEmojiPicker.tsx";
import { cn } from "@/lib/utils";

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
  pending,
}: SpaceDialogFormProps) {
  const { t } = useTranslation();
  const [wasInvalid, setWasInvalid] = useState({ name: false, description: false });

  const markInvalid = useCallback((field: "name" | "description", value: string) => {
    if (value.length === 0) {
      setWasInvalid((s) => ({ ...s, [field]: true }));
    }
  }, []);

  const formValid = name.length > 0 && description.length > 0;
  const nameInvalid = name.length === 0 && wasInvalid.name;
  const nameRecovered = name.length > 0 && wasInvalid.name;
  const descriptionInvalid = description.length === 0 && wasInvalid.description;
  const descriptionRecovered = description.length > 0 && wasInvalid.description;

  return (
    <Dialog
      onOpenChange={(open) => {
        if (open) {
          setWasInvalid({ name: false, description: false });
        }
      }}
    >
      <form
        id={id}
        onSubmit={(e) => {
          e.preventDefault();
          if (formValid) {
            onSubmit();
          }
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
              <FieldContent>
                <Input
                  id="space-name"
                  name="name"
                  value={name}
                  onChange={(e) => {
                    onNameChange(e.target.value);
                    markInvalid("name", e.target.value);
                  }}
                  aria-invalid={nameInvalid || undefined}
                  className={cn(
                    "h-10",
                    nameRecovered &&
                      "border-b-green-500 focus-visible:border-b-green-500",
                  )}
                />
                {nameInvalid && (
                  <p className="text-sm text-foreground">
                    {t("spaces.validation.nameRequired")}
                  </p>
                )}
              </FieldContent>
            </Field>

            <Field>
              <Label htmlFor="space-description">
                {t("space.description")}
              </Label>
              <FieldContent>
                <Input
                  id="space-description"
                  name="description"
                  value={description}
                  onChange={(e) => {
                    onDescriptionChange(e.target.value);
                    markInvalid("description", e.target.value);
                  }}
                  aria-invalid={descriptionInvalid || undefined}
                  className={cn(
                    "h-10",
                    descriptionRecovered &&
                      "border-b-green-500 focus-visible:border-b-green-500",
                  )}
                />
                {descriptionInvalid && (
                  <p className="text-sm text-foreground">
                    {t("spaces.validation.descriptionRequired")}
                  </p>
                )}
              </FieldContent>
            </Field>
          </FieldGroup>

          <DialogFooter className={"flex flex-col md:flex-row gap-2"}>
            <DialogClose className={"order-0"} asChild>
              <Button
                type="button"
                disabled={!formValid || pending}
                onClick={() => {
                  if (formValid) {
                    onSubmit();
                  }
                }}
              >
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

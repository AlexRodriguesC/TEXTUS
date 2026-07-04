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
import EmojiPicker, { Emoji, Theme } from "emoji-picker-react";
import { useTheme } from "@/hooks/useTheme.ts";
import en from "emoji-picker-react/dist/data/emojis-en";
import it from "emoji-picker-react/dist/data/emojis-it";
import type { EmojiData } from "emoji-picker-react/dist/types/exposedTypes";
import { useState } from "react";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover.tsx";

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

function mapToLanguage(language: string): EmojiData {
  switch (language) {
    case "en":
      return en;
    case "it":
      return it;
    default:
      throw new Error(
        "Translations for emoji picker not found. Did you update the languages after adding new locales?",
      );
  }
}

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
  const { theme } = useTheme();
  const { i18n } = useTranslation();
  const [open, setOpen] = useState(false);

  const displayIcon = <Emoji unified={icon ?? "1f929"} />;
  const emojiData = mapToLanguage(i18n.resolvedLanguage ? i18n.language : "");

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
        <DialogContent onCloseAutoFocus={() => setOpen(false)} showCloseButton={false}>
          <DialogHeader>
            <DialogTitle>{title}</DialogTitle>
            <DialogDescription>{descriptionText}</DialogDescription>
          </DialogHeader>

          <FieldGroup className="grid grid-cols-1 gap-4">
            <Field>
              <Label htmlFor="space-icon">{t("space.icon")}</Label>
              <Popover open={open} onOpenChange={setOpen}>
                <PopoverTrigger asChild>
                  <Button variant="ghost" size="icon" className="h-10 w-full justify-start text-2xl">
                    {displayIcon}
                  </Button>
                </PopoverTrigger>
                <PopoverContent onWheel={(e) => e.stopPropagation()} className="w-fit h-fit bg-transparent border-none shadow-none p-0">
                  <EmojiPicker
                      emojiData={emojiData}
                      height={350}
                      width={300}
                      skinTonesDisabled
                      theme={theme === "light" ? Theme.LIGHT : Theme.DARK}
                      onEmojiClick={(e) => {
                        onIconChange(e.unified)
                        setOpen(false)
                      }}
                  />
                </PopoverContent>
              </Popover>
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
              <Button type="submit" form={id}>
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

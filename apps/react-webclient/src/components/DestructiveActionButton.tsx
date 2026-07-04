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
import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";
import { Trash2 } from "lucide-react";
import * as React from "react";

type DestructiveActionButtonProps = {
  title: string;
  onConfirm: () => void;
};

function DestructiveActionButton({
  title,
  onConfirm,
    ...props
}: DestructiveActionButtonProps & React.ComponentProps<"button">) {
  const { t } = useTranslation();

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button disabled={props.disabled} variant="ghost" size="icon" aria-label="Delete">
          <Trash2 className="size-4" color={"red"} />
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>
            {t("action.destructive.description")}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter className="flex flex-col md:flex-row">
          <DialogClose asChild>
            <Button
                type="button"
                variant="secondary"
                className="flex-1 order-1 md:order-2"
            >
              {t("action.destructive.cancel")}
            </Button>
          </DialogClose>
          <DialogClose asChild>
            <Button
                type="button"
                variant="ghost"
                className="text-red-500 flex-1 order-2 md:order-1"
                onClick={onConfirm}
            >
              {t("action.destructive.confirm")}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default DestructiveActionButton;

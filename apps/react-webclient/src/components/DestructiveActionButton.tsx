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

type DestructiveActionButtonProps = {
  title: string;
  onConfirm: () => void;
};

function DestructiveActionButton({
  title,
  onConfirm,
}: DestructiveActionButtonProps) {
  const { t } = useTranslation();

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="ghost" size="icon" aria-label="Delete">
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
        <DialogFooter>
          <DialogClose asChild>
            <Button type="button" variant={"destructive"} onClick={onConfirm}>
              {t("action.destructive.confirm")}
            </Button>
          </DialogClose>
          <DialogClose asChild>
            <Button type={"button"} variant={"secondary"}>
              {t("action.destructive.cancel")}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default DestructiveActionButton;

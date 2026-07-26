import { Button } from "@/components/ui/button.tsx";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import { Plus } from "lucide-react";
import { useState } from "react";
import { useTranslation } from "react-i18next";

interface CreateChatDialogProps {
  isCreating: boolean;
  onCreate: (title: string) => void;
}

function CreateChatDialog({ isCreating, onCreate }: CreateChatDialogProps) {
  const { t } = useTranslation();
  const [title, setTitle] = useState("");

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button className={"w-fit p-0"} variant="ghost" disabled={isCreating}>
          {isCreating ? (
            <Spinner className="h-4 w-4" />
          ) : (
            <span className="flex items-center justify-center rounded-full bg-background/60 h-7 w-7 shrink-0">
              <Plus className="h-4 w-4" />
            </span>
          )}
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("chat.create.title")}</DialogTitle>
        </DialogHeader>
        <Input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder={t("chat.create.placeholder")}
        />
        <DialogFooter className="flex flex-col w-full md:flex-row md:justify-between">
          <DialogClose asChild>
            <Button variant="ghost">{t("chat.cancel")}</Button>
          </DialogClose>
          <DialogClose asChild>
            <Button
              disabled={isCreating || !title.trim()}
              onClick={() => onCreate(title)}
            >
              {t("chat.create.confirm")}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default CreateChatDialog;

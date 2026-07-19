import * as React from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { useTranslation } from "react-i18next";
import MotionButton from "@/components/MotionButton.tsx";

interface NodeProps {
  completed: boolean;
  title: string;
  summary: string;
  children?: React.ReactNode;
}

function Node({ completed = false, title, summary, children }: NodeProps) {
  const { t } = useTranslation();

  return (
    <Dialog>
      <DialogTrigger asChild>
        <MotionButton completed={completed}>{children}</MotionButton>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <div className={"flex flex-col gap-4"}>
            <p className={"text-muted-foreground text-xl font-bold uppercase"}>
              {t("general.title")}
            </p>
            <>{title}</>
            <Separator orientation={"horizontal"} />
            <p className={"text-muted-foreground text-xl font-bold uppercase"}>
              {t("general.summary")}
            </p>
            <>{summary}</>
          </div>
        </DialogHeader>
      </DialogContent>
    </Dialog>
  );
}

export default Node;

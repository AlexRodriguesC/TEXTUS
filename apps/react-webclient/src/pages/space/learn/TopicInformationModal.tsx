import type Topic from "@/classes/Topic.ts";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import TitleSummarySection from "@/components/TitleSummarySection.tsx";
import { useTranslation } from "react-i18next";

function TopicInformationModal({ topic }: { topic: Topic }) {
  const { t } = useTranslation();

  return (
    <Dialog>
      <DialogTrigger asChild>
        <InformationQuestionMark
          className={"cursor-pointer"}
          tooltip={t("exercise.guide.tooltip")}
        />
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="text-muted-foreground text-xl font-bold uppercase">
            {t("exercise.guide.title")}
          </DialogTitle>
          <DialogDescription className="sr-only">
            {t("exercise.guide.tooltip")}
          </DialogDescription>
        </DialogHeader>

        <TitleSummarySection title={topic.title} summary={topic.summary} />
      </DialogContent>
    </Dialog>
  );
}

export default TopicInformationModal;

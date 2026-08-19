import type Topic from "@/classes/Topic.ts";
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import { useTranslation } from "react-i18next";

interface TopicEntryProps {
  topic: Topic;
}

function TopicEntry({ topic }: TopicEntryProps) {
  const { t } = useTranslation();

  return (
    <div className="border p-4 hover:bg-muted/50 transition-colors">
      <div className="flex flex-row gap-2 items-center text-foreground/40">
        <h2 className="font-semibold text-foreground">{topic.title}</h2>
        <InformationQuestionMark
          className="hover:text-foreground transition-colors size-4"
          tooltip={t("chapter.guide.location", {
            page: topic.page,
            documentName: topic.sourceFileName,
          })}
        />
      </div>
      <p className="mt-1 text-sm text-muted-foreground">{topic.summary}</p>
    </div>
  );
}

export default TopicEntry;

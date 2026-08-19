import { Separator } from "@/components/ui/separator.tsx";
import { useTranslation } from "react-i18next";

interface TitleSummarySectionProps {
  title: string;
  summary: string;
}

function TitleSummarySection({ title, summary }: TitleSummarySectionProps) {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col gap-4">
      <p className="text-muted-foreground text-xl font-bold uppercase">
        {t("general.title")}
      </p>
      <>{title}</>
      <Separator orientation="horizontal" />
      <p className="text-muted-foreground text-xl font-bold uppercase">
        {t("general.summary")}
      </p>
      <>{summary}</>
    </div>
  );
}

export default TitleSummarySection;

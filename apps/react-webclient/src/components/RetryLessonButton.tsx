import { Spinner } from "@/components/ui/spinner.tsx";
import { RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";

function RetryLessonButton({
  onRetry,
  isDeleting,
}: {
  onRetry: () => void;
  isDeleting: boolean;
}) {
  const { t } = useTranslation();

  return (
    <Button disabled={isDeleting} onClick={onRetry}>
      {isDeleting ? <Spinner /> : <RotateCcw size={16} />}
      {t("button.retry")}
    </Button>
  );
}

export default RetryLessonButton;

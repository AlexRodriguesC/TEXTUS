import PathConstants from "@/routing/pathConstants.ts";
import { Button } from "@/components/ui/button.tsx";
import { Link } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";

interface ReviewTopicsButtonProps {
  onCancel: () => void;
  strSpaceId: string;
  chapterId: number;
  className?: string;
}

function ReviewTopicsButton({
  onCancel,
  strSpaceId,
  chapterId,
  className,
}: ReviewTopicsButtonProps) {
  const { t } = useTranslation();

  return (
    <div className={className}>
      <Link
        to={PathConstants.SPACE_LEARN_GUIDE}
        params={{ spaceId: strSpaceId, chapterId: chapterId.toString() }}
      >
        <Button onClick={onCancel} variant={"outline"}>
          {t("button.review-topics")}
        </Button>
      </Link>
    </div>
  );
}

export default ReviewTopicsButton;

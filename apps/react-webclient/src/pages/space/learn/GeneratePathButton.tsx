import GenerateJobButton from "@/components/GenerateJobButton.tsx";
import useLearningPath from "@/hooks/learning/useLearningPath.ts";
import useDocuments from "@/hooks/useDocuments.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import PathConstants from "@/routing/pathConstants.ts";
import { Button } from "@/components/ui/button.tsx";
import { Link } from "@tanstack/react-router";
import { useMemo } from "react";
import { useTranslation } from "react-i18next";

function GeneratePathButton() {
  const { t } = useTranslation();
  const { spaceId, strSpaceId } = useSpaceId();
  const { documents } = useDocuments(spaceId);
  const hasDocuments = useMemo(() => documents.length > 0, [documents.length]);
  const { status, regeneratePath, deletePath } = useLearningPath();

  const fallback = !hasDocuments ? (
    <Button className="w-full" asChild>
      <Link to={PathConstants.SPACE_MENU} params={{ spaceId: strSpaceId! }}>
        {t("learning-path.empty.cta-add")}
      </Link>
    </Button>
  ) : undefined;

  return (
    <GenerateJobButton
      status={status}
      labels={{
        cta: t("learning-path.empty.cta"),
        generating: t("learning-path.empty.generating"),
        ctaRegenerate: t("learning-path.empty.cta-regenerate"),
      }}
      onGenerate={regeneratePath}
      onRegenerate={() => {
        deletePath();
        regeneratePath();
      }}
      fallback={fallback}
    />
  );
}

export default GeneratePathButton;

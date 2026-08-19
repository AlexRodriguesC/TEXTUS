import GenerateJobButton from "@/components/GenerateJobButton.tsx";
import useMindMap from "@/hooks/useMindMap.ts";
import useDocuments from "@/hooks/useDocuments.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import PathConstants from "@/routing/pathConstants.ts";
import { Button } from "@/components/ui/button.tsx";
import { Link } from "@tanstack/react-router";
import { useMemo } from "react";
import { useTranslation } from "react-i18next";

function GenerateMindMapButton() {
  const { t } = useTranslation();
  const { spaceId, strSpaceId } = useSpaceId();
  const { documents } = useDocuments(spaceId);
  const hasDocuments = useMemo(() => documents.length > 0, [documents.length]);
  const { status, regenerateMindMap, deleteMindMap } = useMindMap();

  const fallback = !hasDocuments ? (
    <Button className="w-full" asChild>
      <Link to={PathConstants.SPACE_MENU} params={{ spaceId: strSpaceId! }}>
        {t("mindmap.empty.cta-add")}
      </Link>
    </Button>
  ) : undefined;

  return (
    <GenerateJobButton
      status={status}
      labels={{
        cta: t("mindmap.empty.cta"),
        generating: t("mindmap.empty.generating"),
        ctaRegenerate: t("mindmap.empty.cta-regenerate"),
      }}
      onGenerate={regenerateMindMap}
      onRegenerate={() => {
        deleteMindMap();
        regenerateMindMap();
      }}
      fallback={fallback}
    />
  );
}

export default GenerateMindMapButton;

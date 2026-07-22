import { SidebarGroupContent } from "@/components/ui/sidebar.tsx";
import { useTranslation } from "react-i18next";
import useMindMap from "@/hooks/useMindMap.ts";
import MindMapJobStatus from "@/classes/MindMapJobStatus.ts";
import { Button } from "@/components/ui/button.tsx";

function MindMapSidebarButton() {
  const { t } = useTranslation();
  const { status, regenerateMindMap, deleteMindMap } = useMindMap();

  const notGenerated = status == MindMapJobStatus.NOT_CREATED;
  const canRegenerate =
    status == MindMapJobStatus.DONE || status == MindMapJobStatus.FAILED;
  const isGenerating = status == MindMapJobStatus.GENERATING;

  return (
    <SidebarGroupContent>
      <Button
        disabled={isGenerating}
        onClick={() => {
          if (canRegenerate) {
            deleteMindMap();
          }
          regenerateMindMap();
        }}
      >
        {notGenerated && t("sidebar.mind-map.generate")}
        {isGenerating && t("sidebar.mind-map.generating")}
        {canRegenerate && t("sidebar.mind-map.re-generate")}
      </Button>
    </SidebarGroupContent>
  );
}

export default MindMapSidebarButton;

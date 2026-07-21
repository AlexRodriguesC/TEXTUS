import { SidebarGroupContent } from "@/components/ui/sidebar.tsx";
import { useTranslation } from "react-i18next";
import useMindMap from "@/hooks/useMindMap.ts";
import { Button } from "@/components/ui/button.tsx";

function MindMapSidebarButton() {
  const { t } = useTranslation();
  const { mindMap, status } = useMindMap();

  const notGenerated = status == "NOT_CREATED";
  const canRegenerate = status == "DONE" || status == "FAILED";
  const isGenerating = status == "GENERATING";

  return (
    <SidebarGroupContent>
      <Button
        disabled={isGenerating}
        onClick={() => {
          if (canRegenerate) {
            mindMap?.delete();
          }
          mindMap?.regenerate();
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

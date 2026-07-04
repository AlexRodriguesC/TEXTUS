import useSpaceId from "@/hooks/useSpaceId.ts";
import {SidebarGroupContent} from "@/components/ui/sidebar.tsx";
import {useTranslation} from "react-i18next";
import useMindMap from "@/hooks/useMindMap.ts";
import {Button} from "@/components/ui/button.tsx";

function MindMapSidebarButton() {
    const { spaceId } = useSpaceId();
    const { t } = useTranslation();
    const { extractMindMap, status} = useMindMap(spaceId);

    const notGenerated = status == "NOT_CREATED";
    const canRegenerate = status == "DONE" || status == "FAILED";
    const isGenerating = status == "GENERATING"

    return (
        <SidebarGroupContent>
            <Button disabled={!spaceId} onClick={() => extractMindMap.mutate({spaceId: spaceId!})}>
                {notGenerated && t("sidebar.mind-map.generate")}
                {isGenerating && t("sidebar.mind-map.generating")}
                {canRegenerate && t("sidebar.mind-map.re-generate")}
            </Button>
        </SidebarGroupContent>
    )
}

export default MindMapSidebarButton;

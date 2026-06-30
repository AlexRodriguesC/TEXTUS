import DocumentStatusList from "@/pages/space/DocumentStatusList.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import Upload from "@/pages/space/Upload.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import { Card } from "@/components/ui/card.tsx";
import { useTranslation } from "react-i18next";
import useMindMap from "@/hooks/useMindMap.ts";
import type { ISpace } from "@/hooks/useSpaces.ts";

interface SpaceSidebarProps {
  space: ISpace;
}

function SpaceSidebar({ space }: SpaceSidebarProps) {
  const { t } = useTranslation();
  const { extractMindMap, status } = useMindMap(space.spaceId);

  return (
    <>
      <Card className="bg-muted/50 flex flex-col h-full items-stretch p-4">
        <div className="flex-1 flex flex-col gap-4">
          <p className={"font-bold text-muted-foreground"}>
            {t("documents.status.overview")}
          </p>
          <DocumentStatusList />
          <Separator orientation="horizontal" className="mt-4 mb-4" />
          <div className={"h-full items-center content-center"}>
            <Upload showHeaders={false} />
          </div>
        </div>

        <Separator orientation="horizontal" className="mt-4 mb-4" />

        <Button
          className="w-full"
          onClick={() => extractMindMap.mutate(space)}
          disabled={status === "GENERATING"}
        >
          <span className="flex items-center justify-center gap-2">
            {t("mindmap.button")}
            {status === "GENERATING" && <Spinner />}
          </span>
        </Button>
      </Card>
    </>
  );
}

export default SpaceSidebar;

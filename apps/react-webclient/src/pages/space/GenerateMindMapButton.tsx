import useMindMap from "@/hooks/useMindMap.ts";
import MindMapJobStatus from "@/classes/MindMapJobStatus.ts";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import { X } from "lucide-react";

function GenerateMindMapButton() {
  const { t } = useTranslation();
  const { status, regenerateMindMap, deleteMindMap } = useMindMap();

  if (status === MindMapJobStatus.NOT_CREATED) {
    return (
      <Button onClick={() => regenerateMindMap()} className={"w-full"}>
        {t("mindmap.empty.cta")}
      </Button>
    );
  }

  if (status === MindMapJobStatus.GENERATING) {
    return (
      <Button disabled={true} className={"w-full"}>
        <div className={"flex items-center justify-between gap-2"}>
          {t("mindmap.empty.cta")}
          <Spinner />
        </div>
      </Button>
    );
  }

  if (status === MindMapJobStatus.FAILED) {
    return (
      <Button
        onClick={() => {
          deleteMindMap();
          regenerateMindMap();
        }}
        className={"w-full"}
      >
        <div className={"flex items-center justify-between gap-2"}>
          {t("mindmap.empty.cta-regenerate")}
          <X color={"red"} />
        </div>
      </Button>
    );
  }

  if (status === MindMapJobStatus.DONE) {
    return (
      <Button
        onClick={() => {
          deleteMindMap();
          regenerateMindMap();
        }}
        className={"w-full"}
      >
        <div className={"flex items-center justify-between gap-2"}>
          {t("mindmap.empty.cta-regenerate")}
        </div>
      </Button>
    );
  }
}

export default GenerateMindMapButton;

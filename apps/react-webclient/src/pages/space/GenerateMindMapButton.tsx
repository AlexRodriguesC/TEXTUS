import useMindMap from "@/hooks/useMindMap.ts";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import { X } from "lucide-react";

function GenerateMindMapButton() {
  const { t } = useTranslation();
  const { mindMap, status } = useMindMap();

  if (status === "NOT_CREATED") {
    return (
      <Button onClick={() => mindMap?.regenerate()} className={"w-full"}>
        {t("mindmap.empty.cta")}
      </Button>
    );
  }

  if (status === "GENERATING") {
    return (
      <Button disabled={true} className={"w-full"}>
        <div className={"flex items-center justify-between gap-2"}>
          {t("mindmap.empty.cta")}
          <Spinner />
        </div>
      </Button>
    );
  }

  if (status === "FAILED") {
    return (
      <Button
        onClick={() => {
          mindMap?.delete();
          mindMap?.regenerate();
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

  if (status === "DONE") {
    return (
      <Button
        onClick={() => {
          mindMap?.delete();
          mindMap?.regenerate();
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

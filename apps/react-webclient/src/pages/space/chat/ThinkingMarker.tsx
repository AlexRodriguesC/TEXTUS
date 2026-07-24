import { Marker, MarkerContent, MarkerIcon } from "@/components/ui/marker.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import {useTranslation} from "react-i18next";

// Shimmer loading marker shown while the assistant has not produced content yet.
function ThinkingMarker() {
    const {t} = useTranslation();

  return (
    <Marker role="status" className="shimmer">
      <MarkerIcon>
        <Spinner />
      </MarkerIcon>
        <MarkerContent className="loading-dots">
            {t("general.thinking")}
        </MarkerContent>
    </Marker>
  );
}

export default ThinkingMarker;

import { Marker, MarkerContent, MarkerIcon } from "@/components/ui/marker.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";

// Shimmer loading marker shown while the assistant has not produced content yet.
function ThinkingMarker() {
  return (
    <Marker role="status" className="shimmer">
      <MarkerIcon>
        <Spinner />
      </MarkerIcon>
      <MarkerContent className="shimmer loading-dots" />
    </Marker>
  );
}

export default ThinkingMarker;

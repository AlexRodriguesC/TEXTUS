import { Button } from "@/components/ui/button.tsx";
import { Marker, MarkerContent, MarkerIcon } from "@/components/ui/marker.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import type { ReactNode } from "react";

type JobStatus = "NOT_CREATED" | "GENERATING" | "DONE" | "FAILED";

type GenerateJobButtonLabels = {
  cta: string;
  generating: string;
  ctaRegenerate: string;
};

type GenerateJobButtonProps = {
  status: JobStatus;
  labels: GenerateJobButtonLabels;
  onGenerate: () => void;
  onRegenerate: () => void;
  fallback?: ReactNode;
};

function GenerateJobButton({
  status,
  labels,
  onGenerate,
  onRegenerate,
  fallback,
}: GenerateJobButtonProps) {
  if (fallback !== undefined) {
    return fallback;
  }

  if (status === "FAILED") {
    return (
      <Button
        onClick={onRegenerate}
        className="w-full border-destructive bg-destructive/80 hover:bg-destructive"
      >
        <p className="flex items-center justify-between gap-2">
          {labels.ctaRegenerate}
        </p>
      </Button>
    );
  }

  if (status === "NOT_CREATED") {
    return (
      <Button onClick={onGenerate} className="w-full">
        {labels.cta}
      </Button>
    );
  }

  if (status === "GENERATING") {
    return (
      <Button disabled={true} className="w-full">
        <Marker role="status" className="w-full flex place-content-center">
          <MarkerIcon>
            <Spinner />
          </MarkerIcon>
          <MarkerContent className="flex items-center justify-between gap-2 shimmer">
            {labels.generating}
          </MarkerContent>
        </Marker>
      </Button>
    );
  }

  if (status === "DONE") {
    return (
      <Button onClick={onRegenerate} className="w-full">
        <div className="flex items-center justify-between gap-2">
          {labels.ctaRegenerate}
        </div>
      </Button>
    );
  }

  return null;
}

export default GenerateJobButton;
export type { GenerateJobButtonLabels, GenerateJobButtonProps, JobStatus };

import { Handle, type NodeProps, Position } from "@xyflow/react";
import MotionPillButton from "@/components/MotionPillButton.tsx";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip.tsx";
import { ChevronDown, ChevronRight } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import TitleSummarySection from "@/components/TitleSummarySection.tsx";
import { useLayoutEffect, useRef, useState, type MouseEvent } from "react";

interface MapNodeData {
  id: number;
  title: string;
  summary: string;
  completed: boolean;
  isPage: boolean;
  hasChildren: boolean;
  isExpanded: boolean;
  onToggleExpand?: () => void;
}

function TruncatedTitlePill({
  title,
  completed,
  entityId,
  isPage,
  onClick,
}: {
  title: string;
  completed: boolean;
  entityId: number;
  isPage: boolean;
  onClick?: (event: MouseEvent<HTMLButtonElement>) => void;
}) {
  const textRef = useRef<HTMLSpanElement>(null);
  const [isTruncated, setIsTruncated] = useState(false);

  useLayoutEffect(() => {
    const el = textRef.current;
    if (!el) return;

    const checkTruncation = () => {
      setIsTruncated(el.scrollWidth > el.clientWidth);
    };

    checkTruncation();

    const observer = new ResizeObserver(checkTruncation);
    observer.observe(el);

    return () => observer.disconnect();
  }, [title]);

  return (
    <Tooltip open={isTruncated ? undefined : false}>
      <TooltipTrigger asChild>
        <MotionPillButton
          completed={completed}
          entityId={entityId}
          isPage={isPage}
          className="w-[154px]"
          textRef={textRef}
          onClick={onClick}
        >
          {title}
        </MotionPillButton>
      </TooltipTrigger>
      {isTruncated && (
        <TooltipContent side="top">{title}</TooltipContent>
      )}
    </Tooltip>
  );
}

function MapNode({ data }: NodeProps) {
  const nodeData = data as MapNodeData;

  const hideHandle = {
    width: 0,
    height: 0,
    border: "none",
    background: "transparent",
  };

  return (
    <div className="relative flex w-43 items-center gap-1">
      <Handle type="target" position={Position.Top} style={hideHandle} />
      {nodeData.hasChildren ? (
        <button
          type="button"
          className="flex size-3.5 shrink-0 items-center justify-center"
          onClick={(e) => {
            e.stopPropagation();
            nodeData.onToggleExpand?.();
          }}
        >
          {nodeData.isExpanded ? (
            <ChevronDown className="size-3.5 text-muted-foreground" />
          ) : (
            <ChevronRight className="size-3.5 text-muted-foreground" />
          )}
        </button>
      ) : (
        <span className="flex size-3.5 shrink-0 items-center justify-center" />
      )}

      <Dialog>
        <DialogTrigger asChild>
          <TruncatedTitlePill
            title={nodeData.title}
            completed={nodeData.completed}
            entityId={nodeData.id}
            isPage={nodeData.isPage}
            onClick={(e) => e.stopPropagation()}
          />
        </DialogTrigger>
        <DialogContent>
          <DialogHeader>
            <TitleSummarySection
              title={nodeData.title}
              summary={nodeData.summary}
            />
          </DialogHeader>
        </DialogContent>
      </Dialog>
      <Handle type="source" position={Position.Bottom} style={hideHandle} />
    </div>
  );
}

export default MapNode;

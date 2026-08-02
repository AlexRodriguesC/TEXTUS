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
import { useTheme } from "@/hooks/context/useTheme.ts";
import { cn } from "@/lib/utils.ts";
import {
  useLayoutEffect,
  useRef,
  useState,
  type MouseEvent,
  type ReactNode,
} from "react";

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
  button,
}: {
  title: string;
  completed: boolean;
  entityId: number;
  isPage: boolean;
  onClick?: (event: MouseEvent<HTMLButtonElement>) => void;
  button?: ReactNode;
}) {
  const textRef = useRef<HTMLSpanElement>(null);
  const [isTruncated, setIsTruncated] = useState(false);
  const { theme } = useTheme();

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
      <MotionPillButton
        completed={completed}
        entityId={entityId}
        isPage={isPage}
        className="w-[154px]"
        button={button}
      >
        <TooltipTrigger asChild>
          <DialogTrigger asChild>
            <button
              type="button"
              className="flex h-full min-w-0 flex-1 items-center justify-center"
              onClick={onClick}
            >
              <span
                ref={textRef}
                className={cn(
                  "block w-full truncate text-center",
                  theme === "dark"
                    ? "text-primary-foreground"
                    : "text-secondary-foreground",
                )}
              >
                {title}
              </span>
            </button>
          </DialogTrigger>
        </TooltipTrigger>
      </MotionPillButton>
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
    <div className="relative flex items-center">
      <Handle type="target" position={Position.Left} style={hideHandle} />

      <Dialog>
        <TruncatedTitlePill
          title={nodeData.title}
          completed={nodeData.completed}
          entityId={nodeData.id}
          isPage={nodeData.isPage}
          onClick={(e) => e.stopPropagation()}
          button={
            nodeData.hasChildren ? (
              <button
                type="button"
                aria-label={nodeData.isExpanded ? "Collapse" : "Expand"}
                className="flex shrink-0 items-center justify-center"
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
            ) : undefined
          }
        />
        <DialogContent>
          <DialogHeader>
            <TitleSummarySection
              title={nodeData.title}
              summary={nodeData.summary}
            />
          </DialogHeader>
        </DialogContent>
      </Dialog>
      <Handle type="source" position={Position.Right} style={hideHandle} />
    </div>
  );
}

export default MapNode;

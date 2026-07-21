import { Handle, type NodeProps, Position } from "@xyflow/react";
import MotionButton from "@/components/MotionButton.tsx";
import { ChevronDown, ChevronRight, CircleQuestionMark } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { useTranslation } from "react-i18next";

interface MapNodeData {
  id: number;
  title: string;
  summary: string;
  completed: boolean;
  hasChildren: boolean;
  isExpanded: boolean;
}

function MapNode({ data }: NodeProps) {
  const nodeData = data as MapNodeData;
  const { t } = useTranslation();

  const hideHandle = {
    width: 0,
    height: 0,
    border: "none",
    background: "transparent",
  };

  return (
    <div className="relative flex items-center gap-1">
      <Handle type="target" position={Position.Top} style={hideHandle} />
      {nodeData.hasChildren &&
        (nodeData.isExpanded ? (
          <ChevronDown className="size-3.5 shrink-0 text-muted-foreground" />
        ) : (
          <ChevronRight className="size-3.5 shrink-0 text-muted-foreground" />
        ))}
      <MotionButton completed={nodeData.completed}>{nodeData.id}</MotionButton>
      <Dialog>
        <DialogTrigger asChild>
          <button
            type="button"
            className="absolute -top-1 -right-1 rounded-full bg-background p-0.5 shadow-sm hover:bg-muted"
            onClick={(e) => e.stopPropagation()}
          >
            <CircleQuestionMark className="size-3 text-muted-foreground" />
          </button>
        </DialogTrigger>
        <DialogContent>
          <DialogHeader>
            <div className={"flex flex-col gap-4"}>
              <p className={"text-muted-foreground text-xl font-bold uppercase"}>
                {t("general.title")}
              </p>
              <>{nodeData.title}</>
              <Separator orientation={"horizontal"} />
              <p className={"text-muted-foreground text-xl font-bold uppercase"}>
                {t("general.summary")}
              </p>
              <>{nodeData.summary}</>
            </div>
          </DialogHeader>
        </DialogContent>
      </Dialog>
      <Handle type="source" position={Position.Bottom} style={hideHandle} />
    </div>
  );
}

export default MapNode;

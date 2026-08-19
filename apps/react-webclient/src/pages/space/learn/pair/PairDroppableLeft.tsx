import { useDroppable } from "@dnd-kit/core";
import DraggableChip from "@/pages/space/learn/dnd/DraggableChip.tsx";
import type { ChipStatus } from "@/pages/space/learn/dnd/DraggableChip.tsx";
import { cn } from "@/lib/utils.ts";

interface PairDroppableLeftProps {
  left: string;
  assignedRight?: string;
  onRemove?: () => void;
  disabled: boolean;
  status?: ChipStatus;
}

function PairDroppableLeft({
  left,
  assignedRight,
  onRemove,
  disabled,
  status,
}: PairDroppableLeftProps) {
  const { setNodeRef, isOver } = useDroppable({
    id: `left-${left}`,
    disabled,
  });

  return (
    <div className="flex items-stretch gap-3">
      <div className="flex-1 border p-3 min-h-14 flex items-center bg-primary/10">
        <span className="font-medium">{left}</span>
      </div>

      <div
        ref={setNodeRef}
        className={cn(
          "flex-1 border border-dashed min-h-14",
          isOver ? "border-primary bg-accent" : "border-border",
          status === "correct" && "border-primary bg-primary/40",
          status === "incorrect" && "border-destructive bg-destructive/40",
        )}
      >
        {assignedRight && (
          <DraggableChip
            id={assignedRight}
            value={assignedRight}
            onRemove={onRemove}
            disabled={disabled}
            status={status}
            className="w-full h-full border px-3 py-1 text-sm flex items-center justify-center"
            variant="assigned"
          />
        )}
      </div>
    </div>
  );
}

export default PairDroppableLeft;

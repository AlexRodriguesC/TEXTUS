import { useDroppable } from "@dnd-kit/core";
import DraggableChip from "@/pages/space/learn/dnd/DraggableChip.tsx";
import type { ChipStatus } from "@/pages/space/learn/dnd/DraggableChip.tsx";
import type { KeyItem } from "@/pages/space/learn/group/useGroupAssignment.ts";
import { cn } from "@/lib/utils.ts";

interface GroupDroppableLabelProps {
  label: string;
  index: number;
  assignedKeys: KeyItem[];
  onRemove: (id: string) => void;
  disabled: boolean;
  getKeyStatus: (id: string) => ChipStatus;
}

function GroupDroppableLabel({
  label,
  index,
  assignedKeys,
  onRemove,
  disabled,
  getKeyStatus,
}: GroupDroppableLabelProps) {
  const { setNodeRef, isOver } = useDroppable({
    id: `label-${index}`,
    disabled,
  });

  return (
    <div
      ref={setNodeRef}
      className={cn(
        "flex flex-wrap items-center gap-2 rounded-md border p-3 min-h-14",
        isOver ? "border-primary bg-accent" : "border-border",
      )}
    >
      <span className="font-medium mr-2">{label}</span>
      {assignedKeys.map((item) => (
        <DraggableChip
          key={item.id}
          id={item.id}
          value={item.value}
          onRemove={() => onRemove(item.id)}
          disabled={disabled}
          status={getKeyStatus(item.id)}
          variant="assigned"
        />
      ))}
    </div>
  );
}

export default GroupDroppableLabel;

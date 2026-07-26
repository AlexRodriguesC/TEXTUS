import { useDraggable } from "@dnd-kit/core";
import { cn } from "@/lib/utils.ts";

export type ChipStatus = "correct" | "incorrect" | null;

interface DraggableChipProps {
  id: string;
  value: string;
  disabled: boolean;
  status?: ChipStatus;
  onRemove?: () => void;
  className?: string;
  variant?: "assigned" | "pool";
}

function DraggableChip({
  id,
  value,
  disabled,
  status,
  onRemove,
  className,
  variant = "pool",
}: DraggableChipProps) {
  const { attributes, listeners, setNodeRef, transform, isDragging } =
    useDraggable({ id, disabled });

  const style = transform
    ? { transform: `translate3d(${transform.x}px, ${transform.y}px, 0)` }
    : undefined;

  return (
    <button
      ref={setNodeRef}
      {...attributes}
      {...listeners}
      style={style}
      onClick={disabled ? undefined : onRemove}
      disabled={disabled}
      className={cn(
        variant === "assigned"
          ? "rounded-md px-3 py-1 text-sm"
          : "rounded-md border px-3 py-2 text-sm",
        status === "correct" &&
          (variant === "assigned"
            ? "bg-primary/40 text-primary-foreground"
            : "border-primary bg-primary text-primary-foreground"),
        status === "incorrect" &&
          (variant === "assigned"
            ? "bg-destructive/40 text-destructive-foreground"
            : "border-destructive bg-destructive text-destructive-foreground"),
        status == null &&
          (variant === "assigned" ? "bg-secondary" : "bg-background"),
        !disabled && "cursor-grab touch-none",
        onRemove && !disabled && "hover:bg-destructive/20",
        isDragging && "opacity-50",
        className,
      )}
    >
      {value}
    </button>
  );
}

export default DraggableChip;

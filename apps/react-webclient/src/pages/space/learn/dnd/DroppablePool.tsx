import { useDroppable } from "@dnd-kit/core";
import { cn } from "@/lib/utils.ts";
import type { ReactNode } from "react";

interface DroppablePoolProps {
  id: string;
  disabled: boolean;
  children: ReactNode;
  className?: string;
  isOverClassName?: string;
}

function DroppablePool({
  id,
  disabled,
  children,
  className,
  isOverClassName = "bg-accent/50",
}: DroppablePoolProps) {
  const { setNodeRef, isOver } = useDroppable({ id, disabled });

  return (
    <div
      ref={setNodeRef}
      className={cn(
        "flex flex-wrap gap-2 pt-4 min-h-14 rounded-md",
        isOver && isOverClassName,
        className,
      )}
    >
      {children}
    </div>
  );
}

export default DroppablePool;

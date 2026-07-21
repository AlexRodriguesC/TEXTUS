import { useMemo, useState } from "react";
import {
  DndContext,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import { useDraggable, useDroppable } from "@dnd-kit/core";
import type { PairExercise, PairResponseDTO } from "@/client/model";
import type { LeftRight } from "@/client/model";
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";
import { cn } from "@/lib/utils.ts";
import * as React from "react";

interface PairProps {
  pair: PairResponseDTO;
  submitted: boolean;
  onAnswer?: (answer: LeftRight[] | null) => void;
}

function DroppableLeft({
  left,
  assignedRight,
  onRemove,
  disabled,
  status,
}: {
  left: string;
  assignedRight?: string;
  onRemove?: () => void;
  disabled: boolean;
  status?: "correct" | "incorrect" | null;
}) {
  const { setNodeRef, isOver } = useDroppable({ id: `left-${left}`, disabled });

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
          <RightChip
            id={assignedRight}
            value={assignedRight}
            onRemove={onRemove}
            disabled={disabled}
            status={status}
            className="w-full h-full"
          />
        )}
      </div>
    </div>
  );
}

function RightChip({
  id,
  value,
  onRemove,
  disabled,
  className,
  status,
}: {
  id: string;
  value: string;
  onRemove?: () => void;
  disabled: boolean;
  className?: string;
  status?: "correct" | "incorrect" | null;
}) {
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
        "border px-3 py-1 text-sm flex items-center justify-center",
        status === "correct" &&
          "bg-primary text-primary-foreground border-primary",
        status === "incorrect" &&
          "bg-destructive text-destructive-foreground border-destructive",
        status == null && "bg-primary/20",
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

function RightPool({
  children,
  disabled,
}: {
  children: React.ReactNode;
  disabled: boolean;
}) {
  const { setNodeRef, isOver } = useDroppable({ id: "right-pool", disabled });

  return (
    <div
      ref={setNodeRef}
      className={cn(
        "flex flex-wrap gap-2 pt-4 min-h-14 rounded-md",
        isOver && "bg-primary/40",
      )}
    >
      {children}
    </div>
  );
}

function Pair({ pair, submitted, onAnswer }: PairProps) {
  const leftItems = useMemo(
    () => pair.pairs.map((p: PairExercise) => p.left),
    [pair.pairs],
  );
  const rightItems = useMemo(
    () => pair.pairs.map((p: PairExercise) => p.right),
    [pair.pairs],
  );

  const correctRightByLeft = useMemo(() => {
    const map: Record<string, string> = {};
    for (const p of pair.pairs) map[p.left] = p.right;
    return map;
  }, [pair.pairs]);

  const [assignments, setAssignments] = useState<Record<string, string>>({});

  const sensors = useSensors(useSensor(PointerSensor));

  function notify(next: Record<string, string>) {
    if (!onAnswer) return;

    const allAssigned =
      leftItems.length > 0 &&
      leftItems.every((left: string) => next[left] !== undefined);

    if (allAssigned) {
      const answer: LeftRight[] = leftItems.map((left: string) => ({
        left,
        right: next[left],
      }));
      onAnswer(answer);
    } else {
      onAnswer(null);
    }
  }

  function removeRight(right: string) {
    if (submitted) return;
    setAssignments((prev) => {
      const next: Record<string, string> = {};
      for (const l of Object.keys(prev)) {
        if (prev[l] !== right) next[l] = prev[l];
      }
      notify(next);
      return next;
    });
  }

  function handleDragEnd(event: DragEndEvent) {
    if (submitted) return;
    const { active, over } = event;
    if (!over) return;

    const right = String(active.id);

    if (over.id === "right-pool") {
      removeRight(right);
      return;
    }

    const left = String(over.id).replace("left-", "");

    setAssignments((prev) => {
      const next: Record<string, string> = {};
      for (const l of Object.keys(prev)) {
        if (prev[l] !== right) next[l] = prev[l];
      }
      next[left] = right;
      notify(next);
      return next;
    });
  }

  const usedRights = new Set(Object.values(assignments));
  const remainingRights = rightItems.filter((r: string) => !usedRights.has(r));

  return (
    <div className={"flex flex-col gap-8"}>
      <ExerciseHeader question={pair.request} topicId={pair.topicId} />

      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
      >
        <div className="flex flex-col gap-3">
          {leftItems.map((left: string) => {
            const assigned = assignments[left];
            const leftCorrect = assigned === correctRightByLeft[left];
            const status = submitted
              ? leftCorrect
                ? "correct"
                : "incorrect"
              : null;
            return (
              <DroppableLeft
                key={left}
                left={left}
                assignedRight={assigned}
                onRemove={() => assigned && removeRight(assigned)}
                disabled={submitted}
                status={status}
              />
            );
          })}
        </div>

        <RightPool disabled={submitted}>
          {remainingRights.map((right: string) => (
            <RightChip
              key={right}
              id={right}
              value={right}
              disabled={submitted}
              status={submitted ? "incorrect" : null}
            />
          ))}
        </RightPool>
      </DndContext>
    </div>
  );
}

export default Pair;

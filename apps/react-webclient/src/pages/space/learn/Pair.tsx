import { useEffect, useMemo, useState } from "react";
import {
  DndContext,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import { useDraggable, useDroppable } from "@dnd-kit/core";
import type { PairResponseDTO } from "@/client/model";
import type { LeftRight } from "@/client/model";
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";
import { cn } from "@/lib/utils.ts";
import * as React from "react";

interface PairProps {
  pair: PairResponseDTO;
  submitted: boolean;
  onAnswer?: (answer: LeftRight[] | null, isCorrect: boolean | null) => void;
}

function DroppableLeft({
                         left,
                         assignedRight,
                         onRemove,
                         disabled,
                         status,
                       }: {
  left: string;
  assignedRight: string | undefined;
  onRemove: () => void;
  disabled: boolean;
  status?: "correct" | "incorrect" | null;
}) {
  const { setNodeRef, isOver } = useDroppable({ id: `left-${left}`, disabled });

  return (
      <div
          ref={setNodeRef}
          className={cn(
              "flex items-center justify-between rounded-md border p-3 min-h-14",
              isOver ? "border-primary bg-accent" : "border-border",
              status === "correct" && "border-primary bg-primary/10",
              status === "incorrect" && "border-destructive bg-destructive/10",
          )}
      >
        <span className="font-medium">{left}</span>
        {assignedRight && (
            <RightChip
                id={assignedRight}
                value={assignedRight}
                onRemove={onRemove}
                disabled={disabled}
            />
        )}
      </div>
  );
}

function RightChip({
                     id,
                     value,
                     onRemove,
                     disabled,
                   }: {
  id: string;
  value: string;
  onRemove?: () => void;
  disabled: boolean;
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
              "rounded-md border bg-background px-3 py-1 text-sm",
              !disabled && "cursor-grab touch-none",
              onRemove && !disabled && "hover:bg-destructive/20",
              isDragging && "opacity-50",
          )}
      >
        {value}
      </button>
  );
}

function RightPool({ children, disabled }: { children: React.ReactNode; disabled: boolean }) {
  const { setNodeRef, isOver } = useDroppable({ id: "right-pool", disabled });

  return (
      <div
          ref={setNodeRef}
          className={cn(
              "flex flex-wrap gap-2 pt-4 min-h-14 rounded-md",
              isOver && "bg-accent/50",
          )}
      >
        {children}
      </div>
  );
}

function Pair({ pair, submitted, onAnswer }: PairProps) {
  const leftItems = useMemo(() => pair.pairs.map((p) => p.left), [pair.pairs]);
  const rightItems = useMemo(() => pair.pairs.map((p) => p.right), [pair.pairs]);

  const correctRightByLeft = useMemo(() => {
    const map: Record<string, string> = {};
    for (const p of pair.pairs) map[p.left] = p.right;
    return map;
  }, [pair.pairs]);

  const [assignments, setAssignments] = useState<Record<string, string>>({});

  const sensors = useSensors(useSensor(PointerSensor));

  function removeRight(right: string) {
    if (submitted) return;
    setAssignments((prev) => {
      const next: Record<string, string> = {};
      for (const l of Object.keys(prev)) {
        if (prev[l] !== right) next[l] = prev[l];
      }
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
      return next;
    });
  }

  const isCorrect = useMemo(() => {
    return leftItems.every((left) => assignments[left] === correctRightByLeft[left]);
  }, [assignments, leftItems, correctRightByLeft]);

  useEffect(() => {
    if (!onAnswer) return;

    const allAssigned =
        leftItems.length > 0 &&
        leftItems.every((left) => assignments[left] !== undefined);

    if (allAssigned) {
      const answer: LeftRight[] = leftItems.map((left) => ({
        left,
        right: assignments[left],
      }));
      onAnswer(answer, isCorrect);
    } else {
      onAnswer(null, null);
    }
  }, [assignments, leftItems, onAnswer, isCorrect]);

  const usedRights = new Set(Object.values(assignments));
  const remainingRights = rightItems.filter((r) => !usedRights.has(r));

  return (
      <div className={"flex flex-col gap-8"}>
        <ExerciseHeader question={pair.request} topicId={pair.topicId} />

        <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
        >
          <div className="flex flex-col gap-3">
            {leftItems.map((left) => {
              const assigned = assignments[left];
              const leftCorrect = assigned === correctRightByLeft[left];
              return (
                  <DroppableLeft
                      key={left}
                      left={left}
                      assignedRight={assigned}
                      onRemove={() => assigned && removeRight(assigned)}
                      disabled={submitted}
                      status={submitted ? (leftCorrect ? "correct" : "incorrect") : null}
                  />
              );
            })}
          </div>

          <RightPool disabled={submitted}>
            {remainingRights.map((right) => (
                <RightChip key={right} id={right} value={right} disabled={submitted} />
            ))}
          </RightPool>
        </DndContext>
      </div>
  );
}

export default Pair;
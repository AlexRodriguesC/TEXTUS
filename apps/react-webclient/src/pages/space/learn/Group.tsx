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
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";
import type { GroupResponseDTO } from "@/client/model";
import { cn } from "@/lib/utils.ts";

interface GroupProps {
  group: GroupResponseDTO;
  submitted: boolean;
  onAnswer?: (answer: Record<string, string[]> | null, isCorrect: boolean | null) => void;
}

interface DroppableLabelProps {
  label: string;
  assignedKeys: string[];
  onRemove: (key: string) => void;
  disabled: boolean;
  status?: "correct" | "incorrect" | null;
}

function AssignedKeyChip({
                           id,
                           value,
                           onRemove,
                           disabled,
                         }: {
  id: string;
  value: string;
  onRemove: () => void;
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
              "rounded-md bg-secondary px-3 py-1 text-sm",
              !disabled && "cursor-grab touch-none hover:bg-destructive/20",
              isDragging && "opacity-50",
          )}
      >
        {value}
      </button>
  );
}

function DroppableLabel({
                          label,
                          assignedKeys,
                          onRemove,
                          disabled,
                          status,
                        }: DroppableLabelProps) {
  const { setNodeRef, isOver } = useDroppable({ id: `label-${label}`, disabled });

  return (
      <div
          ref={setNodeRef}
          className={cn(
              "flex flex-wrap items-center gap-2 rounded-md border p-3 min-h-14",
              isOver ? "border-primary bg-accent" : "border-border",
              status === "correct" && "border-primary bg-primary/10",
              status === "incorrect" && "border-destructive bg-destructive/10",
          )}
      >
        <span className="font-medium mr-2">{label}</span>
        {assignedKeys.map((key) => (
            <AssignedKeyChip
                key={key}
                id={key}
                value={key}
                onRemove={() => onRemove(key)}
                disabled={disabled}
            />
        ))}
      </div>
  );
}

function DraggableKey({ id, value, disabled }: { id: string; value: string; disabled: boolean }) {
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
          disabled={disabled}
          className={cn(
              "rounded-md border bg-background px-3 py-2 text-sm",
              !disabled && "cursor-grab touch-none",
              isDragging && "opacity-50",
          )}
      >
        {value}
      </button>
  );
}

function KeyPool({ children, disabled }: { children: React.ReactNode; disabled: boolean }) {
  const { setNodeRef, isOver } = useDroppable({ id: "key-pool", disabled });

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

function Group({ group, submitted, onAnswer }: GroupProps) {
  const labels = useMemo(() => Object.keys(group.groups ?? {}), [group.groups]);
  const keys = useMemo(
      () => Object.values(group.groups ?? {}).flat(),
      [group.groups],
  );

  const [assignments, setAssignments] = useState<Record<string, string[]>>({});

  const sensors = useSensors(useSensor(PointerSensor));

  function removeKey(key: string) {
    if (submitted) return;
    setAssignments((prev) => {
      const next: Record<string, string[]> = {};
      for (const l of Object.keys(prev)) {
        next[l] = prev[l].filter((k) => k !== key);
      }
      return next;
    });
  }

  function handleDragEnd(event: DragEndEvent) {
    if (submitted) return;
    const { active, over } = event;
    if (!over) return;

    const key = String(active.id);

    if (over.id === "key-pool") {
      removeKey(key);
      return;
    }

    const label = String(over.id).replace("label-", "");

    setAssignments((prev) => {
      const next: Record<string, string[]> = {};
      for (const l of Object.keys(prev)) {
        next[l] = prev[l].filter((k) => k !== key);
      }
      next[label] = [...(next[label] ?? []), key];
      return next;
    });
  }

  const isCorrect = useMemo(() => {
    return labels.every((label) => {
      const expected = new Set(group.groups[label] ?? []);
      const actual = new Set(assignments[label] ?? []);
      return (
          expected.size === actual.size &&
          [...expected].every((k) => actual.has(k))
      );
    });
  }, [assignments, labels, group.groups]);

  useEffect(() => {
    if (!onAnswer) return;

    const usedKeys = new Set(Object.values(assignments).flat());
    const allAssigned = keys.length > 0 && keys.every((k) => usedKeys.has(k));

    if (allAssigned) {
      const answer: Record<string, string[]> = {};
      for (const label of labels) {
        answer[label] = assignments[label] ?? [];
      }
      onAnswer(answer, isCorrect);
    } else {
      onAnswer(null, null);
    }
  }, [assignments, keys, labels, onAnswer, isCorrect]);

  const usedKeys = new Set(Object.values(assignments).flat());
  const remainingKeys = keys.filter((k) => !usedKeys.has(k));

  return (
      <div className={"flex flex-col gap-8"}>
        <ExerciseHeader question={group.request} topicId={group.topicId} />

        <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
        >
          <div className="flex flex-col gap-3">
            {labels.map((label) => {
              const expected = new Set(group.groups[label] ?? []);
              const actual = assignments[label] ?? [];
              const labelCorrect =
                  expected.size === actual.length &&
                  actual.every((k) => expected.has(k));
              return (
                  <DroppableLabel
                      key={label}
                      label={label}
                      assignedKeys={actual}
                      onRemove={removeKey}
                      disabled={submitted}
                      status={submitted ? (labelCorrect ? "correct" : "incorrect") : null}
                  />
              );
            })}
          </div>

          <KeyPool disabled={submitted}>
            {remainingKeys.map((key) => (
                <DraggableKey key={key} id={key} value={key} disabled={submitted} />
            ))}
          </KeyPool>
        </DndContext>
      </div>
  );
}

export default Group;
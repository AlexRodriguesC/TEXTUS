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
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";
import type GroupExercise from "@/classes/GroupExercise.ts";
import { cn } from "@/lib/utils.ts";
import * as React from "react";

interface GroupProps {
  group: GroupExercise;
  submitted: boolean;
  onAnswer?: (answer: Record<string, string[]> | null) => void;
}

interface KeyItem {
  id: string;
  value: string;
}

function resolveDropLabel(
  overId: string,
  assignments: Record<string, string[]>,
  labels: string[],
): string | "key-pool" | null {
  if (overId === "key-pool") return "key-pool";
  if (overId.startsWith("label-")) {
    const index = Number(overId.slice("label-".length));
    return labels[index] ?? null;
  }
  for (const label of labels) {
    if ((assignments[label] ?? []).includes(overId)) return label;
  }
  return null;
}

function AssignedKeyChip({
  id,
  value,
  onRemove,
  disabled,
  status,
}: {
  id: string;
  value: string;
  onRemove: () => void;
  disabled: boolean;
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
        "rounded-md px-3 py-1 text-sm",
        status === "correct" && "bg-primary/40 text-primary-foreground",
        status === "incorrect" &&
          "bg-destructive/40 text-destructive-foreground",
        status == null && "bg-secondary",
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
  index,
  assignedKeys,
  onRemove,
  disabled,
  getKeyStatus,
}: {
  label: string;
  index: number;
  assignedKeys: KeyItem[];
  onRemove: (id: string) => void;
  disabled: boolean;
  getKeyStatus: (id: string) => "correct" | "incorrect" | null;
}) {
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
        <AssignedKeyChip
          key={item.id}
          id={item.id}
          value={item.value}
          onRemove={() => onRemove(item.id)}
          disabled={disabled}
          status={getKeyStatus(item.id)}
        />
      ))}
    </div>
  );
}

function DraggableKey({
  id,
  value,
  disabled,
  status,
}: {
  id: string;
  value: string;
  disabled: boolean;
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
      disabled={disabled}
      className={cn(
        "rounded-md border px-3 py-2 text-sm",
        status === "correct" &&
          "border-primary bg-primary text-primary-foreground",
        status === "incorrect" &&
          "border-destructive bg-destructive text-destructive-foreground",
        status == null && "bg-background",
        !disabled && "cursor-grab touch-none",
        isDragging && "opacity-50",
      )}
    >
      {value}
    </button>
  );
}

function KeyPool({
  children,
  disabled,
}: {
  children: React.ReactNode;
  disabled: boolean;
}) {
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

  const keyItems = useMemo<KeyItem[]>(() => {
    const values = Object.values(group.groups ?? {}).flat();
    return values.map((value, i) => ({ id: `key-${i}`, value }));
  }, [group.groups]);

  const keyById = useMemo(() => {
    const map = new Map<string, string>();
    for (const item of keyItems) map.set(item.id, item.value);
    return map;
  }, [keyItems]);

  const [assignments, setAssignments] = useState<Record<string, string[]>>({});

  const sensors = useSensors(useSensor(PointerSensor));

  function notify(next: Record<string, string[]>) {
    if (!onAnswer) return;

    const usedIds = new Set(Object.values(next).flat());
    const allAssigned =
      keyItems.length > 0 && keyItems.every((item) => usedIds.has(item.id));

    if (allAssigned) {
      const answer: Record<string, string[]> = {};
      for (const label of labels) {
        answer[label] = (next[label] ?? []).map(
          (id) => keyById.get(id) as string,
        );
      }
      onAnswer(answer);
    } else {
      onAnswer(null);
    }
  }

  function removeKey(id: string) {
    if (submitted) return;
    setAssignments((prev) => {
      const next: Record<string, string[]> = {};
      for (const l of Object.keys(prev)) {
        next[l] = prev[l].filter((existingId) => existingId !== id);
      }
      notify(next);
      return next;
    });
  }

  function handleDragEnd(event: DragEndEvent) {
    if (submitted) return;
    const { active, over } = event;
    if (!over) return;

    const id = String(active.id);
    const overId = String(over.id);
    const target = resolveDropLabel(overId, assignments, labels);

    if (target === "key-pool") {
      removeKey(id);
      return;
    }
    if (target === null) return;

    setAssignments((prev) => {
      const next: Record<string, string[]> = {};
      for (const l of Object.keys(prev)) {
        next[l] = prev[l].filter((existingId) => existingId !== id);
      }
      next[target] = [...(next[target] ?? []), id];
      notify(next);
      return next;
    });
  }

  const usedIds = new Set(Object.values(assignments).flat());
  const remainingKeyItems = keyItems.filter((item) => !usedIds.has(item.id));

  function getAssignedKeyStatus(
    id: string,
    label: string,
  ): "correct" | "incorrect" | null {
    if (!submitted) return null;
    const assignedIds = assignments[label] ?? [];
    const remaining = [...(group.groups[label] ?? [])];
    for (const assignedId of assignedIds) {
      const value = keyById.get(assignedId) as string;
      const matchIndex = remaining.indexOf(value);
      if (assignedId === id) {
        return matchIndex >= 0 ? "correct" : "incorrect";
      }
      if (matchIndex >= 0) remaining.splice(matchIndex, 1);
    }
    return null;
  }

  return (
    <div className={"flex flex-col gap-8"}>
      <ExerciseHeader question={group.request} topicId={group.topicId} />

      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
      >
        <div className="flex flex-col gap-3">
          {labels.map((label, i: number) => {
            const assignedIds = assignments[label] ?? [];
            const assignedItems = assignedIds.map((id) => ({
              id,
              value: keyById.get(id) as string,
            }));
            return (
              <DroppableLabel
                key={label}
                label={label}
                index={i}
                assignedKeys={assignedItems}
                onRemove={removeKey}
                disabled={submitted}
                getKeyStatus={(itemId) => getAssignedKeyStatus(itemId, label)}
              />
            );
          })}
        </div>

        <KeyPool disabled={submitted}>
          {remainingKeyItems.map((item) => (
            <DraggableKey
              key={item.id}
              id={item.id}
              value={item.value}
              disabled={submitted}
              status={submitted ? "incorrect" : null}
            />
          ))}
        </KeyPool>
      </DndContext>
    </div>
  );
}

export default Group;

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
  onAnswer?: (answer: Record<string, string[]> | null) => void;
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
        status === "correct" && "bg-primary text-primary-foreground",
        status === "incorrect" && "bg-destructive text-destructive-foreground",
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
  assignedKeys,
  onRemove,
  disabled,
  getKeyStatus,
}: {
  label: string;
  assignedKeys: string[];
  onRemove: (key: string) => void;
  disabled: boolean;
  getKeyStatus: (key: string) => "correct" | "incorrect" | null;
}) {
  const { setNodeRef, isOver } = useDroppable({ id: `label-${label}`, disabled });

  return (
    <div
      ref={setNodeRef}
      className={cn(
        "flex flex-wrap items-center gap-2 rounded-md border p-3 min-h-14",
        isOver ? "border-primary bg-accent" : "border-border",
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
          status={getKeyStatus(key)}
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
        status === "correct" && "border-primary bg-primary text-primary-foreground",
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

  useEffect(() => {
    if (!onAnswer) return;

    const usedKeys = new Set(Object.values(assignments).flat());
    const allAssigned = keys.length > 0 && keys.every((k) => usedKeys.has(k));

    if (allAssigned) {
      const answer: Record<string, string[]> = {};
      for (const label of labels) {
        answer[label] = assignments[label] ?? [];
      }
      onAnswer(answer);
    } else {
      onAnswer(null);
    }
  }, [assignments, keys, labels, onAnswer]);

  const usedKeys = new Set(Object.values(assignments).flat());
  const remainingKeys = keys.filter((k) => !usedKeys.has(k));

  function getAssignedKeyStatus(key: string, label: string): "correct" | "incorrect" | null {
    if (!submitted) return null;
    const expected = new Set(group.groups[label] ?? []);
    return expected.has(key) ? "correct" : "incorrect";
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
          {labels.map((label) => {
            const actual = assignments[label] ?? [];
            return (
              <DroppableLabel
                key={label}
                label={label}
                assignedKeys={actual}
                onRemove={removeKey}
                disabled={submitted}
                getKeyStatus={(key) => getAssignedKeyStatus(key, label)}
              />
            );
          })}
        </div>

        <KeyPool disabled={submitted}>
          {remainingKeys.map((key) => (
            <DraggableKey
              key={key}
              id={key}
              value={key}
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

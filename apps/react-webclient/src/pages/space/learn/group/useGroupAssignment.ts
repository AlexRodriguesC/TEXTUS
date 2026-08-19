import {
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import { useCallback, useState } from "react";
import { removeIdFromAllSlots } from "@/pages/space/learn/dnd/assignmentUtils.ts";
import {
  buildGroupAnswer,
  resolveDropLabel,
} from "@/pages/space/learn/group/groupAssignmentUtils.ts";

export interface KeyItem {
  id: string;
  value: string;
}

interface UseGroupAssignmentOptions {
  labels: string[];
  keyItems: KeyItem[];
  keyById: Map<string, string>;
  submitted: boolean;
  onAnswer?: (answer: Record<string, string[]> | null) => void;
}

function useGroupAssignment({
  labels,
  keyItems,
  keyById,
  submitted,
  onAnswer,
}: UseGroupAssignmentOptions) {
  const [assignments, setAssignments] = useState<Record<string, string[]>>({});
  const sensors = useSensors(useSensor(PointerSensor));

  const notify = useCallback(
    (next: Record<string, string[]>) => {
      if (!onAnswer) return;

      const usedIds = new Set(Object.values(next).flat());
      const allAssigned =
        keyItems.length > 0 && keyItems.every((item) => usedIds.has(item.id));

      if (allAssigned) {
        onAnswer(buildGroupAnswer(next, labels, keyById));
      } else {
        onAnswer(null);
      }
    },
    [keyById, keyItems, labels, onAnswer],
  );

  const removeKey = useCallback(
    (id: string) => {
      if (submitted) return;
      setAssignments((prev) => {
        const next = removeIdFromAllSlots(prev, id);
        notify(next);
        return next;
      });
    },
    [notify, submitted],
  );

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
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
        const cleared = removeIdFromAllSlots(prev, id);
        const next = {
          ...cleared,
          [target]: [...(cleared[target] ?? []), id],
        };
        notify(next);
        return next;
      });
    },
    [assignments, labels, notify, removeKey, submitted],
  );

  const usedIds = new Set(Object.values(assignments).flat());
  const remainingKeyItems = keyItems.filter((item) => !usedIds.has(item.id));

  return {
    assignments,
    sensors,
    removeKey,
    handleDragEnd,
    remainingKeyItems,
  };
}

export default useGroupAssignment;

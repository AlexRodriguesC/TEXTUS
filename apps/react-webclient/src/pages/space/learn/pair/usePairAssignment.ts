import {
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import { useCallback, useState } from "react";
import type LeftRight from "@/classes/LeftRight.ts";
import { removeValueFromRecord } from "@/pages/space/learn/dnd/assignmentUtils.ts";

interface UsePairAssignmentOptions {
  leftItems: string[];
  rightItems: string[];
  submitted: boolean;
  onAnswer?: (answer: LeftRight[] | null) => void;
}

function usePairAssignment({
  leftItems,
  rightItems,
  submitted,
  onAnswer,
}: UsePairAssignmentOptions) {
  const [assignments, setAssignments] = useState<Record<string, string>>({});
  const sensors = useSensors(useSensor(PointerSensor));

  const notify = useCallback(
    (next: Record<string, string>) => {
      if (!onAnswer) return;

      const allAssigned =
        leftItems.length > 0 &&
        leftItems.every((left) => next[left] !== undefined);

      if (allAssigned) {
        const answer: LeftRight[] = leftItems.map((left) => ({
          left,
          right: next[left],
        }));
        onAnswer(answer);
      } else {
        onAnswer(null);
      }
    },
    [leftItems, onAnswer],
  );

  const removeRight = useCallback(
    (right: string) => {
      if (submitted) return;
      setAssignments((prev) => {
        const next = removeValueFromRecord(prev, right);
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

      const right = String(active.id);

      if (over.id === "right-pool") {
        removeRight(right);
        return;
      }

      const left = String(over.id).replace("left-", "");

      setAssignments((prev) => {
        const next = removeValueFromRecord(prev, right);
        next[left] = right;
        notify(next);
        return next;
      });
    },
    [notify, removeRight, submitted],
  );

  const usedRights = new Set(Object.values(assignments));
  const remainingRights = rightItems.filter((r) => !usedRights.has(r));

  return {
    assignments,
    sensors,
    removeRight,
    handleDragEnd,
    remainingRights,
  };
}

export default usePairAssignment;

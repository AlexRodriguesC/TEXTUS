import { useMemo } from "react";
import {
  DndContext,
  closestCenter,
} from "@dnd-kit/core";
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";
import type GroupExercise from "@/classes/GroupExercise.ts";
import DraggableChip from "@/pages/space/learn/dnd/DraggableChip.tsx";
import DroppablePool from "@/pages/space/learn/dnd/DroppablePool.tsx";
import GroupDroppableLabel from "@/pages/space/learn/group/GroupDroppableLabel.tsx";
import { getAssignedKeyStatus } from "@/pages/space/learn/group/groupAssignmentUtils.ts";
import useGroupAssignment from "@/pages/space/learn/group/useGroupAssignment.ts";

interface GroupProps {
  group: GroupExercise;
  submitted: boolean;
  onAnswer?: (answer: Record<string, string[]> | null) => void;
}

function Group({ group, submitted, onAnswer }: GroupProps) {
  const labels = useMemo(() => Object.keys(group.groups ?? {}), [group.groups]);

  const keyItems = useMemo(() => {
    const values = Object.values(group.groups ?? {}).flat();
    return values.map((value, i) => ({ id: `key-${i}`, value }));
  }, [group.groups]);

  const keyById = useMemo(() => {
    const map = new Map<string, string>();
    for (const item of keyItems) map.set(item.id, item.value);
    return map;
  }, [keyItems]);

  const {
    assignments,
    sensors,
    removeKey,
    handleDragEnd,
    remainingKeyItems,
  } = useGroupAssignment({
    labels,
    keyItems,
    keyById,
    submitted,
    onAnswer,
  });

  return (
    <div className={"flex flex-col gap-8"}>
      <ExerciseHeader question={group.request} topicId={group.topicId} />

      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
      >
        <div className="flex flex-col gap-3">
          {labels.map((label, i) => {
            const assignedIds = assignments[label] ?? [];
            const assignedItems = assignedIds.map((id) => ({
              id,
              value: keyById.get(id) as string,
            }));
            return (
              <GroupDroppableLabel
                key={label}
                label={label}
                index={i}
                assignedKeys={assignedItems}
                onRemove={removeKey}
                disabled={submitted}
                getKeyStatus={(itemId) =>
                  getAssignedKeyStatus(
                    itemId,
                    label,
                    submitted,
                    assignments,
                    group.groups,
                    keyById,
                  )
                }
              />
            );
          })}
        </div>

        <DroppablePool id="key-pool" disabled={submitted}>
          {remainingKeyItems.map((item) => (
            <DraggableChip
              key={item.id}
              id={item.id}
              value={item.value}
              disabled={submitted}
              status={submitted ? "incorrect" : null}
            />
          ))}
        </DroppablePool>
      </DndContext>
    </div>
  );
}

export default Group;

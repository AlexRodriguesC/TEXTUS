import { useMemo } from "react";
import { DndContext, closestCenter } from "@dnd-kit/core";
import type PairExercise from "@/classes/PairExercise.ts";
import type LeftRight from "@/classes/LeftRight.ts";
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";
import DraggableChip from "@/pages/space/learn/dnd/DraggableChip.tsx";
import DroppablePool from "@/pages/space/learn/dnd/DroppablePool.tsx";
import PairDroppableLeft from "@/pages/space/learn/pair/PairDroppableLeft.tsx";
import usePairAssignment from "@/pages/space/learn/pair/usePairAssignment.ts";

interface PairProps {
  pair: PairExercise;
  submitted: boolean;
  onAnswer?: (answer: LeftRight[] | null) => void;
}

function Pair({ pair, submitted, onAnswer }: PairProps) {
  const leftItems = useMemo(
    () => pair.pairs.map((p: LeftRight) => p.left),
    [pair.pairs],
  );
  const rightItems = useMemo(
    () => pair.pairs.map((p: LeftRight) => p.right),
    [pair.pairs],
  );

  const correctRightByLeft = useMemo(() => {
    const map: Record<string, string> = {};
    for (const p of pair.pairs) map[p.left] = p.right;
    return map;
  }, [pair.pairs]);

  const {
    assignments,
    sensors,
    removeRight,
    handleDragEnd,
    remainingRights,
  } = usePairAssignment({
    leftItems,
    rightItems,
    submitted,
    onAnswer,
  });

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
              <PairDroppableLeft
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

        <DroppablePool
          id="right-pool"
          disabled={submitted}
          isOverClassName="bg-primary/40"
        >
          {remainingRights.map((right: string) => (
            <DraggableChip
              key={right}
              id={right}
              value={right}
              disabled={submitted}
              status={submitted ? "incorrect" : null}
              className={submitted ? undefined : "bg-primary/20"}
            />
          ))}
        </DroppablePool>
      </DndContext>
    </div>
  );
}

export default Pair;

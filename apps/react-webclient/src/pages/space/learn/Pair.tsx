import { useState } from "react";
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
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";

interface PairProps {
    pair: PairResponseDTO;
}

function DroppableLeft({ left, assignedRight, onRemove }: { left: string; assignedRight: string | undefined; onRemove: () => void }) {
    const { setNodeRef, isOver } = useDroppable({ id: `left-${left}` });

    return (
        <div
            ref={setNodeRef}
            className={`flex items-center justify-between rounded-md border p-3 min-h-14 ${
                isOver ? "border-primary bg-accent" : "border-border"
            }`}
        >
            <span className="font-medium">{left}</span>
            {assignedRight && <RightChip id={assignedRight} value={assignedRight} onRemove={onRemove} />}
        </div>
    );
}

function RightChip({ id, value, onRemove }: { id: string; value: string; onRemove?: () => void }) {
    const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({ id });

    const style = transform
        ? { transform: `translate3d(${transform.x}px, ${transform.y}px, 0)` }
        : undefined;

    return (
        <button
            ref={setNodeRef}
            {...attributes}
            {...listeners}
            style={style}
            onClick={onRemove}
            className={`cursor-grab touch-none rounded-md border bg-background px-3 py-1 text-sm ${
                onRemove ? "hover:bg-destructive/20" : ""
            } ${isDragging ? "opacity-50" : ""}`}
        >
            {value}
        </button>
    );
}

function RightPool({ children }: { children: React.ReactNode }) {
    const { setNodeRef, isOver } = useDroppable({ id: "right-pool" });

    return (
        <div
            ref={setNodeRef}
            className={`flex flex-wrap gap-2 pt-4 min-h-14 rounded-md ${isOver ? "bg-accent/50" : ""}`}
        >
            {children}
        </div>
    );
}

function Pair({ pair }: PairProps) {
    const leftItems = pair.left ?? [];
    const rightItems = pair.right ?? [];

    const [assignments, setAssignments] = useState<Record<string, string>>({});

    const sensors = useSensors(useSensor(PointerSensor));

    function removeRight(right: string) {
        setAssignments((prev) => {
            const next: Record<string, string> = {};
            for (const l of Object.keys(prev)) {
                if (prev[l] !== right) next[l] = prev[l];
            }
            return next;
        });
    }

    function handleDragEnd(event: DragEndEvent) {
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

    const usedRights = new Set(Object.values(assignments));
    const remainingRights = rightItems.filter((r) => !usedRights.has(r));

    return (
        <div className={"flex flex-col gap-8"}>
           <ExerciseHeader question={pair.request} topicId={pair.topicId} />

            <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
                <div className="flex flex-col gap-3">
                    {leftItems.map((left) => (
                        <DroppableLeft
                            key={left}
                            left={left}
                            assignedRight={assignments[left]}
                            onRemove={() => removeRight(assignments[left])}
                        />
                    ))}
                </div>

                <RightPool>
                    {remainingRights.map((right) => (
                        <RightChip key={right} id={right} value={right} />
                    ))}
                </RightPool>
            </DndContext>
        </div>
    );
}

export default Pair;
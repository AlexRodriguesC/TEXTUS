import { useEffect, useState } from "react";
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
import type {GroupResponseDTO} from "@/client/model";

interface GroupProps {
    group: GroupResponseDTO;
    onAnswer?: (answer: Record<string, string[]> | null) => void;
}

interface DroppableLabelProps {
    label: string;
    assignedKeys: string[];
    onRemove: (key: string) => void;
}

function AssignedKeyChip({ id, value, onRemove }: { id: string; value: string; onRemove: () => void }) {
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
            className={`cursor-grab touch-none rounded-md bg-secondary px-3 py-1 text-sm hover:bg-destructive/20 ${
                isDragging ? "opacity-50" : ""
            }`}
        >
            {value}
        </button>
    );
}

function DroppableLabel({ label, assignedKeys, onRemove }: DroppableLabelProps) {
    const { setNodeRef, isOver } = useDroppable({ id: `label-${label}` });

    return (
        <div
            ref={setNodeRef}
            className={`flex flex-wrap items-center gap-2 rounded-md border p-3 min-h-14 ${
                isOver ? "border-primary bg-accent" : "border-border"
            }`}
        >
            <span className="font-medium mr-2">{label}</span>
            {assignedKeys.map((key) => (
                <AssignedKeyChip key={key} id={key} value={key} onRemove={() => onRemove(key)} />
            ))}
        </div>
    );
}

function DraggableKey({ id, value }: { id: string; value: string }) {
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
            className={`cursor-grab touch-none rounded-md border bg-background px-3 py-2 text-sm ${
                isDragging ? "opacity-50" : ""
            }`}
        >
            {value}
        </button>
    );
}

function KeyPool({ children }: { children: React.ReactNode }) {
    const { setNodeRef, isOver } = useDroppable({ id: "key-pool" });

    return (
        <div
            ref={setNodeRef}
            className={`flex flex-wrap gap-2 pt-4 min-h-14 rounded-md ${
                isOver ? "bg-accent/50" : ""
            }`}
        >
            {children}
        </div>
    );
}

function Group({ group, onAnswer }: GroupProps) {
    const labels = group.labels ?? [];
    const keys = group.keys ?? [];

    const [assignments, setAssignments] = useState<Record<string, string[]>>({});

    const sensors = useSensors(useSensor(PointerSensor));

    function removeKey(key: string) {
        setAssignments((prev) => {
            const next: Record<string, string[]> = {};
            for (const l of Object.keys(prev)) {
                next[l] = prev[l].filter((k) => k !== key);
            }
            return next;
        });
    }

    function handleDragEnd(event: DragEndEvent) {
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

    return (
        <div className={"flex flex-col gap-8"}>
            <ExerciseHeader question={group.request} topicId={group.topicId}/>

            <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
                <div className="flex flex-col gap-3">
                    {labels.map((label) => (
                        <DroppableLabel
                            key={label}
                            label={label}
                            assignedKeys={assignments[label] ?? []}
                            onRemove={removeKey}
                        />
                    ))}
                </div>

                <KeyPool>
                    {remainingKeys.map((key) => (
                        <DraggableKey key={key} id={key} value={key} />
                    ))}
                </KeyPool>
            </DndContext>
        </div>
    );
}

export default Group;
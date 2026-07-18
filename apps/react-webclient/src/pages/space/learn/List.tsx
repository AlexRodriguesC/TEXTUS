import { useEffect, useState } from "react";
import {
    DndContext,
    closestCenter,
    PointerSensor,
    useSensor,
    useSensors,
    type DragEndEvent,
} from "@dnd-kit/core";
import {
    SortableContext,
    verticalListSortingStrategy,
    useSortable,
    arrayMove,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { GripVertical } from "lucide-react";
import type {ListResponseDTO} from "@/client/model";
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";

interface ListProps {
    list: ListResponseDTO;
    onAnswer?: (answer: string[] | null) => void;
}

interface SortableItemProps {
    id: string;
    value: string;
}

function SortableItem({ id, value }: SortableItemProps) {
    const { attributes, listeners, setNodeRef, transform, transition, isDragging } =
        useSortable({ id });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.5 : 1,
    };

    return (
        <div
            ref={setNodeRef}
            style={style}
            className="flex items-center gap-2 rounded-md border bg-background p-3"
        >
            <button
                {...attributes}
                {...listeners}
                className="cursor-grab touch-none text-muted-foreground"
            >
                <GripVertical className="h-4 w-4" />
            </button>
            <span>{value}</span>
        </div>
    );
}

function List({ list, onAnswer }: ListProps) {
    const [items, setItems] = useState(
        list.answer.map((value, i) => ({ id: `${i}-${value}`, value }))
    );

    const sensors = useSensors(useSensor(PointerSensor));

    function handleDragEnd(event: DragEndEvent) {
        const { active, over } = event;
        if (!over || active.id === over.id) return;

        setItems((prev) => {
            const oldIndex = prev.findIndex((i) => i.id === active.id);
            const newIndex = prev.findIndex((i) => i.id === over.id);
            return arrayMove(prev, oldIndex, newIndex);
        });
    }

    useEffect(() => {
        if (!onAnswer) return;
        onAnswer(items.map((i) => i.value));
    }, [items, onAnswer]);

    return (
        <div className={"flex flex-col gap-8"}>
            <ExerciseHeader question={list.request} topicId={list.topicId}/>
            <DndContext
                sensors={sensors}
                collisionDetection={closestCenter}
                onDragEnd={handleDragEnd}
            >
                <SortableContext
                    items={items.map((i) => i.id)}
                    strategy={verticalListSortingStrategy}
                >
                    <div className="flex flex-col gap-2">
                        {items.map((item) => (
                            <SortableItem key={item.id} id={item.id} value={item.value} />
                        ))}
                    </div>
                </SortableContext>
            </DndContext>
        </div>
    );
}

export default List;
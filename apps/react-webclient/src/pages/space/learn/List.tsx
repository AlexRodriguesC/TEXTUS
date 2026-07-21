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
import type { ListResponseDTO } from "@/client/model";
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";
import { cn } from "@/lib/utils.ts";

interface ListProps {
  list: ListResponseDTO;
  submitted: boolean;
  onAnswer?: (answer: string[] | null) => void;
}

interface SortableItemProps {
  id: string;
  value: string;
  disabled: boolean;
  status?: "correct" | "incorrect" | null;
}

function SortableItem({ id, value, disabled, status }: SortableItemProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id, disabled });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        "flex items-center gap-2 rounded-md border p-3",
        status === "correct" &&
          "border-primary bg-primary text-primary-foreground",
        status === "incorrect" &&
          "border-destructive bg-destructive text-destructive-foreground",
        status == null && "bg-background",
      )}
    >
      {!disabled && (
        <button
          {...attributes}
          {...listeners}
          className="cursor-grab touch-none text-muted-foreground"
        >
          <GripVertical className="h-4 w-4" />
        </button>
      )}
      <span>{value}</span>
    </div>
  );
}

function List({ list, submitted, onAnswer }: ListProps) {
  const [items, setItems] = useState(() => {
    const arr = list.answer.map((value, i) => ({ id: `${i}-${value}`, value }));
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [arr[i], arr[j]] = [arr[j], arr[i]];
    }
    return arr;
  });

  const sensors = useSensors(useSensor(PointerSensor));

  function handleDragEnd(event: DragEndEvent) {
    if (submitted) return;
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
      <ExerciseHeader question={list.request} topicId={list.topicId} />
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
            {items.map((item, index) => {
              const isCorrect = item.value === list.answer[index];
              return (
                <SortableItem
                  key={item.id}
                  id={item.id}
                  value={item.value}
                  disabled={submitted}
                  status={
                    submitted ? (isCorrect ? "correct" : "incorrect") : null
                  }
                />
              );
            })}
          </div>
        </SortableContext>
      </DndContext>
    </div>
  );
}

export default List;

import MotionButton from "@/components/MotionButton.tsx";
import * as React from "react";
import type Lesson from "@/classes/Lesson.ts";
import LessonType from "@/classes/LessonType.ts";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover.tsx";
import PathNodePopover from "@/pages/space/learn/PathNodePopover.tsx";

function PathNode({
  node,
  offset,
  disabled,
}: {
  node: Lesson;
  offset: number;
  disabled?: boolean;
}) {
  const [open, setOpen] = React.useState(false);

  const wrappedOnClick = () => {
    if (disabled) {
      setOpen((prev) => !prev);
      return;
    }
  };

  const onOpenChange = (next: boolean) => {
    if (disabled) {
      setOpen(false);
      return;
    }
    setOpen(next);
  };

  return (
    <div
      className={"flex justify-center"}
      style={{ transform: `translateX(${offset}px)` }}
    >
      <Popover open={open} onOpenChange={onOpenChange}>
        <PopoverTrigger asChild>
          <MotionButton disabled={disabled} onClick={wrappedOnClick}>
            {getNodeIcon(node.type)}
          </MotionButton>
        </PopoverTrigger>
        <PopoverContent side="bottom" className="bg-muted/80 py-4 px-10 w-fit">
          <PathNodePopover lessonId={node.id} />
        </PopoverContent>
      </Popover>
    </div>
  );
}

export default PathNode;

function getNodeIcon(type: Lesson["type"]): React.ReactNode {
  switch (type) {
    case LessonType.CHECKPOINT:
      return (
        <img className="w-fit h-fit" src="/checkpoint.webp" alt="trophy" />
      );
    case LessonType.LESSON:
      return <img src="/lesson.webp" alt="star" />;
  }
}

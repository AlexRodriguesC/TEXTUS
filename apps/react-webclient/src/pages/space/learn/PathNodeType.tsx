import MotionButton from "@/components/MotionButton.tsx";
import * as React from "react";
import type { LessonResponseDTO } from "@/client/model";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover.tsx";
import PathNodePopover from "@/pages/space/learn/PathNodePopover.tsx";

function PathNode({ node, offset, disabled }: { node: LessonResponseDTO; offset: number; disabled?: boolean }) {
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
  }

  return (
      <div className={"flex justify-center"} style={{ transform: `translateX(${offset}px)` }}>
        <Popover open={open} onOpenChange={onOpenChange}>
          <PopoverTrigger asChild>
            <MotionButton disabled={disabled} onClick={wrappedOnClick}>
              {getNodeIcon(node.type)}
            </MotionButton>
          </PopoverTrigger>
          <PopoverContent side="bottom" className="bg-muted/80">
            <PathNodePopover lessonId={node.id}/>
          </PopoverContent>
        </Popover>
      </div>
  );
}

export default PathNode;

function getNodeIcon(type: LessonResponseDTO["type"]): React.ReactNode {
  switch (type) {
    case "CHECKPOINT":
      return <img className="w-fit h-fit" src="/checkpoint.webp" alt="trophy" />;
    case "LESSON":
      return <img src="/lesson.webp" alt="star" />;
  }
}
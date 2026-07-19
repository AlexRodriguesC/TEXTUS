import MotionButton from "@/components/MotionButton.tsx";
import * as React from "react";
import type { LessonResponseDTO } from "@/client/model";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover.tsx";
import PathNodePopover from "@/pages/space/learn/PathNodePopover.tsx";

function PathNode({ node, offset, onClick, disabled }: { node: LessonResponseDTO; offset: number; onClick: () => void; disabled?: boolean }) {
  const [open, setOpen] = React.useState(false);

  const wrappedOnClick = () => {
    if (disabled) {
      setOpen((prev) => !prev);
      return;
    }
    //onClick();
  };

  return (
      <div className={"flex justify-center"} style={{ transform: `translateX(${offset}px)` }}>
        <Popover open={open} onOpenChange={setOpen}>
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
      return <img className="w-fit h-fit" src="/checkpoint.png" alt="trophy" />;
    case "LESSON":
      return <img src="/lesson.png" alt="star" />;
  }
}
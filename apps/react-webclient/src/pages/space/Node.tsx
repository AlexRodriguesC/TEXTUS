import * as React from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import MotionButton from "@/components/MotionButton.tsx";
import TitleSummarySection from "@/components/TitleSummarySection.tsx";

interface NodeProps {
  completed: boolean;
  title: string;
  summary: string;
  children?: React.ReactNode;
}

function Node({ completed = false, title, summary, children }: NodeProps) {
  return (
    <Dialog>
      <DialogTrigger asChild>
        <MotionButton completed={completed}>{children}</MotionButton>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <TitleSummarySection title={title} summary={summary} />
        </DialogHeader>
      </DialogContent>
    </Dialog>
  );
}

export default Node;

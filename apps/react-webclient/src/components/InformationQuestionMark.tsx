import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip.tsx";
import { CircleQuestionMark } from "lucide-react";

interface InformationQuestionMarkProps {
  tooltip: string;
}

function InformationQuestionMark({ tooltip }: InformationQuestionMarkProps) {
  return (
    <Tooltip>
      <TooltipTrigger>
        <CircleQuestionMark />
      </TooltipTrigger>
      <TooltipContent>{tooltip}</TooltipContent>
    </Tooltip>
  );
}

export default InformationQuestionMark;

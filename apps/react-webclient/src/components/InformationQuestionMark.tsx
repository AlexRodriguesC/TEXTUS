import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip.tsx";
import { CircleQuestionMark } from "lucide-react";

interface InformationQuestionMarkProps {
  tooltip: string;
  onClick?: () => void;
  size?: number
}

function InformationQuestionMark({ tooltip, onClick, size }: InformationQuestionMarkProps) {
  return (
    <Tooltip>
      <TooltipTrigger>
        <CircleQuestionMark onClick={onClick} size={size} />
      </TooltipTrigger>
      <TooltipContent>{tooltip}</TooltipContent>
    </Tooltip>
  );
}

export default InformationQuestionMark;

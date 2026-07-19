import { forwardRef, useState } from "react";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip.tsx";
import { CircleQuestionMark } from "lucide-react";

interface InformationQuestionMarkProps
    extends React.ComponentPropsWithoutRef<typeof CircleQuestionMark> {
  tooltip: string;
}

const InformationQuestionMark = forwardRef<
    SVGSVGElement,
    InformationQuestionMarkProps
>(({ tooltip, onClick, size, ...props }, ref) => {
  const [open, setOpen] = useState(false);

  return (
      <Tooltip open={open} onOpenChange={setOpen}>
        <TooltipTrigger asChild>
          <CircleQuestionMark
              ref={ref}
              size={size}
              onClick={(e) => {
                e?.stopPropagation();
                setOpen((prev) => !prev);
                onClick?.(e);
              }}
              {...props}
          />
        </TooltipTrigger>
        <TooltipContent>{tooltip}</TooltipContent>
      </Tooltip>
  );
});

InformationQuestionMark.displayName = "InformationQuestionMark";

export default InformationQuestionMark;
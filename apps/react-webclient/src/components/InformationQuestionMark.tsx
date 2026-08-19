import { forwardRef, useState } from "react";
import { HintOverlay } from "@/components/HintOverlay.tsx";
import { CircleQuestionMark } from "lucide-react";

interface InformationQuestionMarkProps extends React.ComponentPropsWithoutRef<
  typeof CircleQuestionMark
> {
  tooltip: string;
}

const InformationQuestionMark = forwardRef<
  SVGSVGElement,
  InformationQuestionMarkProps
>(({ tooltip, onClick, size, ...props }, ref) => {
  const [open, setOpen] = useState(false);

  return (
    <HintOverlay
      open={open}
      onOpenChange={setOpen}
      trigger={
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
      }
      content={tooltip}
    />
  );
});

InformationQuestionMark.displayName = "InformationQuestionMark";

export default InformationQuestionMark;

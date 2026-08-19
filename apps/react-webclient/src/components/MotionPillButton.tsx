import { cn } from "@/lib/utils.ts";
import { motion } from "framer-motion";
import { forwardRef, useMemo, type PropsWithChildren, type ReactNode } from "react";
import useLearningPath from "@/hooks/learning/useLearningPath.ts";
import {
  isPageFullyMastered,
  isTopicInMasteredLesson,
} from "@/hooks/learning/learningPathUtils.ts";
import useTopics from "@/hooks/useTopics.tsx";

interface MotionPillButtonProps extends PropsWithChildren {
  completed?: boolean;
  entityId?: number;
  isPage?: boolean;
  disabled?: boolean;
  className?: string;
  button?: ReactNode;
}

const MotionPillButton = forwardRef<HTMLDivElement, MotionPillButtonProps>(
  function MotionPillButton(
    {
      children,
      completed = false,
      entityId,
      isPage = false,
      disabled,
      className,
      button,
      ...props
    },
    ref,
  ) {
    const { path } = useLearningPath();
    const { topics } = useTopics();

    const mastered = useMemo(() => {
      if (entityId === undefined || !path || path.chapters.length === 0) {
        return false;
      }
      if (isPage) {
        return isPageFullyMastered(path, entityId, topics);
      }
      return isTopicInMasteredLesson(path, entityId);
    }, [entityId, isPage, path, topics]);

    const isSettled = completed || mastered;

    return (
      <motion.div
        ref={ref}
        data-slot="button"
        whileHover={disabled ? undefined : { scale: isSettled ? 1.0 : 1.1 }}
        whileTap={
          disabled
            ? undefined
            : { scale: 0.95, y: 4, boxShadow: "0 0 0 var(--color-ring)" }
        }
        transition={{ type: "spring", stiffness: 300, damping: 10 }}
        className={cn(
          "group relative rounded-full border-3",
          "bg-card text-primary-foreground font-extrabold",
          mastered
            ? "shadow-[0_3px_0_theme(colors.green.500)]"
            : "shadow-[0_3px_0_var(--color-ring)]",
          "transition-shadow duration-75",
          mastered ? "border-green-500" : "border-primary",
          "h-9 shrink-0 w-full max-w-[172px]",
          "flex items-center",
          button ? "justify-between gap-0.5 py-1.5 pl-3 pr-1.5" : "justify-center px-3 py-1.5",
          "text-xs",
          disabled
            ? "cursor-not-allowed opacity-50 grayscale"
            : "cursor-pointer",
          className,
        )}
        {...props}
      >
        {children}
        {button ? (
          <span
            className="flex shrink-0 items-center justify-center opacity-50 transition-opacity group-hover:opacity-100 group-focus-within:opacity-100"
            data-testid="pill-trailing-control"
          >
            {button}
          </span>
        ) : null}
      </motion.div>
    );
  },
);

export default MotionPillButton;

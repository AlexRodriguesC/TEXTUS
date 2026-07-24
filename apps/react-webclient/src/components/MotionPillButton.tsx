import { cn } from "@/lib/utils.ts";
import { motion } from "framer-motion";
import { forwardRef, useMemo } from "react";
import type { PropsWithChildren, Ref } from "react";
import { useTheme } from "@/hooks/context/useTheme.ts";
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
  onClick?: () => void;
  disabled?: boolean;
  className?: string;
  textRef?: Ref<HTMLSpanElement>;
}

const MotionPillButton = forwardRef<HTMLButtonElement, MotionPillButtonProps>(
  function MotionPillButton(
    {
      children,
      completed = false,
      entityId,
      isPage = false,
      onClick = () => {},
      disabled,
      className,
      textRef,
      ...props
    },
    ref,
  ) {
    const { theme } = useTheme();
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
      <motion.button
        ref={ref}
        onClick={onClick}
        data-slot="button"
        whileHover={disabled ? undefined : { scale: isSettled ? 1.0 : 1.1 }}
        whileTap={
          disabled
            ? undefined
            : { scale: 0.95, y: 4, boxShadow: "0 0 0 var(--color-ring)" }
        }
        transition={{ type: "spring", stiffness: 300, damping: 10 }}
        className={cn(
          "relative rounded-full border-3",
          "bg-card text-primary-foreground font-extrabold",
          mastered
            ? "shadow-[0_3px_0_theme(colors.green.500)]"
            : "shadow-[0_3px_0_var(--color-ring)]",
          "transition-shadow duration-75",
          mastered ? "border-green-500" : "border-primary",
          "h-9 shrink-0 w-full max-w-[172px]",
          "px-3 py-1.5",
          "flex items-center justify-center",
          "text-xs",
          disabled
            ? "cursor-not-allowed opacity-50 grayscale"
            : "cursor-pointer",
          className,
        )}
        {...props}
      >
        <span
          ref={textRef}
          className={cn("block w-full truncate text-center", theme === "dark" ? "text-primary-foreground" : "text-secondary-foreground")}
        >
          {children}
        </span>
      </motion.button>
    );
  },
);

export default MotionPillButton;

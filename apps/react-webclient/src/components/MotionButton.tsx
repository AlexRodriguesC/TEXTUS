import { cn } from "@/lib/utils.ts";
import { motion } from "framer-motion";
import { forwardRef } from "react";
import type { PropsWithChildren } from "react";

interface MotionButtonProps extends PropsWithChildren {
  completed?: boolean;
  onClick?: () => void;
  disabled?: boolean;
}

const MotionButton = forwardRef<HTMLButtonElement, MotionButtonProps>(
    function MotionButton(
        { children, completed = false, onClick = () => {}, disabled, ...props },
        ref,
    ) {
      return (
          <motion.button
              ref={ref}
              onClick={onClick}
              data-slot="button"
              whileHover={disabled ? undefined : { scale: completed ? 1.0 : 1.1 }}
              whileTap={
                disabled
                    ? undefined
                    : { scale: 0.95, y: 4, boxShadow: "0 0 0 var(--color-ring)" }
              }
              transition={{ type: "spring", stiffness: 300, damping: 10 }}
              className={cn(
                  "relative rounded-full border-3",
                  "bg-card text-primary-foreground font-extrabold",
                  "shadow-[0_3px_0_var(--color-ring)]",
                  "transition-shadow duration-75",
                  "border-primary",
                  "size-12",
                  "flex items-center justify-center",
                  "shrink-0",
                  "p-1",
                  "text-sm",
                  "overflow-hidden",
                  disabled
                      ? "cursor-not-allowed opacity-50 grayscale"
                      : "cursor-pointer",
              )}
              {...props}
          >
        <span className={cn("flex items-center justify-center w-full h-full")}>
          {children}
        </span>
          </motion.button>
      );
    },
);

export default MotionButton;
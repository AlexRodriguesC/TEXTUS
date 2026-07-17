import {cn} from "@/lib/utils.ts";
import {motion} from "framer-motion";
import type {PropsWithChildren} from "react";

interface MotionButtonProps extends PropsWithChildren {
    completed?: boolean;
    onClick?: () => void;
}

function MotionButton({ children, completed = false, onClick = () => {} }: MotionButtonProps) {
  return (
    <motion.button
        onClick={onClick}
      data-slot="button"
      whileHover={{ scale: completed ? 1.0 : 1.1 }}
      transition={{ type: "spring", stiffness: 0, damping: 10 }}
      className={cn(
        "relative cursor-pointer rounded-full border-3",
        "bg-card text-primary-foreground font-extrabold",
        "shadow-[0_3px_0_var(--color-ring)]",
        "active:shadow-none active:translate-y-1",
        "transition-[box-shadow,transform] duration-75",
        "border-primary",
        "size-12",
        "flex items-center justify-center",
        "shrink-0",
        "p-1",
        "text-sm",
        "overflow-hidden",
      )}
    >
      <span className={cn("flex items-center justify-center w-full h-full")}>
        {children}
      </span>
    </motion.button>
  )
}

export default MotionButton;

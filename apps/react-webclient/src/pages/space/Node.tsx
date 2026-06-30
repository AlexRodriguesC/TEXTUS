import { motion } from "framer-motion";
import { cn } from "@/lib/utils.ts";
import * as React from "react";

interface NodeProps {
  completed: boolean;
  children?: React.ReactNode;
}

function Node({ completed = false, children }: NodeProps) {
  return (
    <motion.button
      data-slot="button"
      whileHover={{ scale: completed ? 1.0 : 1.1 }}
      transition={{ type: "spring", stiffness: 0, damping: 10 }}
      className={cn(
        "relative cursor-pointer rounded-full border-3 p-4",
        "bg-card text-primary-foreground font-extrabold",
        "shadow-[0_3px_0_var(--color-ring)]",
        "active:shadow-none active:translate-y-[4px]",
        "transition-[box-shadow,transform] duration-75",
        completed ? "border-green-500" : "border-primary",
      )}
    >
      {children}
    </motion.button>
  );
}

export default Node;

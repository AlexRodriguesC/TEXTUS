import { motion } from "framer-motion";
import { cn } from "@/lib/utils.ts";
import * as React from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import { Separator } from "@/components/ui/separator.tsx";

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
            "size-12",
            "flex items-center justify-center",
            "shrink-0",
            "p-0",
            "text-sm",
          )}
        >
          {children}
        </motion.button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <div className={"flex flex-col gap-4"}>
            <p className={"text-muted-foreground text-xl font-bold uppercase"}>
              title
            </p>
            <>{title}</>
            <Separator orientation={"horizontal"} />
            <p className={"text-muted-foreground text-xl font-bold uppercase"}>
              summary
            </p>
            <>{summary}</>
          </div>
        </DialogHeader>
      </DialogContent>
    </Dialog>
  );
}

export default Node;

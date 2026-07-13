import {RightSidebarOpenContext} from "@/context/RightSidebarOpenContext.tsx";
import { type ReactNode, useState } from "react";

function RightSidebarOpenProvider({ children }: { children: ReactNode }) {
  const [open, setOpen] = useState(false);
  return (
    <RightSidebarOpenContext.Provider value={{ open, setOpen }}>
      {children}
    </RightSidebarOpenContext.Provider>
  );
}

export default RightSidebarOpenProvider;

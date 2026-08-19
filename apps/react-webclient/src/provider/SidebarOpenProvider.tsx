import { SidebarOpenContext } from "@/context/SidebarOpenContext";
import { type ReactNode, useState } from "react";

function SidebarOpenProvider({ children }: { children: ReactNode }) {
  const [open, setOpen] = useState(false);
  return (
    <SidebarOpenContext.Provider value={{ open, setOpen }}>
      {children}
    </SidebarOpenContext.Provider>
  );
}

export default SidebarOpenProvider;

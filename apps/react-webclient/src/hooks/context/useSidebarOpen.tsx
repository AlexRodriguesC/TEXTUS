import { SidebarOpenContext } from "@/context/SidebarOpenContext.tsx";
import { useContext } from "react";

function useSidebarOpen() {
  const ctx = useContext(SidebarOpenContext);
  if (!ctx)
    throw new Error("useSidebarOpen must be used within SidebarOpenProvider");
  return ctx;
}

export default useSidebarOpen;

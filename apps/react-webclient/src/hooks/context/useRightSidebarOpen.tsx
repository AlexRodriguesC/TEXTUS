import { useContext } from "react";
import { RightSidebarOpenContext } from "@/context/RightSidebarOpenContext.tsx";

function useRightSidebarOpen() {
  const ctx = useContext(RightSidebarOpenContext);
  if (!ctx)
    throw new Error(
      "useRightSidebarOpen must be used within RightSidebarOpenProvider",
    );
  return ctx;
}

export default useRightSidebarOpen;

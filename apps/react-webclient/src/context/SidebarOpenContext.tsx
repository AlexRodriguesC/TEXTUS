import { createContext } from "react";

interface SidebarOpenContextValue {
  open: boolean;
  setOpen: (open: boolean) => void;
}

export const SidebarOpenContext = createContext<
  SidebarOpenContextValue | undefined
>(undefined);

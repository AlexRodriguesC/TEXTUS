import { createContext } from "react";

interface RightSidebarOpenContextValue {
    open: boolean;
    setOpen: (open: boolean) => void;
}

export const RightSidebarOpenContext = createContext<
    RightSidebarOpenContextValue | undefined
>(undefined);

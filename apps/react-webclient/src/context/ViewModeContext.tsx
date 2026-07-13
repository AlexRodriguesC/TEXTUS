import {createContext} from "react";

export type ViewMode = "menu" | "chat" | "mind-map" | "learning";

interface ViewModeContextValue {
    viewMode: ViewMode;
    setViewMode: (mode: ViewMode) => void;
}

const ViewModeContext = createContext<ViewModeContextValue | undefined>(undefined);

export default ViewModeContext;

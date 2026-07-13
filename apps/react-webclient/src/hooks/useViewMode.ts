import {useContext} from "react";
import ViewModeContext from "@/context/ViewModeContext.tsx";

function useViewMode() {
    const ctx = useContext(ViewModeContext);
    if (!ctx) throw new Error("useViewMode must be used within a ViewModeProvider");
    return ctx;
}

export default useViewMode;

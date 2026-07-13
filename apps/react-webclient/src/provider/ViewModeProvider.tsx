import ViewModeContext, {type ViewMode} from "@/context/ViewModeContext";
import {useState} from "react";
import * as React from "react";

function ViewModeProvider({ children }: { children: React.ReactNode }) {
  const [viewMode, setViewMode] = useState<ViewMode>("chat");

  return (
    <ViewModeContext.Provider value={{ viewMode, setViewMode }}>
      {children}
    </ViewModeContext.Provider>
  );
}

export default ViewModeProvider;

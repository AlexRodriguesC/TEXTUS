import {useContext} from "react";
import {TutorialContext} from "@/context/TutorialContext.tsx";

function useTutorial() {
    const ctx = useContext(TutorialContext);
    if (!ctx)
        throw new Error("useTutorial must be used within TutorialContextProvider");
    return ctx;
}

export default useTutorial;
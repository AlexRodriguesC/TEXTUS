import {type ReactNode, useCallback, useState} from "react";
import {TutorialContext} from "@/context/TutorialContext.tsx";

const TUTORIAL_DISABLED = "TUTORIAL_DISABLED";

function TutorialContextProvider({ children }: { children: ReactNode }) {
    const [tutorialEnabled, setTutorialEnabled] = useState(!!localStorage.getItem(TUTORIAL_DISABLED));

    const enableTutorial = useCallback(() => {
        localStorage.removeItem(TUTORIAL_DISABLED);
        setTutorialEnabled(true);
    }, [])

    const disableTutorial = useCallback(() => {
        localStorage.setItem(TUTORIAL_DISABLED, "");
        setTutorialEnabled(false);
    }, [])

    return (
        <TutorialContext.Provider value={{tutorialEnabled: tutorialEnabled, enableTutorial, disableTutorial}}>
            {children}
        </TutorialContext.Provider>
    )
}

export default TutorialContextProvider;

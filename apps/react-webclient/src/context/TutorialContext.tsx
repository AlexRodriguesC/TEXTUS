import { createContext } from "react";

interface TutorialContext {
  tutorialEnabled: boolean
  enableTutorial: () => void
  disableTutorial: () => void
}

export const TutorialContext = createContext<TutorialContext | null>(null);

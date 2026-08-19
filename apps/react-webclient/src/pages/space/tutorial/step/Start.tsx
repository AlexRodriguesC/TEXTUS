import TutorialStep from "@/pages/space/tutorial/TutorialStep.tsx";

interface StartProps {
  index: number;
}

function Start({ index }: StartProps) {
  return (
    <TutorialStep
      index={index}
      titleKey="start.title"
      descriptionKey="start.description"
      lottieSrc="/animations/start.lottie"
    />
  );
}

export default Start;

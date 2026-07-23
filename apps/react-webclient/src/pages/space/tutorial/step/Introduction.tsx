import TutorialStep from "@/pages/space/tutorial/TutorialStep.tsx";

interface IntroductionProps {
  index: number;
}

function Introduction({ index }: IntroductionProps) {
  return (
    <TutorialStep
      index={index}
      titleKey="intro.title"
      descriptionKey="intro.description"
      lottieSrc="/animations/ideas.lottie"
    />
  );
}

export default Introduction;

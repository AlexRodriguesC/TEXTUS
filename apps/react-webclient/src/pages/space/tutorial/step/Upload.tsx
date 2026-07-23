import UploadZone from "@/components/UploadZone.tsx";
import TutorialStep from "@/pages/space/tutorial/TutorialStep.tsx";

interface UploadProps {
  index: number;
}

function Upload({ index }: UploadProps) {
  return (
    <TutorialStep
      index={index}
      titleKey="upload.title"
      descriptionKey="upload.description"
      lottieSide="right"
      aside={<UploadZone />}
    />
  );
}

export default Upload;

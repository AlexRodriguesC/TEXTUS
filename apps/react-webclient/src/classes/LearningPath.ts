import type Chapter from "@/classes/Chapter.ts";
import type PathSettings from "@/classes/PathSettings.ts";

class LearningPath {
  chapters: Chapter[];
  settings: PathSettings;

  constructor(chapters: Chapter[], settings: PathSettings) {
    this.chapters = chapters;
    this.settings = settings;
  }
}

export default LearningPath;

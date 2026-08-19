import type Lesson from "@/classes/Lesson.ts";

class Chapter {
  index: number;
  title: string;
  description: string;
  lessons: Lesson[];

  constructor(
    index: number,
    title: string,
    description: string,
    lessons: Lesson[],
  ) {
    this.index = index;
    this.title = title;
    this.description = description;
    this.lessons = lessons;
  }
}

export default Chapter;

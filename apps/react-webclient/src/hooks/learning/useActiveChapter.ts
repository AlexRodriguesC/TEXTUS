import type Chapter from "@/classes/Chapter.ts";
import type LearningPath from "@/classes/LearningPath.ts";
import { useEffect, useRef, useState, type RefObject } from "react";

function useActiveChapter(
  path: LearningPath | undefined,
  viewportRef: RefObject<HTMLDivElement | null>,
  headerHeight: number,
): Chapter | null {
  const [activeChapter, setActiveChapter] = useState<Chapter | null>(null);
  const intersectingRef = useRef<Set<number>>(new Set());

  useEffect(() => {
    if (path?.chapters.length) {
      setActiveChapter((prev) => prev ?? path.chapters[0]);
    }
  }, [path]);

  useEffect(() => {
    if (!path?.chapters.length || !viewportRef.current || headerHeight === 0) {
      return;
    }

    const elements = document.querySelectorAll<HTMLDivElement>(
      "[data-chapter-index]",
    );
    intersectingRef.current = new Set();

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          const index = Number(entry.target.getAttribute("data-chapter-index"));
          if (entry.isIntersecting) {
            intersectingRef.current.add(index);
          } else {
            intersectingRef.current.delete(index);
          }
        });

        if (intersectingRef.current.size === 0) return;

        const bestIndex = Math.min(...intersectingRef.current);
        const chapter = path.chapters.find((c) => c.index === bestIndex);
        if (chapter) setActiveChapter(chapter);
      },
      {
        root: viewportRef.current,
        rootMargin: `-${headerHeight}px 0px -80% 0px`,
        threshold: 0,
      },
    );

    elements.forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, [path, headerHeight, viewportRef]);

  return activeChapter;
}

export default useActiveChapter;

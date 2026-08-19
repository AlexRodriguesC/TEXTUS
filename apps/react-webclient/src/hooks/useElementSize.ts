import { useEffect, useState, type RefObject } from "react";

function useElementSize<T extends HTMLElement>(
  ref: RefObject<T | null>,
  deps: unknown[] = [],
): number {
  const [height, setHeight] = useState(0);

  useEffect(() => {
    if (!ref.current) return;

    const ro = new ResizeObserver(([entry]) => {
      setHeight(entry.contentRect.height);
    });
    ro.observe(ref.current);
    return () => ro.disconnect();
  }, [ref, ...deps]);

  return height;
}

export default useElementSize;

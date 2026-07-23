import { useEffect, useState, type RefObject } from "react";

type UseListPageSizeOptions = {
  gapPx?: number;
  fallback?: number;
  enabled?: boolean;
  measureKey?: string | number;
};

function useListPageSize(
  containerRef: RefObject<HTMLElement | null>,
  itemRef: RefObject<HTMLElement | null>,
  {
    gapPx = 8,
    fallback = 4,
    enabled = true,
    measureKey,
  }: UseListPageSizeOptions = {},
) {
  const [pageSize, setPageSize] = useState(fallback);

  useEffect(() => {
    if (!enabled) return;

    const container = containerRef.current;
    if (!container) return;

    const measure = () => {
      const item = itemRef.current;
      if (!item) return;

      const containerHeight = container.clientHeight;
      const itemHeight = item.getBoundingClientRect().height;
      if (containerHeight <= 0 || itemHeight <= 0) return;

      const nextPageSize = Math.max(
        1,
        Math.floor((containerHeight + gapPx) / (itemHeight + gapPx)),
      );
      setPageSize(nextPageSize);
    };

    const observer = new ResizeObserver(measure);
    observer.observe(container);
    if (itemRef.current) {
      observer.observe(itemRef.current);
    }

    measure();

    return () => observer.disconnect();
  }, [containerRef, itemRef, gapPx, enabled, measureKey]);

  return pageSize;
}

export default useListPageSize;

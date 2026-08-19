import { useLayoutEffect, useRef, useState, type RefObject } from "react";

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
  const overflowCapRef = useRef<number | null>(null);
  const lastContainerHeightRef = useRef(0);

  useLayoutEffect(() => {
    if (!enabled) return;

    const container = containerRef.current;
    if (!container) return;

    const measure = () => {
      const item = itemRef.current;
      if (!item) return;

      const containerHeight = container.clientHeight;
      const itemHeight = item.getBoundingClientRect().height;
      if (containerHeight <= 0 || itemHeight <= 0) return;

      const heightChanged = containerHeight !== lastContainerHeightRef.current;
      if (heightChanged) {
        overflowCapRef.current = null;
        lastContainerHeightRef.current = containerHeight;
      }

      if (container.scrollHeight > container.clientHeight) {
        setPageSize((prev) => {
          const next = Math.max(1, prev - 1);
          if (next < prev) {
            overflowCapRef.current = prev;
          }
          return next;
        });
        return;
      }

      const estimated = Math.max(
        1,
        Math.floor((containerHeight + gapPx) / (itemHeight + gapPx)),
      );
      const capped =
        overflowCapRef.current === null
          ? estimated
          : Math.min(estimated, overflowCapRef.current - 1);
      const nextSize = Math.max(1, capped);

      setPageSize((prev) =>
        nextSize > prev && heightChanged ? nextSize : prev,
      );
    };

    const observer = new ResizeObserver(measure);
    observer.observe(container);
    if (itemRef.current) {
      observer.observe(itemRef.current);
    }

    measure();

    return () => observer.disconnect();
  }, [containerRef, itemRef, gapPx, enabled, measureKey, pageSize]);

  return pageSize;
}

export default useListPageSize;

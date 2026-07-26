import useListPageSize from "@/hooks/useListPageSize.ts";
import { useMemo, useRef, useState, type RefObject } from "react";

interface UsePaginatedFilterOptions<T> {
  items: T[];
  search: string;
  isLoading: boolean;
  filterFn: (item: T, search: string) => boolean;
  isEmpty?: (items: T[], filtered: T[]) => boolean;
  measureKey?: string | number;
  gapPx?: number;
}

interface UsePaginatedFilterResult<T> {
  listRef: RefObject<HTMLDivElement | null>;
  itemRef: RefObject<HTMLDivElement | null>;
  filtered: T[];
  paginated: T[];
  isEmpty: boolean;
  isNotFound: boolean;
  showList: boolean;
  totalPages: number;
  safePage: number;
  goToPage: (nextPage: number) => void;
  setPage: (page: number) => void;
}

function usePaginatedFilter<T>({
  items,
  search,
  isLoading,
  filterFn,
  isEmpty: isEmptyFn,
  measureKey,
  gapPx,
}: UsePaginatedFilterOptions<T>): UsePaginatedFilterResult<T> {
  const [page, setPage] = useState(1);
  const listRef = useRef<HTMLDivElement>(null);
  const itemRef = useRef<HTMLDivElement>(null);

  const filtered = useMemo(
    () => items.filter((item) => filterFn(item, search)),
    [items, search, filterFn],
  );

  const isEmpty = isEmptyFn
    ? isEmptyFn(items, filtered)
    : items.length === 0;
  const isNotFound = search.length > 0 && filtered.length === 0;
  const showList = !isLoading && !isEmpty && !isNotFound;

  const pageSize = useListPageSize(listRef, itemRef, {
    enabled: showList && filtered.length > 0,
    measureKey,
    gapPx,
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const safePage = Math.min(page, totalPages);

  const paginated = useMemo(() => {
    const start = (safePage - 1) * pageSize;
    return filtered.slice(start, start + pageSize);
  }, [filtered, safePage, pageSize]);

  const goToPage = (nextPage: number) => {
    setPage(Math.min(Math.max(1, nextPage), totalPages));
  };

  return {
    listRef,
    itemRef,
    filtered,
    paginated,
    isEmpty,
    isNotFound,
    showList,
    totalPages,
    safePage,
    goToPage,
    setPage,
  };
}

export default usePaginatedFilter;

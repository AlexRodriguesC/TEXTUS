import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useSearch } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useTopics from "@/hooks/useTopics.tsx";
import EmptyTopics from "@/pages/space/topics/EmptyTopics.tsx";
import TopicEntryList from "@/pages/space/topics/TopicEntryList.tsx";
import TopicEntryListSkeleton from "@/pages/space/topics/TopicEntryListSkeleton.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside, {
  AsideFooter,
  AsideHeader,
} from "@/components/IllustratedAside.tsx";
import useListPageSize from "@/hooks/useListPageSize.ts";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination.tsx";

function getPaginationRange(
  current: number,
  total: number,
): (number | "ellipsis")[] {
  if (total <= 7) {
    return Array.from({ length: total }, (_, index) => index + 1);
  }

  const pages: (number | "ellipsis")[] = [1];
  const showLeftEllipsis = current > 3;
  const showRightEllipsis = current < total - 2;

  if (showLeftEllipsis) {
    pages.push("ellipsis");
  }

  const rangeStart = showLeftEllipsis ? Math.max(2, current - 1) : 2;
  const rangeEnd = showRightEllipsis ? Math.min(total - 1, current + 1) : total - 1;

  for (let page = rangeStart; page <= rangeEnd; page++) {
    pages.push(page);
  }

  if (showRightEllipsis) {
    pages.push("ellipsis");
  }

  if (total > 1) {
    pages.push(total);
  }

  return pages;
}

function TopicsPage() {
  const { t } = useTranslation();
  const { strSpaceId } = useSpaceId();
  const navigate = useNavigate();
  const { q } = useSearch({ strict: false }) as { q?: string };
  const search = q ?? "";
  const [page, setPage] = useState(1);
  const searchInputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);
  const itemRef = useRef<HTMLDivElement>(null);
  const { topics, isLoading } = useTopics();

  const filtered = useMemo(() => {
    return topics.filter((topic) =>
      topic.title.toLowerCase().includes(search.toLowerCase()),
    );
  }, [search, topics]);

  const isEmpty = topics.length === 0;
  const isNotFound = search.length > 0 && filtered.length === 0;
  const showList = !isLoading && !isEmpty && !isNotFound;

  const pageSize = useListPageSize(listRef, itemRef, {
    enabled: showList && filtered.length > 0,
    measureKey: filtered[0]?.id,
    gapPx: 16,
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const safePage = Math.min(page, totalPages);

  const paginated = useMemo(() => {
    const start = (safePage - 1) * pageSize;
    return filtered.slice(start, start + pageSize);
  }, [filtered, safePage, pageSize]);

  useEffect(() => {
    setPage(1);
  }, [search]);

  const setSearch = (value: string) => {
    navigate({
      to: PathConstants.SPACE_TOPICS,
      params: { spaceId: strSpaceId },
      search: { q: value || undefined },
      replace: true,
    });
  };

  const resetSearch = () => {
    setSearch("");
    searchInputRef.current?.focus();
  };

  const goToPage = (nextPage: number) => {
    setPage(Math.min(Math.max(1, nextPage), totalPages));
  };

  const content = (() => {
    if (isLoading) {
      return <TopicEntryListSkeleton />;
    }

    if (isEmpty) {
      return (
        <div className="flex flex-1 min-h-0 items-center justify-center overflow-y-auto">
          <EmptyTopics />
        </div>
      );
    }

    if (isNotFound) {
      return (
        <div className="flex flex-1 min-h-0 items-center justify-center overflow-y-auto">
          <CenteredPage>
            <IllustratedAside
              className="gap-4"
              lottieSrc="/animations/not_found.lottie"
              lottieClassName="opacity-60"
              lottieProps={{ loop: true, autoplay: true }}
            >
              <AsideHeader>
                <p>{t("topics.not-found")}</p>
              </AsideHeader>
              <AsideFooter>
                <Button variant="outline" onClick={resetSearch}>
                  {t("topics.reset-search")}
                </Button>
              </AsideFooter>
            </IllustratedAside>
          </CenteredPage>
        </div>
      );
    }

    return (
      <div className="flex flex-1 min-h-0 flex-col items-center overflow-hidden">
        <div
          ref={listRef}
          className="flex flex-1 min-h-0 w-full max-w-[90%] lg:max-w-[40%] flex-col px-4"
        >
          <div className="flex flex-1 min-h-0 flex-col overflow-y-auto py-6">
            <TopicEntryList topics={paginated} itemRef={itemRef} />
          </div>

          {totalPages > 1 && (
            <Pagination className="mt-4 shrink-0 pb-6">
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    href="#"
                    text={t("button.back")}
                    className={
                      safePage === 1 ? "pointer-events-none opacity-50" : undefined
                    }
                    onClick={(event) => {
                      event.preventDefault();
                      goToPage(safePage - 1);
                    }}
                  />
                </PaginationItem>

                {getPaginationRange(safePage, totalPages).map((pageNumber, index) =>
                  pageNumber === "ellipsis" ? (
                    <PaginationItem key={`ellipsis-${index}`}>
                      <PaginationEllipsis />
                    </PaginationItem>
                  ) : (
                    <PaginationItem key={pageNumber}>
                      <PaginationLink
                        href="#"
                        isActive={safePage === pageNumber}
                        onClick={(event) => {
                          event.preventDefault();
                          goToPage(pageNumber);
                        }}
                      >
                        {pageNumber}
                      </PaginationLink>
                    </PaginationItem>
                  ),
                )}

                <PaginationItem>
                  <PaginationNext
                    href="#"
                    text={t("button.next")}
                    className={
                      safePage === totalPages
                        ? "pointer-events-none opacity-50"
                        : undefined
                    }
                    onClick={(event) => {
                      event.preventDefault();
                      goToPage(safePage + 1);
                    }}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          )}
        </div>
      </div>
    );
  })();

  return (
    <div className="mx-auto flex h-full w-full max-w-[75%] flex-col">
      {!isLoading && !isEmpty && (
        <header className="flex shrink-0 flex-col items-center gap-4 px-4 pt-8">
          <Input
            ref={searchInputRef}
            className="w-full max-w-[90%] lg:max-w-[40%]"
            placeholder={t("topics.search")}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </header>
      )}

      <div className="flex min-h-0 flex-1 flex-col">{content}</div>
    </div>
  );
}

export default TopicsPage;

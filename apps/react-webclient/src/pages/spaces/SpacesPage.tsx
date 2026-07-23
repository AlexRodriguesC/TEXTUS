import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { useMemo, useRef, useState } from "react";
import SpacesListItem from "@/pages/spaces/SpacesListItem.tsx";
import EmptySpaces from "@/pages/spaces/EmptySpaces.tsx";
import CreateSpace from "@/pages/spaces/CreateSpace.tsx";
import useSpaces from "@/hooks/useSpaces.ts";
import SpacesListSkeleton from "@/pages/spaces/SpacesListSkeleton.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside from "@/components/IllustratedAside.tsx";
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

export function SpacesPage() {
  const { t } = useTranslation();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const searchInputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);
  const itemRef = useRef<HTMLDivElement>(null);
  const { spaces, isLoading } = useSpaces();

  const filtered = useMemo(() => {
    return spaces.filter((space) =>
      space.name.toLowerCase().includes(search.toLowerCase()),
    );
  }, [search, spaces]);

  const isEmpty = filtered.length === 0 && spaces.length === 0;
  const isNotFound = search.length > 0 && filtered.length === 0;
  const showList = !isLoading && !isEmpty && !isNotFound;

  const pageSize = useListPageSize(listRef, itemRef, {
    enabled: showList && filtered.length > 0,
    measureKey: filtered[0]?.spaceId,
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const safePage = Math.min(page, totalPages);

  const paginated = useMemo(() => {
    const start = (safePage - 1) * pageSize;
    return filtered.slice(start, start + pageSize);
  }, [filtered, safePage, pageSize]);

  const resetSearch = () => {
    setSearch("");
    setPage(1);
    searchInputRef.current?.focus();
  };

  const goToPage = (nextPage: number) => {
    setPage(Math.min(Math.max(1, nextPage), totalPages));
  };

  const content = (() => {
    if (isLoading) {
      return <SpacesListSkeleton />;
    }

    if (isEmpty) {
      return (
        <div className="flex flex-1 min-h-0 items-center justify-center overflow-y-auto">
          <EmptySpaces />
        </div>
      );
    }

    if (isNotFound) {
      return (
        <div className="flex flex-1 min-h-0 items-center justify-center overflow-y-auto">
          <CenteredPage>
            <IllustratedAside
              className="gap-4"
              lottieSrc={"/animations/not_found.lottie"}
              lottieClassName="opacity-60"
              lottieProps={{ loop: true, autoplay: true }}
            >
              <div className={"flex flex-col items-center gap-2"}>
                <p>{t("spaces.not-found")}</p>
                <Button variant={"outline"} onClick={resetSearch}>
                  {t("spaces.reset-search")}
                </Button>
              </div>
            </IllustratedAside>
          </CenteredPage>
        </div>
      );
    }

    return (
      <div className="flex flex-1 min-h-0 flex-col">
        <div
          ref={listRef}
          className="flex flex-1 min-h-0 flex-col min-w-full overflow-y-auto gap-2"
        >
          {paginated.map((space, index) => (
            <div
              key={space.spaceId}
              ref={index === 0 ? itemRef : undefined}
            >
              <SpacesListItem
                icon={space.icon}
                spaceId={space.spaceId}
                name={space.name}
                description={space.description}
              />
            </div>
          ))}
        </div>

        {totalPages > 1 && (
          <Pagination className="mt-4 shrink-0">
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
    );
  })();

  return (
    <div className="mx-auto flex h-full w-full max-w-[75%] flex-col">
      <header className="flex shrink-0 flex-col items-center gap-4 pt-8 md:flex-row md:items-start md:gap-16">
        <CreateSpace
          trigger={
            <Button className={spaces.length === 0 ? "animate-pulse" : ""}>
              {t("spaces.add.message")}
            </Button>
          }
        />
        <Input
          ref={searchInputRef}
          placeholder={t("spaces.search")}
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(1);
          }}
        />
      </header>

      <div className="flex min-h-0 flex-1 flex-col pt-4">{content}</div>
    </div>
  );
}

export default SpacesPage;

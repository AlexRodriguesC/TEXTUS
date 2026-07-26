import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import SearchInput from "@/components/SearchInput.tsx";
import { useRef, useState } from "react";
import SpacesListItem from "@/pages/spaces/SpacesListItem.tsx";
import EmptySpaces from "@/pages/spaces/EmptySpaces.tsx";
import CreateSpace from "@/pages/spaces/CreateSpace.tsx";
import useSpaces from "@/hooks/useSpaces.ts";
import SpacesListSkeleton from "@/pages/spaces/SpacesListSkeleton.tsx";
import PaginatedListControls from "@/components/PaginatedListControls.tsx";
import SearchNotFoundAside from "@/components/SearchNotFoundAside.tsx";
import usePaginatedFilter from "@/hooks/usePaginatedFilter.ts";
import type Space from "@/classes/Space.ts";

const filterSpaces = (space: Space, search: string) =>
  space.name.toLowerCase().includes(search.toLowerCase());

export function SpacesPage() {
  const { t } = useTranslation();
  const [search, setSearch] = useState("");
  const searchInputRef = useRef<HTMLInputElement>(null);
  const { spaces, isLoading } = useSpaces();

  const {
    listRef,
    itemRef,
    paginated,
    isEmpty,
    isNotFound,
    totalPages,
    safePage,
    goToPage,
    setPage,
  } = usePaginatedFilter({
    items: spaces,
    search,
    isLoading,
    filterFn: filterSpaces,
    isEmpty: (all) => all.length === 0,
    measureKey: spaces.find((s) =>
      s.name.toLowerCase().includes(search.toLowerCase()),
    )?.spaceId,
  });

  const resetSearch = () => {
    setSearch("");
    setPage(1);
    searchInputRef.current?.focus();
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
        <SearchNotFoundAside
          message={t("spaces.not-found")}
          resetLabel={t("spaces.reset-search")}
          onReset={resetSearch}
        />
      );
    }

    return (
      <div className="flex flex-1 min-h-0 flex-col">
        <div ref={listRef} className="flex flex-1 min-h-0 flex-col min-w-full">
          <div className="flex flex-1 min-h-0 flex-col min-w-full overflow-y-auto gap-2">
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
        </div>

        <PaginatedListControls
          safePage={safePage}
          totalPages={totalPages}
          onPageChange={goToPage}
        />
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
        <SearchInput
          ref={searchInputRef}
          placeholder={t("spaces.search")}
          clearLabel={t("spaces.reset-search")}
          value={search}
          onChange={(value) => {
            setSearch(value);
            setPage(1);
          }}
        />
      </header>

      <div className="flex min-h-0 flex-1 flex-col pt-4">{content}</div>
    </div>
  );
}

export default SpacesPage;

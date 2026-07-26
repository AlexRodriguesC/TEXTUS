import { useTranslation } from "react-i18next";
import SearchInput from "@/components/SearchInput.tsx";
import { useRef } from "react";
import { useNavigate, useSearch } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useTopics from "@/hooks/useTopics.tsx";
import EmptyTopics from "@/pages/space/topics/EmptyTopics.tsx";
import TopicEntryList from "@/pages/space/topics/TopicEntryList.tsx";
import TopicEntryListSkeleton from "@/pages/space/topics/TopicEntryListSkeleton.tsx";
import PaginatedListControls from "@/components/PaginatedListControls.tsx";
import SearchNotFoundAside from "@/components/SearchNotFoundAside.tsx";
import usePaginatedFilter from "@/hooks/usePaginatedFilter.ts";
import type Topic from "@/classes/Topic.ts";

const filterTopics = (topic: Topic, search: string) =>
  topic.title.toLowerCase().includes(search.toLowerCase());

function TopicsPage() {
  const { t } = useTranslation();
  const { strSpaceId } = useSpaceId();
  const navigate = useNavigate();
  const { q } = useSearch({ strict: false }) as { q?: string };
  const search = q ?? "";
  const searchInputRef = useRef<HTMLInputElement>(null);
  const { topics, isLoading } = useTopics();

  const {
    listRef,
    itemRef,
    paginated,
    isEmpty,
    isNotFound,
    totalPages,
    safePage,
    goToPage,
  } = usePaginatedFilter({
    items: topics,
    search,
    isLoading,
    filterFn: filterTopics,
    measureKey: topics.find((topic) =>
      topic.title.toLowerCase().includes(search.toLowerCase()),
    )?.id,
    gapPx: 16,
    resetPageOnSearchChange: true,
  });

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
        <SearchNotFoundAside
          message={t("topics.not-found")}
          resetLabel={t("topics.reset-search")}
          onReset={resetSearch}
        />
      );
    }

    return (
      <div className="flex flex-1 min-h-0 flex-col items-center overflow-hidden">
        <div
          ref={listRef}
          className="flex flex-1 min-h-0 w-full flex-col px-4"
        >
          <div className="flex flex-1 min-h-0 flex-col overflow-y-auto py-6">
            <TopicEntryList topics={paginated} itemRef={itemRef} />
          </div>

          <PaginatedListControls
            safePage={safePage}
            totalPages={totalPages}
            onPageChange={goToPage}
            className="pb-6"
          />
        </div>
      </div>
    );
  })();

  return (
    <div className="flex h-full w-full flex-col">
      {!isLoading && !isEmpty && (
        <header className="flex shrink-0 flex-col items-center gap-4 px-4 pt-8">
          <SearchInput
            ref={searchInputRef}
            className="w-full"
            placeholder={t("topics.search")}
            clearLabel={t("topics.reset-search")}
            value={search}
            onChange={setSearch}
          />
        </header>
      )}

      <div className="flex min-h-0 flex-1 flex-col">{content}</div>
    </div>
  );
}

export default TopicsPage;

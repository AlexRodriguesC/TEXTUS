import { useTranslation } from "react-i18next";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination.tsx";
import { getPaginationRange } from "@/lib/paginationRange.ts";
import { cn } from "@/lib/utils.ts";

interface PaginatedListControlsProps {
  safePage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  className?: string;
}

function renderPageItems(
  range: (number | "ellipsis")[],
  safePage: number,
  onPageChange: (page: number) => void,
  keyPrefix: string,
  itemClassName: string,
) {
  return range.map((pageNumber, index) =>
    pageNumber === "ellipsis" ? (
      <PaginationItem
        key={`${keyPrefix}-ellipsis-${index}`}
        className={itemClassName}
      >
        <PaginationEllipsis />
      </PaginationItem>
    ) : (
      <PaginationItem key={`${keyPrefix}-${pageNumber}`} className={itemClassName}>
        <PaginationLink
          href="#"
          isActive={safePage === pageNumber}
          className="size-9 sm:size-10"
          onClick={(event) => {
            event.preventDefault();
            onPageChange(pageNumber);
          }}
        >
          {pageNumber}
        </PaginationLink>
      </PaginationItem>
    ),
  );
}

function PaginatedListControls({
  safePage,
  totalPages,
  onPageChange,
  className,
}: PaginatedListControlsProps) {
  const { t } = useTranslation();

  if (totalPages <= 1) {
    return null;
  }

  const compactRange = getPaginationRange(safePage, totalPages, 0);
  const fullRange = getPaginationRange(safePage, totalPages);

  return (
    <Pagination className={cn("mt-4 shrink-0", className)}>
      <PaginationContent className="max-w-full gap-0.5 sm:gap-1">
        <PaginationItem>
          <PaginationPrevious
            href="#"
            text={t("button.back")}
            className={
              safePage === 1 ? "pointer-events-none opacity-50" : undefined
            }
            onClick={(event) => {
              event.preventDefault();
              onPageChange(safePage - 1);
            }}
          />
        </PaginationItem>

        {renderPageItems(
          compactRange,
          safePage,
          onPageChange,
          "mobile",
          "sm:hidden",
        )}
        {renderPageItems(
          fullRange,
          safePage,
          onPageChange,
          "desktop",
          "hidden sm:list-item",
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
              onPageChange(safePage + 1);
            }}
          />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  );
}

export default PaginatedListControls;

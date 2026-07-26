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

  return (
    <Pagination className={cn("mt-4 shrink-0", className)}>
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
              onPageChange(safePage - 1);
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
                  onPageChange(pageNumber);
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
              onPageChange(safePage + 1);
            }}
          />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  );
}

export default PaginatedListControls;

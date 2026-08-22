export function getPaginationRange(
  current: number,
  total: number,
  siblingCount = 1,
): (number | "ellipsis")[] {
  if (total <= siblingCount * 2 + 5) {
    return Array.from({ length: total }, (_, index) => index + 1);
  }

  const pages: (number | "ellipsis")[] = [1];
  const showLeftEllipsis = current > siblingCount + 2;
  const showRightEllipsis = current < total - siblingCount - 1;

  if (showLeftEllipsis) {
    pages.push("ellipsis");
  }

  const rangeStart = showLeftEllipsis
    ? Math.max(2, current - siblingCount)
    : 2;
  const rangeEnd = showRightEllipsis
    ? Math.min(total - 1, current + siblingCount)
    : total - 1;

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

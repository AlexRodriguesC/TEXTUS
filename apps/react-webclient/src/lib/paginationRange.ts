export function getPaginationRange(
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
  const rangeEnd = showRightEllipsis
    ? Math.min(total - 1, current + 1)
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

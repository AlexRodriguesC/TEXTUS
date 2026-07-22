import { useGetPages } from "@/client/generated/pages/pages.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import type { ErrorResponseDTO, PagesResponseDTO } from "@/client/model";
import Page from "@/classes/Page.ts";
import { mapPage } from "@/hooks/dtoMappers.ts";

function usePages() {
  const { spaceId } = useSpaceId();

  const { data: pagesData } = useGetPages(spaceId ?? 0, {
    query: {
      enabled: spaceId !== undefined,
    },
  });

  let pages: Page[] = [];

  if (pagesData?.data && isPagesResponse(pagesData.data)) {
    pages = pagesData.data.pages.map(mapPage);
  }

  return {
    pages,
  };
}

export default usePages;

function isPagesResponse(
  data: ErrorResponseDTO | PagesResponseDTO,
): data is PagesResponseDTO {
  return "pages" in data;
}

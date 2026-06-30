import SpaceMap from "@/pages/space/SpaceMap.tsx";
import useDocuments from "@/hooks/useDocuments.ts";
import Upload from "@/pages/space/Upload.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import CenteredPage from "@/components/CenteredPage.tsx";
import SpaceSidebar from "@/pages/space/SpaceSidebar.tsx";
import useSpaces from "@/hooks/useSpaces.ts";
import { useMemo } from "react";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb.tsx";
import PathConstants from "@/routing/pathConstants.ts";

function SpacePage() {
  const { spaceId } = useSpaceId();
  const { spaces } = useSpaces();
  const space = useMemo(
    () => spaces.filter((s) => s.spaceId == spaceId)[0],
    [spaces, spaceId],
  );
  const { documents } = useDocuments(spaceId);

  if (documents.length == 0) {
    return (
      <div className={"max-w-[75%] md:max-w-[50%] lg:max-w-[40%]"}>
        <Upload showHeaders={true} />
      </div>
    );
  }

  return (
    <CenteredPage>
      <div className={"w-full h-full pt-8 pb-8"}>
        <div className={"w-full h-fit"}>
          <Breadcrumb>
            <BreadcrumbList>
              <BreadcrumbItem>
                <BreadcrumbLink href={PathConstants.HOME}>Home</BreadcrumbLink>
              </BreadcrumbItem>
              <BreadcrumbSeparator />
              <BreadcrumbItem>
                <BreadcrumbLink href={PathConstants.SPACES}>
                  Spaces
                </BreadcrumbLink>
              </BreadcrumbItem>
              <BreadcrumbSeparator />
              <BreadcrumbItem>
                <BreadcrumbPage>{space.name}</BreadcrumbPage>
              </BreadcrumbItem>
            </BreadcrumbList>
          </Breadcrumb>
        </div>
        <div className="flex flex-col lg:flex-row gap-4 h-full w-full">
          {/* Sidebar */}
          <aside className="w-full lg:w-1/4">
            <SpaceSidebar space={space} />
          </aside>

          {/* Main content */}
          <div className="flex-1 w-full">
            <SpaceMap />
          </div>
        </div>
      </div>
    </CenteredPage>
  );
}

export default SpacePage;

import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { useMemo, useRef, useState } from "react";
import SpacesListItem from "@/pages/spaces/SpacesListItem.tsx";
import EmptySpaces from "@/pages/spaces/EmptySpaces.tsx";
import CreateSpace from "@/pages/spaces/CreateSpace.tsx";
import useSpaces from "@/hooks/useSpaces.ts";
import LoadingScreen from "@/components/LoadingScreen.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";

export function SpacesPage() {
  const { t } = useTranslation();
  const [search, setSearch] = useState("");
  const searchInputRef = useRef<HTMLInputElement>(null);
  const { spaces, isLoading } = useSpaces();

  const filtered = useMemo(() => {
    return spaces.filter((space) =>
      space.name.toLowerCase().includes(search.toLowerCase()),
    );
  }, [search, spaces]);

  const resetSearch = () => {
    setSearch("");
    searchInputRef.current?.focus();
  };

  const content = (() => {
    if (isLoading) {
      return <LoadingScreen />;
    }

    if (filtered.length === 0 && spaces.length === 0) {
      return (
        <div className="max-h-[60vh] min-h-[60vh] overflow-y-auto items-center place-content-center">
          <EmptySpaces />
        </div>
      );
    }

    if (search.length > 0 && filtered.length === 0) {
      return (
        <div className="flex flex-col max-h-[60vh] min-h-[60vh] min-w-full overflow-y-auto gap-2">
          <CenteredPage>
            <div className={"flex flex-col items-center gap-4 md:flex-row"}>
              <ResponsiveLottie
                className={"opacity-60"}
                src={"/animations/not_found.lottie"}
                loop
                autoplay
              />
              <div className={"flex flex-col items-center gap-2"}>
                <p>{t("spaces.not-found")}</p>
                <Button variant={"outline"} onClick={resetSearch}>
                  {t("spaces.reset-search")}
                </Button>
              </div>
            </div>
          </CenteredPage>
        </div>
      );
    }

    return (
      <div className="flex flex-col max-h-[60vh] min-h-[60vh] min-w-full overflow-y-auto gap-2">
        {filtered.map((space) => (
          <SpacesListItem
            key={space.spaceId}
            icon={space.icon}
            spaceId={space.spaceId}
            name={space.name}
            description={space.description}
          />
        ))}
      </div>
    );
  })();

  return (
    <div className="mx-auto flex w-full max-w-[75%] min-h-[80vh] max-h-[80vh] flex-col place-content-center gap-4">
      <div className="flex flex-col pt-8 md:pt-0 items-center md:flex-row md:items-start md:gap-16">
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
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className={"mt-4 mb-4"} />

      {content}
    </div>
  );
}

export default SpacesPage;

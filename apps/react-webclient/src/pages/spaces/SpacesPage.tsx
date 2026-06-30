import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { useMemo, useState } from "react";
import SpacesListItem from "@/pages/spaces/SpacesListItem.tsx";
import EmptySpaces from "@/pages/spaces/EmptySpaces.tsx";
import CreateSpace from "@/pages/spaces/CreateSpace.tsx";
import useSpaces from "@/hooks/useSpaces.ts";

export function SpacesPage() {
  const { t } = useTranslation();
  const [search, setSearch] = useState("");
  const { spaces } = useSpaces();

  const filtered = useMemo(() => {
    return spaces.filter((space) =>
      space.name.toLowerCase().includes(search.toLowerCase()),
    );
  }, [search, spaces]);

  return (
    <div className="mx-auto flex w-full max-w-[75%] min-h-[80vh] max-h-[80vh] flex-col place-content-center gap-4">
      <div className="flex flex-row items-start gap-16">
        <CreateSpace
          trigger={
            <Button className={spaces.length === 0 ? "animate-pulse" : ""}>
              {t("spaces.add.message")}
            </Button>
          }
        />
        <Input
          placeholder={t("spaces.search")}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className={"mt-4 mb-4"} />

      {filtered.length === 0 ? (
        <div className="max-h-[60vh] min-h-[60vh] overflow-y-auto items-center place-content-center">
          <EmptySpaces />
        </div>
      ) : (
        <div className="flex flex-col max-h-[60vh] min-h-[60vh] min-w-[100%] overflow-y-auto gap-2">
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
      )}
    </div>
  );
}

export default SpacesPage;

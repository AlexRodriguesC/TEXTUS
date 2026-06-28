import {useTranslation} from "react-i18next";
import {Button} from "@/components/ui/button.tsx";
import {Input} from "@/components/ui/input.tsx";
import {useMemo, useState} from "react";
import {useReadSpaces} from "@/api/generated/spaces/spaces.ts";
import SpacesListItem from "@/pages/spaces/SpacesListItem.tsx";
import EmptySpaces from "@/pages/spaces/EmptySpaces.tsx";
import CreateSpace from "@/pages/spaces/CreateSpace.tsx";

export function SpacesPage() {
    const {t} = useTranslation();
    const [search, setSearch] = useState("");

    const {data, isError} = useReadSpaces();

    const spaces = useMemo(() => {
        return isError ? [] : (data?.data ?? [])
    }, [isError, data]);

    const filtered = useMemo(() => {
        return spaces.filter((space) =>
            space && space.spaceId && space.name && space.description && space.name.toLowerCase().includes(search.toLowerCase())
        );
    }, [search, spaces]);

    return (
        <div className="mx-auto flex w-full max-w-[75%] min-h-[80vh] max-h-[80vh] flex-col place-content-center gap-4">
            <div className="flex flex-row items-start gap-16">
                <CreateSpace trigger={<Button className={spaces.length === 0 ? "animate-pulse" : ""}>{t("spaces.add.message")}</Button>}/>
                <Input
                    placeholder={t("spaces.search")}
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </div>

            <div className={"mt-4 mb-4"}/>

            {filtered.length === 0 ? (
                <div className="max-h-[60vh] min-h-[60vh] overflow-y-auto items-center place-content-center">
                    <EmptySpaces />
                </div>
            ) : (
                <div className="flex flex-col max-h-[60vh] min-h-[60vh] min-w-[100%] overflow-y-auto gap-2">
                    {filtered.map((space) => (
                        <SpacesListItem
                            key={`${space.spaceId}-${space.name}-${space.description}`}
                            spaceId={space.spaceId!}
                            name={space.name!}
                            description={space.description!}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

export default SpacesPage;

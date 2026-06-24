import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Moon, Pencil, Trash2 } from "lucide-react";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import {useState} from "react";

export function SpacesPage() {
    const { t } = useTranslation();
    const [search, setSearch] = useState("");

    const spaces = [
        { id: 0, name: "Pippo", description: "some desc" },
        { id: 1, name: "francesca", description: "some desc" },
        { id: 2, name: "abc", description: "some desc" },
        { id: 3, name: "Pippo", description: "some desc" },
        { id: 4, name: "francesca", description: "some desc" },
        { id: 5, name: "abc", description: "some desc" },
        { id: 6, name: "Pippo", description: "some desc" },
        { id: 7, name: "francesca", description: "some desc" },
        { id: 8, name: "abc", description: "some desc" },
    ];

    const filtered = spaces.filter((space) =>
        space.name.toLowerCase().includes(search.toLowerCase())
    );

    return (
        <div className="mx-auto flex w-full max-w-[75%] flex-col gap-8">
            <div className="flex flex-row items-center gap-16">
                <Button>Add space</Button>
                <Input
                    placeholder={t("spaces.search")}
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </div>

            <div className="flex flex-col gap-2 overflow-y-auto max-h-[60vh] pr-1">
                {filtered.length === 0 ? (
                    <p className="text-center text-muted-foreground py-8">
                        {t("spaces.empty")}
                    </p>
                ) : (
                    filtered.map((space) => (
                        <div
                            key={space.id}
                            className="flex flex-row items-center justify-between rounded-lg border border-border p-4 hover:bg-muted/50 transition-colors"
                        >
                            <Link
                                to={PathConstants.SPACE}
                                params={{ spaceId: String(space.id) }}
                                className="flex flex-1 flex-row items-center gap-4"
                            >
                                <Moon className="shrink-0" />
                                <div className="flex flex-col">
                                    <p className="font-medium">{space.name}</p>
                                    <p className="text-sm text-muted-foreground">{space.description}</p>
                                </div>
                            </Link>

                            <div className="flex flex-row gap-2">
                                <Button variant="ghost" size="icon" aria-label="Edit space">
                                    <Pencil className="size-4" />
                                </Button>
                                <Button variant="ghost" size="icon" aria-label="Delete space">
                                    <Trash2 className="size-4" />
                                </Button>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

export default SpacesPage;

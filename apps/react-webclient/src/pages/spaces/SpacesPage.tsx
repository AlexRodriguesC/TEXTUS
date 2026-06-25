import {useTranslation} from "react-i18next";
import {Button} from "@/components/ui/button.tsx";
import {Input} from "@/components/ui/input.tsx";
import {useCallback, useState} from "react";
import {getReadSpacesQueryKey, useCreateSpace, useReadSpaces} from "@/api/generated/spaces/spaces.ts";
import {useQueryClient} from "@tanstack/react-query";
import {failureToastAction, successToast} from "@/lib/toasts.ts";
import SpacesListItem from "@/pages/spaces/SpacesListItem.tsx";
import SpaceDialogForm from "@/pages/spaces/SpaceDialogForm.tsx";

export function SpacesPage() {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const [search, setSearch] = useState("");
    const defaultName = t("spaces.add.defaultName");
    const [name, setName] = useState(defaultName);
    const defaultDescription = t("spaces.add.defaultDescription");
    const [description, setDescription] = useState(defaultDescription);

    const [open, setOpen] = useState(false);

    const restoreDefaults = useCallback(() => {
        setName(defaultName);
        setDescription(defaultDescription);
    }, []);

    const submitCreate = useCallback(() => {
        createSpace.mutate({data: {name, description}});
    }, [])

    const {data, isError} = useReadSpaces();

    const createSpace = useCreateSpace({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({queryKey: getReadSpacesQueryKey()})
                setOpen(false);
                successToast(t("spaces.add.success"));
                restoreDefaults();
            },
            onError: async () => {
                failureToastAction(t("spaces.add.error"), <Button onClick={submitCreate}>t("button.retry")</Button>)
            }
        }
    })

    const spaces = isError ? [] : (data?.data ?? []);

    const filtered = spaces.filter((space) =>
        space && space.spaceId && space.name && space.description && space.name.toLowerCase().includes(search.toLowerCase())
    );

    return (
        <div className="mx-auto flex w-full max-w-[75%] flex-col gap-8">
            <div className="flex flex-row items-center gap-16">
                <SpaceDialogForm
                    open={open}
                    onOpenChange={setOpen}
                    trigger={<Button>{t("spaces.add.message")}</Button>}
                    title={t("spaces.add.title")}
                    descriptionText={t("spaces.add.description")}
                    submitLabel={t("button.add")}
                    cancelLabel={t("button.cancel")}
                    name={name}
                    description={description}
                    onNameChange={setName}
                    onDescriptionChange={setDescription}
                    onCancel={restoreDefaults}
                    onSubmit={submitCreate}
                />
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
                        <SpacesListItem spaceId={space.spaceId!} name={space.name!} description={space.description!}/>))
                )}
            </div>
        </div>
    );
}

export default SpacesPage;

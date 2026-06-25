import {Link} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import {Moon, Pencil, Trash2} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import {getReadSpacesQueryKey, useDeleteSpace} from "@/api/generated/spaces/spaces.ts";
import {failureToastAction, successToast} from "@/lib/toasts.ts";
import {useQueryClient} from "@tanstack/react-query";
import {useTranslation} from "react-i18next";
import {useCallback} from "react";

type SpacesListItem = {
    spaceId: number,
    name: string,
    description: string
}

function SpacesListItem({spaceId, name, description} : SpacesListItem) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();

    const submitDelete = useCallback(() => {
        deleteSpace.mutate({spaceId});
    }, [])

    const deleteSpace = useDeleteSpace({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({queryKey: getReadSpacesQueryKey()});
                successToast(t("spaces.delete.success"));
            },
            onError: async () => {
                failureToastAction(t("spaces.delete.error"), <Button onClick={submitDelete}>t("button.retry")</Button>)
            }
        }
    })


    return (
        <div
            key={spaceId}
            className="flex flex-row items-center justify-between rounded-lg border border-border p-4 hover:bg-muted/50 transition-colors"
        >
            <Link
                to={PathConstants.SPACE}
                params={{ spaceId: String(spaceId) }}
                className="flex flex-1 flex-row items-center gap-4"
            >
                <Moon className="shrink-0" />
                <div className="flex flex-col">
                    <p className="font-medium">{name}</p>
                    <p className="text-sm text-muted-foreground">{description}</p>
                </div>
            </Link>

            <div className="flex flex-row gap-2">
                <Button variant="ghost" size="icon" aria-label="Edit space">
                    <Pencil className="size-4" />
                </Button>
                <Button onClick={submitDelete} variant="ghost" size="icon" aria-label="Delete space">
                    <Trash2 className="size-4" />
                </Button>
            </div>
        </div>
    )
}

export default SpacesListItem
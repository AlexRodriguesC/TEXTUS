import SpaceMap from "@/pages/space/SpaceMap.tsx";
import useDocuments from "@/hooks/useDocuments.tsx";
import {useParams} from "@tanstack/react-router";
import Upload from "@/pages/space/Upload.tsx";
import {Card} from "@/components/ui/card.tsx";
import DocumentStatusList from "@/pages/space/DocumentStatusList.tsx";
import {Button} from "@/components/ui/button.tsx";
import useMindMap from "@/hooks/useMindMap.tsx";
import {useTranslation} from "react-i18next";
import {Spinner} from "@/components/ui/spinner.tsx";

const wrapStyles = "mx-auto flex min-h-[100%] max-h-[100%] flex-row items-center gap-4";

function SpacePage() {
    const { spaceId } = useParams({
        from: "/main/spaces/$spaceId",
    });

    const { t } = useTranslation();

    const { documents } = useDocuments(Number(spaceId));

    const { extractMindMap, status, mindMap } = useMindMap(Number(spaceId));

    if (documents.length == 0) {
        return (
            <div className={wrapStyles}>
                <Upload showHeaders={true}/>
            </div>
        )
    }


    return (
        <div className={wrapStyles}>
            <div className="flex w-full h-full flex-row gap-4 items-stretch">
                <aside className="min-w-[25%] max-w-[25%]">
                    <Card className="bg-muted/50 h-full p-4 justify-between">
                        <div className={"flex flex-col gap-4"}>
                            <DocumentStatusList></DocumentStatusList>
                            <Upload showHeaders={false}/>
                        </div>
                        <div className={"w-full"}>
                            <Button className={"w-full"} onClick={() => extractMindMap.mutate({spaceId})} disabled={status == "GENERATING"}>
                                <div className={"flex flex-row gap-8"}>
                                    {t("mindmap.button")}
                                    {status == "GENERATING" && <Spinner/>}
                                </div>
                            </Button>
                        </div>
                    </Card>
                </aside>
                <SpaceMap />
            </div>
        </div>
    )
}

export default SpacePage;

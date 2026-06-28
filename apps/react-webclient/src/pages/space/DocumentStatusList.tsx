import {useGetDocumentStatuses} from "@/api/generated/spaces/spaces.ts";
import {useParams} from "@tanstack/react-router";
import DocumentStatus from "./DocumentStatus";
import {Card, CardContent, CardTitle} from "@/components/ui/card.tsx";

function DocumentStatusList() {
    const { spaceId } = useParams({
        from: "/main/spaces/$spaceId",
    });

    const { data, isSuccess } = useGetDocumentStatuses(Number(spaceId), {
        query: {
            refetchInterval: (query) => {
                const documents = query.state.data?.data.documents ?? [];
                const allDone = documents.length > 0 && documents.every((doc) => doc.status === "DONE");
                return allDone ? false : 2000;
            },
            refetchIntervalInBackground: true,
        },
    });

    const documentStatuses = isSuccess ? (data?.data.documents ?? []) : [];

    return (
        <Card className={"p-4"}>
            <CardTitle>Files:</CardTitle>
            <CardContent className={"flex flex-col gap-2"}>
                {documentStatuses.length > 0 &&
                    documentStatuses.map((e) => (
                        e.id && e.status && <DocumentStatus key={e.id} id={e.id} status={e.status} />
                    ))}
            </CardContent>
        </Card>
    );
}

export default DocumentStatusList;

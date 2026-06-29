import {
    useGetDocumentStatuses
} from "@/api/generated/spaces/spaces.ts";


export interface Document {
    id: number;
    status: "RECEIVED" | "DONE" | "PROCESSING";
}

function useDocuments(spaceId: number) {
    const { data:documentStatuses } = useGetDocumentStatuses(spaceId);

    let documents : Document[] = [];

    if (documentStatuses && documentStatuses.data && documentStatuses.data.documents) {
        documents = documentStatuses.data.documents;
    }

    return {
        documents
    }
}

export default useDocuments;

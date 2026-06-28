import {DocumentResponseDTOStatus} from "@/api/model";
import {Check, FileDown} from "lucide-react";
import {useCallback} from "react";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {useTranslation} from "react-i18next";
import {Spinner} from "@/components/ui/spinner.tsx";

export type DocumentStatus = {
    id: number,
    status: DocumentResponseDTOStatus
}

function DocumentStatus({id, status} : DocumentStatus) {
    const { t } = useTranslation();


    const statusToIcon = useCallback((status: DocumentResponseDTOStatus) => {
        if (status == "DONE") return <Check className={"size-4"}/>
        if (status == "PROCESSING") return <Spinner className={"size-4"}/>
        if (status == "RECEIVED") return <FileDown className={"size-4"}/>
    }, []);

    const statusToTooltip = useCallback((status: DocumentResponseDTOStatus) => {
        if (status == "DONE") return t("document.status.tooltip.done");
        if (status == "PROCESSING") return t("document.status.tooltip.loading");
        if (status == "RECEIVED") return t("document.status.tooltip.received");
    }, [t]);

    return (
        <div key={id} className={"flex flex-row items-center gap-2"}>
            <div>Status for file {id}: </div>
            {
                <Tooltip>
                    <TooltipTrigger asChild>
                        {statusToIcon(status)}
                    </TooltipTrigger>
                    <TooltipContent>
                        {statusToTooltip(status)}
                    </TooltipContent>
                </Tooltip>
            }
        </div>

    );
}

export default DocumentStatus;

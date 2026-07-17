import type {TopicResponseDTO} from "@/client/model";
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTrigger} from "@/components/ui/dialog.tsx";
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import {Separator} from "@/components/ui/separator.tsx";
import {useTranslation} from "react-i18next";

function TopicInformationModal({topic}: {topic: TopicResponseDTO}) {
    const { t } = useTranslation();

    return (
        <Dialog>
            <DialogTrigger asChild>
                <InformationQuestionMark tooltip={t("exercise.guide.tooltip")}/>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <p className={"text-muted-foreground text-xl font-bold uppercase"}>
                        {t("exercise.guide.title")}
                    </p>
                </DialogHeader>
                <DialogDescription>
                    <div className={"flex flex-col gap-4"}>
                        <p className={"text-muted-foreground text-xl font-bold uppercase"}>
                            {t("general.title")}
                        </p>
                        <>{topic.title}</>
                        <Separator orientation={"horizontal"} />
                        <p className={"text-muted-foreground text-xl font-bold uppercase"}>
                            {t("general.summary")}
                        </p>
                        <>{topic.summary}</>
                    </div>
                </DialogDescription>
            </DialogContent>
        </Dialog>
    )
}

export default TopicInformationModal;

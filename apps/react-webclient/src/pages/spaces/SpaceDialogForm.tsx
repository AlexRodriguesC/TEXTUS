import {
    Dialog,
    DialogClose,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog.tsx";
import {Field, FieldGroup} from "@/components/ui/field";
import {Label} from "@/components/ui/label";
import {Input} from "@/components/ui/input.tsx";
import {Textarea} from "@/components/ui/textarea";
import {Button} from "@/components/ui/button.tsx";
import * as React from "react";
import {useTranslation} from "react-i18next";

type SpaceDialogFormProps = {
    id: string,
    trigger?: React.ReactNode;
    title: string;
    descriptionText: string;
    submitLabel: string;
    cancelLabel: string;
    name: string;
    onNameChange: (value: string) => void;
    description: string;
    onDescriptionChange: (value: string) => void;
    onCancel?: () => void;
    onSubmit: () => void;
    pending?: boolean;
};

function SpaceDialogForm({
    id,
                             trigger,
                             title,
                             descriptionText,
                             submitLabel,
                             cancelLabel,
                             name,
                             description,
                             onNameChange,
                             onDescriptionChange,
                             onCancel,
                             onSubmit,
                         }: SpaceDialogFormProps) {

    const {t} = useTranslation();

    return (
        <Dialog>
            <form
                id={id}
                onSubmit={(e) => {
                    e.preventDefault();
                    onSubmit();
                }}
            >
                {trigger ? <DialogTrigger asChild>{trigger}</DialogTrigger> : null}
                <DialogContent showCloseButton={false}>
                    <DialogHeader>
                        <DialogTitle>{title}</DialogTitle>
                        <DialogDescription>{descriptionText}</DialogDescription>
                    </DialogHeader>

                    <FieldGroup>
                        <Field>
                            <Label htmlFor="space-name">{t("space.name")}</Label>
                            <Input
                                id="space-name"
                                name="name"
                                value={name}
                                onChange={(e) => onNameChange(e.target.value)}
                            />
                        </Field>

                        <Field>
                            <Label htmlFor="space-description">{t("space.description")}</Label>
                            <Textarea
                                id="space-description"
                                name="description"
                                value={description}
                                onChange={(e) => onDescriptionChange(e.target.value)}
                            />
                        </Field>
                    </FieldGroup>

                    <DialogFooter>
                        <DialogClose asChild>
                            <Button type="submit" form={id}>
                                {submitLabel}
                            </Button>
                        </DialogClose>

                        <DialogClose asChild>
                            <Button type="button" variant="outline" onClick={onCancel}>
                                {cancelLabel}
                            </Button>
                        </DialogClose>

                    </DialogFooter>
                </DialogContent>
            </form>
        </Dialog>
    );
}

export default SpaceDialogForm;
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

type SpaceDialogFormProps = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    trigger?: React.ReactNode;
    title: string;
    descriptionText: string;
    submitLabel: string;
    cancelLabel: string;
    name: string;
    description: string;
    onNameChange: (value: string) => void;
    onDescriptionChange: (value: string) => void;
    onCancel: () => void;
    onSubmit: () => void;
    pending?: boolean;
};

function SpaceDialogForm({
                             open,
                             onOpenChange,
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
                             pending = false,
                         }: SpaceDialogFormProps) {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>

            <form
                id="space-form"
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
                            <Label htmlFor="space-name">Name</Label>
                            <Input
                                id="space-name"
                                name="name"
                                value={name}
                                onChange={(e) => onNameChange(e.target.value)}
                            />
                        </Field>

                        <Field>
                            <Label htmlFor="space-description">Description</Label>
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
                            <Button type="button" variant="outline" onClick={onCancel}>
                                {cancelLabel}
                            </Button>
                        </DialogClose>

                        <Button type="submit" form="space-form" disabled={pending}>
                            {submitLabel}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </form>
        </Dialog>
    );
}

export default SpaceDialogForm;
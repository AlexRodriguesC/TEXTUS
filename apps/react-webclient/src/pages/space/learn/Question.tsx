import {Button} from "@/components/ui/button.tsx";

function Question() {

    return (
        <div>
            <p>
                {"Who's Pippo?"}
            </p>
            <div className={"grid grid-cols-2 grid-rows-2 gap-4"}>
                <Button></Button>
                <Button></Button>
                <Button></Button>
                <Button></Button>
            </div>
        </div>
    )
}

export default Question;

import Upload from "@/pages/space/Upload.tsx";

function SpacePage() {



    return (
        <div className={"mx-auto flex w-full max-w-[75%] min-h-[80vh] max-h-[80vh] flex-col items-center place-content-center gap-4"}>
            <div className={"min-w-[50%] max-w-[50%]"}>
                <Upload></Upload>
            </div>
        </div>
    )
}

export default SpacePage;

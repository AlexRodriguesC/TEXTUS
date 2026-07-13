import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import DocumentStatusList from "@/pages/space/DocumentStatusList.tsx";
import UploadZone from "@/components/UploadZone.tsx";

function SpaceMenu() {

    return (
        <CenteredPage>
            <div className="flex flex-col md:flex-row w-full gap-6 items-center place-content-center">
                <ResponsiveLottie src={"/animations/menu.lottie"}/>

                <div className="flex flex-col max-w-[80%] gap-4">
                    <UploadZone/>

                    <div className={"flex flex-col w-full items-center"}>
                        <DocumentStatusList />
                    </div>
                </div>
            </div>
        </CenteredPage>
    )
}

export default SpaceMenu;
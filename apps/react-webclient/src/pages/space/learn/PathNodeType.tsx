import MotionButton from "@/components/MotionButton.tsx";
import * as React from "react";
import type {LessonResponseDTO} from "@/client/model";


function PathNode({node, offset, onClick, disabled}: { node: LessonResponseDTO; offset: number; onClick: () => void; disabled?: boolean }) {

    return (
        <div className={"flex justify-center"} style={{transform: `translateX(${offset}px)`}}>
            <MotionButton disabled={disabled} onClick={onClick}>
                {getNodeIcon(node.type)}
            </MotionButton>
        </div>
    );
}

export default PathNode;

function getNodeIcon(type: LessonResponseDTO["type"]): React.ReactNode {
    switch (type) {
        case "CHECKPOINT":
            return <img className="w-fit h-fit" src="/checkpoint.png" alt="trophy"/>;
        case "LESSON":
            return <img src="/lesson.png" alt="star"/>;
    }
}


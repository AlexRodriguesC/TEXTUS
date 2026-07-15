import MotionButton from "@/components/MotionButton.tsx";
import * as React from "react";

export interface PathNodeType {
    topics: number[];
    type: "star" | "trophy";
}


function PathNode({node, offset}: { node: PathNodeType; offset: number }) {

    return (
        <div className={"flex justify-center"} style={{transform: `translateX(${offset}px)`}}>
            <MotionButton>
                {getNodeIcon(node.type)}
            </MotionButton>
        </div>
    );
}

export default PathNode;

function getNodeIcon(type: PathNodeType["type"]): React.ReactNode {
    switch (type) {
        case "trophy":
            return <img className="w-fit h-fit" src="/checkpoint.png" alt="trophy"/>;
        case "star":
            return <img src="/lesson.png" alt="star"/>;
    }
}


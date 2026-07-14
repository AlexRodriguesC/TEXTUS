import type {TopicResponseDTO} from "@/client/model";

interface TopicGuideProps {
    topic: TopicResponseDTO
}

function TopicGuide({ topic }: TopicGuideProps) {

    return (
        <>
            <h1 className={"font-bold"}>{topic.title}</h1>
            <p>{topic.summary}</p>
        </>
    )
}

export default TopicGuide;
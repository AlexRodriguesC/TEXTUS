import type Topic from "@/classes/Topic.ts";
import TopicEntry from "@/pages/space/topics/TopicEntry.tsx";
import type { RefObject } from "react";

interface TopicEntryListProps {
  topics: Topic[];
  itemRef?: RefObject<HTMLDivElement | null>;
}

function TopicEntryList({ topics, itemRef }: TopicEntryListProps) {
  return (
    <div className="flex flex-col gap-4">
      {topics.map((topic, index) => (
        <div key={topic.id} ref={index === 0 ? itemRef : undefined}>
          <TopicEntry topic={topic} />
        </div>
      ))}
    </div>
  );
}

export default TopicEntryList;

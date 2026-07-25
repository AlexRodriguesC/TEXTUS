import SpacesListItem from "@/pages/spaces/SpacesListItem.tsx";
import type Space from "@/classes/Space.ts";

interface SpacesListProps {
  spaces: Space[];
}

function SpacesList({ spaces }: SpacesListProps) {
  return (
    <div className="flex flex-1 min-h-0 flex-col min-w-full overflow-y-auto gap-2">
      {spaces.map((space) => (
        <SpacesListItem
          key={space.spaceId}
          icon={space.icon}
          spaceId={space.spaceId}
          name={space.name}
          description={space.description}
        />
      ))}
    </div>
  );
}

export default SpacesList;

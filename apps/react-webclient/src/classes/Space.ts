class Space {
  spaceId: number;
  icon: string;
  name: string;
  description: string;

  constructor(
    spaceId: number,
    icon: string,
    name: string,
    description: string,
  ) {
    this.spaceId = spaceId;
    this.icon = icon;
    this.name = name;
    this.description = description;
  }
}

export default Space;

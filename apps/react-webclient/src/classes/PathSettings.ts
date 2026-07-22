class PathSettings {
  questionPercentage: number;
  listPercentage: number;
  pairPercentage: number;
  groupPercentage: number;

  constructor(
    questionPercentage: number,
    listPercentage: number,
    pairPercentage: number,
    groupPercentage: number,
  ) {
    this.questionPercentage = questionPercentage;
    this.listPercentage = listPercentage;
    this.pairPercentage = pairPercentage;
    this.groupPercentage = groupPercentage;
  }
}

export default PathSettings;

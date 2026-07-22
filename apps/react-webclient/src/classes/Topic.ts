class Topic {
  id: number;
  sourceFileName: string;
  page: number;
  title: string;
  summary: string;
  labels: string[];

  constructor(
    id: number,
    sourceFileName: string,
    page: number,
    title: string,
    summary: string,
    labels: string[],
  ) {
    this.id = id;
    this.sourceFileName = sourceFileName;
    this.page = page;
    this.title = title;
    this.summary = summary;
    this.labels = labels;
  }
}

export default Topic;

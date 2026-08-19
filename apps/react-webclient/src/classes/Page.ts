class Page {
  id: number;
  documentId: number;
  title: string;
  summary: string;

  constructor(
    id: number,
    documentId: number,
    title: string,
    summary: string,
  ) {
    this.id = id;
    this.documentId = documentId;
    this.title = title;
    this.summary = summary;
  }
}

export default Page;

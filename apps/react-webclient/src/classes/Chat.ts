class Chat {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;

  constructor(
    id: string,
    title: string,
    createdAt: string,
    updatedAt: string,
  ) {
    this.id = id;
    this.title = title;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}

export default Chat;

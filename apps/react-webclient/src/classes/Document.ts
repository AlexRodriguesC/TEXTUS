import Task from "@/classes/Task.ts";

class Document {
  name: string;
  id: number;
  tasks: Task[];

  constructor(name: string, id: number, tasks: Task[]) {
    this.name = name;
    this.id = id;
    this.tasks = tasks;
  }
}

export default Document;

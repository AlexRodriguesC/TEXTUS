import Task from "@/classes/Task.ts";

class Document {
  name: string;
  id: number;
  orderIndex: number;
  tasks: Task[];

  constructor(name: string, id: number, tasks: Task[], orderIndex = 0) {
    this.name = name;
    this.id = id;
    this.tasks = tasks;
    this.orderIndex = orderIndex;
  }
}

export default Document;

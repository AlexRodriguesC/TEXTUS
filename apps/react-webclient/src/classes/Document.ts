import type { DocumentResponseDTO } from "@/client/model";
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

  static from(dto: DocumentResponseDTO): Document {
    return new Document(dto.name, dto.id, dto.tasks.map(Task.from));
  }
}

export default Document;

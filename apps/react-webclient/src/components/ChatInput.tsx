import { useState } from "react";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ArrowUp } from "lucide-react";

interface ChatInputProps {
  onSubmit: (message: string) => void;
  models: { value: string; label: string }[];
  defaultModel?: string;
  onModelChange?: (model: string) => void;
}

function ChatInput({
  onSubmit,
  models,
  defaultModel,
  onModelChange,
}: ChatInputProps) {
  const [value, setValue] = useState("");
  const [model, setModel] = useState(defaultModel ?? models[0]?.value);

  const handleSend = () => {
    if (!value.trim()) return;
    onSubmit(value);
    setValue("");
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="w-full max-w-2xl rounded-2xl border bg-muted/30 p-3 flex flex-col gap-2">
      <Textarea
        value={value}
        onChange={(e) => setValue(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="Ask anything..."
        className="resize-none border-0 shadow-none focus-visible:ring-0 bg-transparent min-h-10 max-h-40"
      />

      <div className="flex items-center gap-2 justify-end">
        <Select
          value={model}
          onValueChange={(v) => {
            setModel(v);
            onModelChange?.(v);
          }}
        >
          <SelectTrigger className="h-8 border-0 shadow-none w-auto gap-1">
            <SelectValue />
          </SelectTrigger>
          <SelectContent align="end">
            {models.map((m) => (
              <SelectItem key={m.value} value={m.value}>
                {m.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Button
          size="icon"
          onClick={handleSend}
          disabled={!value.trim()}
          className="rounded-full h-8 w-8"
        >
          <ArrowUp className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}

export default ChatInput;

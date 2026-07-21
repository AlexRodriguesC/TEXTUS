import { useMemo, useState } from "react";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ArrowRight } from "lucide-react";
import {
  persistChatModel,
  readStoredChatModel,
  resolveChatModel,
} from "@/lib/chatModelStorage.ts";

interface ChatInputProps {
  onSubmit: (message: string, model?: string) => void;
  models: { value: string; label: string }[];
  defaultModel?: string;
  disabled?: boolean;
}

function ChatInput({
  onSubmit,
  models,
  defaultModel,
  disabled = false,
}: ChatInputProps) {
  const [value, setValue] = useState("");
  const [preferredModel, setPreferredModel] = useState<string | null>(
    readStoredChatModel,
  );

  const modelValues = useMemo(
    () => models.map((entry) => entry.value),
    [models],
  );

  const model = useMemo(
    () => resolveChatModel(modelValues, preferredModel, defaultModel),
    [defaultModel, modelValues, preferredModel],
  );

  const handleModelChange = (nextModel: string) => {
    persistChatModel(nextModel);
    setPreferredModel(nextModel);
  };

  const handleSend = () => {
    if (!value.trim() || disabled || !model) {
      return;
    }

    persistChatModel(model);
    setPreferredModel(model);
    onSubmit(value, model);
    setValue("");
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="w-full max-w-3xl rounded-md border bg-muted/30 p-3 flex flex-col gap-2">
      <Textarea
        value={value}
        onChange={(e) => setValue(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="Ask anything..."
        disabled={disabled}
        className="resize-none border-0 shadow-none focus-visible:ring-0 bg-transparent min-h-10 max-h-40 overflow-y-auto"
      />

      <div className="flex items-center gap-2 justify-end">
        <Select
          value={model}
          onValueChange={handleModelChange}
          disabled={disabled || models.length === 0}
        >
          <SelectTrigger className="h-8 border-0 shadow-none w-auto gap-1">
            <SelectValue />
          </SelectTrigger>
          <SelectContent align="end">
            {models.map((entry) => (
              <SelectItem key={entry.value} value={entry.value}>
                {entry.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Button
          size="icon"
          onClick={handleSend}
          disabled={disabled || !value.trim()}
          className="h-8 w-8 rounded-full"
        >
          <ArrowRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}

export default ChatInput;

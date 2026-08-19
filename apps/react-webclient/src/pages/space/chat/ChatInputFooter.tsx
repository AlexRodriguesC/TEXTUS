import ChatInput from "@/components/ChatInput.tsx";

// Bottom-centered chat input footer used under the message list.
function ChatInputFooter({
  onSubmit,
  models,
  defaultModel,
  disabled,
}: {
  onSubmit: (message: string, model?: string) => void;
  models: { value: string; label: string }[];
  defaultModel?: string;
  disabled?: boolean;
}) {
  return (
    <div className="w-full flex items-center justify-center relative">
      <div className="flex py-3 w-full shrink-0 place-content-center">
        <ChatInput
          onSubmit={onSubmit}
          models={models}
          defaultModel={defaultModel}
          disabled={disabled}
        />
      </div>
    </div>
  );
}

export default ChatInputFooter;

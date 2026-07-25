import useChatContext from "@/hooks/context/useChatContext.ts";
import useChatStream from "@/hooks/useChatStream.ts";
import useChats from "@/hooks/useChats.ts";
import useModels from "@/hooks/useModels.ts";
import SpaceChatConversation from "@/pages/space/chat/SpaceChatConversation.tsx";
import SpaceChatEmpty from "@/pages/space/chat/SpaceChatEmpty.tsx";
import SpaceChatNoSelection from "@/pages/space/chat/SpaceChatNoSelection.tsx";
import SpaceChatSkeleton from "@/pages/space/chat/SpaceChatSkeleton.tsx";

function SpaceChat() {
  const { chats, isLoading: isChatsLoading, invalidateChats } = useChats();
  const { models, defaultModel, isLoading: isModelsLoading } = useModels();
  const { sendMessage, isStreaming } = useChatStream();
  const { selectedChatId } = useChatContext();

  const selectedChat = chats.find((chat) => chat.id === selectedChatId);
  const isLoading = isChatsLoading || isModelsLoading;

  const renderContent = () => {
    if (isLoading) {
      return <SpaceChatSkeleton />;
    }
    if (chats.length === 0) {
      return <SpaceChatEmpty />;
    }
    if (selectedChatId) {
      return (
        <SpaceChatConversation
          key={selectedChatId}
          chatId={selectedChatId}
          chatTitle={selectedChat?.title}
          models={models}
          defaultModel={defaultModel}
          sendMessage={sendMessage}
          isStreaming={isStreaming}
          invalidateChats={invalidateChats}
        />
      );
    }
    return (
      <SpaceChatNoSelection models={models} defaultModel={defaultModel} />
    );
  };

  return (
    <div className="flex w-full h-full overflow-hidden">
      <div className="flex flex-col flex-1 min-h-0 items-center overflow-hidden">
        <div className="w-full h-full md:max-w-[80%] lg:max-w-[60%] flex flex-col min-h-0 overflow-hidden">
          {renderContent()}
        </div>
      </div>
    </div>
  );
}

export default SpaceChat;

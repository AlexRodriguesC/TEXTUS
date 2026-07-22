const CHAT_MODEL_STORAGE_KEY = "chat-selected-model";

function isValidModel(model: string | null | undefined, modelValues: string[]) {
  return model != null && modelValues.includes(model);
}

export function readStoredChatModel(): string | null {
  return localStorage.getItem(CHAT_MODEL_STORAGE_KEY);
}

export function persistChatModel(model: string) {
  localStorage.setItem(CHAT_MODEL_STORAGE_KEY, model);
}

export function resolveChatModel(
  modelValues: string[],
  preferredModel: string | null,
  defaultModel?: string,
): string {
  if (isValidModel(preferredModel, modelValues)) {
    return preferredModel;
  }

  const storedModel = readStoredChatModel();
  if (isValidModel(storedModel, modelValues)) {
    return storedModel;
  }

  if (isValidModel(defaultModel, modelValues)) {
    return defaultModel;
  }

  return modelValues[0] ?? "";
}

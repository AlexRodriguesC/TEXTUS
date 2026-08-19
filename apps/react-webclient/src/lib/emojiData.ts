import en from "emoji-picker-react/dist/data/emojis-en";
import it from "emoji-picker-react/dist/data/emojis-it";
import type { EmojiData } from "emoji-picker-react/dist/types/exposedTypes";

export function mapEmojiDataToLanguage(language: string): EmojiData {
  switch (language) {
    case "en":
      return en;
    case "it":
      return it;
    default:
      throw new Error(
        "Translations for emoji picker not found. Did you update the languages after adding new locales?",
      );
  }
}

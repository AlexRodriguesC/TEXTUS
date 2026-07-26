import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";
import EmojiPicker, { Emoji, Theme } from "emoji-picker-react";
import { useTheme } from "@/hooks/context/useTheme.ts";
import { mapEmojiDataToLanguage } from "@/lib/emojiData.ts";
import { useState } from "react";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover.tsx";

interface SpaceEmojiPickerProps {
  icon: string;
  onIconChange: (value: string) => void;
}

function SpaceEmojiPicker({ icon, onIconChange }: SpaceEmojiPickerProps) {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const { i18n } = useTranslation();
  const [open, setOpen] = useState(false);
  const emojiData = mapEmojiDataToLanguage(
    i18n.resolvedLanguage ? i18n.language : "",
  );

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className="h-10 w-full justify-start text-2xl"
          aria-label={t("space.icon")}
        >
          <Emoji unified={icon ?? "1f929"} />
        </Button>
      </PopoverTrigger>
      <PopoverContent
        onWheel={(e) => e.stopPropagation()}
        className="w-fit h-fit bg-transparent border-none shadow-none p-0"
      >
        <EmojiPicker
          emojiData={emojiData}
          height={350}
          width={300}
          skinTonesDisabled
          theme={theme === "light" ? Theme.LIGHT : Theme.DARK}
          onEmojiClick={(e) => {
            onIconChange(e.unified);
            setOpen(false);
          }}
        />
      </PopoverContent>
    </Popover>
  );
}

export default SpaceEmojiPicker;

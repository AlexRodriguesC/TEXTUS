import { Cog } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import { Slider } from "@/components/ui/slider.tsx";
import { Skeleton } from "@/components/ui/skeleton.tsx";
import { useTranslation } from "react-i18next";
import { useState } from "react";
import useLearningPath, {
} from "@/hooks/useLearningPath.ts";
import type LearningPath from "@/classes/LearningPath.ts";
import type { LearningPathSettings } from "@/classes/LearningPath.ts";
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import { Button } from "@/components/ui/button.tsx";

const DISPLAY_NAMES = ["question", "list", "group", "pair"] as const;
type DisplayName = (typeof DISPLAY_NAMES)[number];

const DISPLAY_TO_KEY: Record<DisplayName, keyof LearningPathSettings> = {
  question: "questionPercentage",
  list: "listPercentage",
  group: "groupPercentage",
  pair: "pairPercentage",
};

function PathSettings() {
  const { path, isPathLoading } = useLearningPath();
  const [dialogOpen, setDialogOpen] = useState(false);

  const showSkeleton = isPathLoading || !path;

  return (
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogTrigger asChild>
          <Cog className="cursor-pointer" />
        </DialogTrigger>
        <DialogContent showCloseButton={false}>
          {showSkeleton ? (
              <PathSettingsSkeleton onCancel={() => setDialogOpen(false)} />
          ) : (
              <PathSettingsContent
                  path={path}
                  onClose={() => setDialogOpen(false)}
              />
          )}
        </DialogContent>
      </Dialog>
  );
}

function PathSettingsContent({
                               path,
                               onClose,
                             }: {
  path: LearningPath;
  onClose: () => void;
}) {
  const { t } = useTranslation();
  const [percentages, setPercentages] = useState<LearningPathSettings>(
      path.settings,
  );

  const handleSliderChange = (type: DisplayName, newValue: number) => {
    const key = DISPLAY_TO_KEY[type];
    setPercentages((prev) => {
      const othersTotal = (Object.keys(prev) as (keyof LearningPathSettings)[])
          .filter((k) => k !== key)
          .reduce((sum, k) => sum + (prev[k] ?? 0), 0);
      const newTotal = othersTotal + newValue;
      if (newTotal > 100) {
        const clamped = 100 - othersTotal;
        return { ...prev, [key]: Math.max(0, clamped) };
      }
      return { ...prev, [key]: newValue };
    });
  };

  const handleApply = () => {
    path.updateSettings(percentages);
    onClose();
  };

  const handleCancel = () => {
    setPercentages(path.settings);
    onClose();
  };

  return (
      <>
        <DialogHeader>
          <h1>{t("sidebar.settings.path.title")}</h1>
        </DialogHeader>
        <div className="flex flex-col gap-6 p-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">
              {t("sidebar.settings.path.exerciseTypes")}
            </h2>
            <div className="flex items-center text-muted-foreground">
              <InformationQuestionMark
                  tooltip={t("sidebar.settings.path.changes")}
              />
            </div>
          </div>
          <div className="flex flex-col gap-2">
            {DISPLAY_NAMES.map((type) => {
              const key = DISPLAY_TO_KEY[type];
              return (
                  <div key={type} className="flex items-center gap-4">
                <span className="w-20 text-sm font-medium capitalize">
                  {t(`sidebar.settings.path.${type}`)}
                </span>
                    <Slider
                        value={[percentages[key] ?? 0]}
                        onValueChange={(v) => handleSliderChange(type, v[0])}
                        min={0}
                        max={100}
                        step={5}
                        className="flex-1"
                    />
                    <span className="w-10 text-right text-sm tabular-nums">
                  {percentages[key] ?? 0}%
                </span>
                  </div>
              );
            })}
          </div>
        </div>
        <DialogFooter className="flex flex-row w-full justify-between md:justify-between">
          <Button variant="secondary" onClick={handleCancel}>
            {t("button.cancel")}
          </Button>
          <Button onClick={handleApply}>{t("button.apply")}</Button>
        </DialogFooter>
      </>
  );
}

function PathSettingsSkeleton({ onCancel }: { onCancel: () => void }) {
  const { t } = useTranslation();

  return (
      <>
        <DialogHeader>
          <h1>{t("sidebar.settings.path.title")}</h1>
        </DialogHeader>
        <div className="flex flex-col gap-6 p-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">
              {t("sidebar.settings.path.exerciseTypes")}
            </h2>
            <div className="flex items-center text-muted-foreground">
              <InformationQuestionMark
                  tooltip={t("sidebar.settings.path.changes")}
              />
            </div>
          </div>
          <div className="flex flex-col gap-2">
            {DISPLAY_NAMES.map((type) => (
                <div key={type} className="flex items-center gap-4">
              <span className="w-20 text-sm font-medium capitalize">
                {t(`sidebar.settings.path.${type}`)}
              </span>
                  <Skeleton className="flex-1 h-6 rounded-full" />
                  <Skeleton className="w-10 h-5" />
                </div>
            ))}
          </div>
        </div>
        <DialogFooter className="flex flex-row w-full justify-between md:justify-between">
          <Button variant="secondary" onClick={onCancel}>
            {t("button.cancel")}
          </Button>
          <Button disabled>{t("button.apply")}</Button>
        </DialogFooter>
      </>
  );
}

export default PathSettings;
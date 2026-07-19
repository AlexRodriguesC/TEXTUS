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
  type LearningPathSettings,
} from "@/hooks/useLearningPath.ts";
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

const DEFAULT_SETTINGS: LearningPathSettings = {
  questionPercentage: 0,
  listPercentage: 0,
  groupPercentage: 0,
  pairPercentage: 0,
};

function PathSettings() {
  const { t } = useTranslation();
  const { path, isPathLoading, updateSettings } = useLearningPath();

  const [percentages, setPercentages] = useState<LearningPathSettings>(
    path?.settings ?? DEFAULT_SETTINGS,
  );
  const [dialogOpen, setDialogOpen] = useState(false);

  const [prevIsPathLoading, setPrevIsPathLoading] = useState(isPathLoading);
  if (isPathLoading !== prevIsPathLoading) {
    setPrevIsPathLoading(isPathLoading);
    if (!isPathLoading) {
      setPercentages(path?.settings ?? DEFAULT_SETTINGS);
    }
  }

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
    updateSettings(percentages);
    setDialogOpen(false);
  };

  const handleCancel = () => {
    setPercentages(path?.settings ?? DEFAULT_SETTINGS);
    setDialogOpen(false);
  };

  const showSkeleton = isPathLoading || !path;

  return (
    <>
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogTrigger asChild>
          <Cog className="cursor-pointer" />
        </DialogTrigger>
        <DialogContent showCloseButton={false}>
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
                    {showSkeleton ? (
                      <Skeleton className="flex-1 h-6 rounded-full" />
                    ) : (
                      <Slider
                        value={[percentages[key] ?? 0]}
                        onValueChange={(v) => handleSliderChange(type, v[0])}
                        min={0}
                        max={100}
                        step={5}
                        className="flex-1"
                      />
                    )}
                    {showSkeleton ? (
                      <Skeleton className="w-10 h-5" />
                    ) : (
                      <span className="w-10 text-right text-sm tabular-nums">
                        {percentages[key] ?? 0}%
                      </span>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
          <DialogFooter
            className={
              "flex flex-row w-full justify-between md:justify-between"
            }
          >
            <Button variant={"secondary"} onClick={handleCancel}>
              {t("button.cancel")}
            </Button>
            <Button onClick={handleApply} disabled={showSkeleton}>
              {t("button.apply")}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

export default PathSettings;

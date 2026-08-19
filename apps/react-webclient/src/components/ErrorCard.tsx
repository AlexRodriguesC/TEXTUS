import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { XIcon } from "lucide-react";
import { useTranslation } from "react-i18next";
import CenteredPage from "@/components/CenteredPage.tsx";
import type { ReactNode } from "react";
import HomeLink from "@/components/HomeLink.tsx";

interface ErrorCardProps {
  messageKey: string;
  className?: string;
  children: ReactNode;
}

export function ErrorCard({
  messageKey,
  className = "max-w-md",
  children,
}: ErrorCardProps) {
  const { t } = useTranslation();

  return (
    <CenteredPage>
      <Card className={className}>
        <CardContent className="flex flex-row items-center place-content-center gap-8 w-full">
          <div className="flex flex-col items-center gap-4 w-full">
            <div className="flex w-full justify-end">
              <HomeLink />
            </div>
            <div className={"flex flex-row gap-2"}>
              <XIcon color="red" />
              <div>{t(messageKey)}</div>
            </div>
          </div>
        </CardContent>
        <CardFooter className="flex flex-col gap-2 w-full">
          {children}
        </CardFooter>
      </Card>
    </CenteredPage>
  );
}

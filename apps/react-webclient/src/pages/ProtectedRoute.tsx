import { useAuth } from "@/hooks/useAuth.ts";
import { useNavigate } from "@tanstack/react-router";
import { useEffect, type PropsWithChildren } from "react";
import PathConstants from "@/routing/pathConstants.ts";
import { successToast } from "@/lib/toasts.ts";
import { useTranslation } from "react-i18next";

function ProtectedRoute({ children }: PropsWithChildren) {
  const { t } = useTranslation();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate({ to: PathConstants.LOGIN }).then(() =>
          successToast(t("general.logout")),
      );
    }
  }, [isAuthenticated, navigate, t]);

  if (!isAuthenticated) {
    return null;
  }

  return <>{children}</>;
}

export default ProtectedRoute;
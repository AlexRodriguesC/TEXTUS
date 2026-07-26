import type { PropsWithChildren } from "react";

function ProtectedRoute({ children }: PropsWithChildren) {
  return <>{children}</>;
}

export default ProtectedRoute;

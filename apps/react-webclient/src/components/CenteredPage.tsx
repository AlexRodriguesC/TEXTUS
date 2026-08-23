import * as React from "react";

function CenteredPage({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex h-full min-h-0 w-full place-content-center place-items-center overflow-y-auto">
      {children}
    </div>
  );
}

export default CenteredPage;

import * as React from "react";

function CenteredPage({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex h-full w-full place-items-center place-content-center overflow-hidden">
      {children}
    </div>
  );
}

export default CenteredPage;

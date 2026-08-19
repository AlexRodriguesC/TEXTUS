import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import MotionPillButton from "@/components/MotionPillButton.tsx";

vi.mock("@/hooks/learning/useLearningPath.ts", () => ({
  default: () => ({ path: null }),
}));

vi.mock("@/hooks/useTopics.tsx", () => ({
  default: () => ({ topics: [] }),
}));

vi.mock("framer-motion", () => ({
  motion: {
    div: ({
      children,
      className,
      whileHover: _whileHover,
      whileTap: _whileTap,
      transition: _transition,
      ...props
    }: React.HTMLAttributes<HTMLDivElement> & {
      children?: React.ReactNode;
      whileHover?: unknown;
      whileTap?: unknown;
      transition?: unknown;
    }) => (
      <div className={className} {...props}>
        {children}
      </div>
    ),
  },
}));

describe("MotionPillButton", () => {
  afterEach(() => {
    cleanup();
  });

  it("renders primary and trailing controls as sibling buttons", () => {
    render(
      <MotionPillButton
        button={<button type="button">Toggle</button>}
      >
        <button type="button">Title</button>
      </MotionPillButton>,
    );

    const title = screen.getByRole("button", { name: "Title" });
    const toggle = screen.getByRole("button", { name: "Toggle" });

    expect(title).toBeInTheDocument();
    expect(toggle).toBeInTheDocument();
    expect(title.contains(toggle)).toBe(false);
    expect(toggle.contains(title)).toBe(false);
  });

  it("applies subdued opacity classes to the trailing control wrapper", () => {
    render(
      <MotionPillButton
        button={<button type="button">Toggle</button>}
      >
        <button type="button">Title</button>
      </MotionPillButton>,
    );

    const wrapper = screen.getByTestId("pill-trailing-control");
    expect(wrapper).toHaveClass("opacity-50");
    expect(wrapper).toHaveClass("group-hover:opacity-100");
    expect(wrapper).toHaveClass("group-focus-within:opacity-100");
    expect(wrapper).toHaveClass("transition-opacity");
  });

  it("renders primary content without an optional trailing control", () => {
    render(
      <MotionPillButton>
        <button type="button">Title only</button>
      </MotionPillButton>,
    );

    expect(screen.getByRole("button", { name: "Title only" })).toBeInTheDocument();
    expect(screen.queryByTestId("pill-trailing-control")).not.toBeInTheDocument();
  });
});

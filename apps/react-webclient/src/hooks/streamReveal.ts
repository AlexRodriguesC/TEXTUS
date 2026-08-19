const BASE_CHARS_PER_FRAME = 2;
const MAX_CHARS_PER_FRAME = 3;
const BACKLOG_SOFT_LIMIT = 40;

export type StreamReveal = {
  push: (text: string) => void;
  done: () => Promise<void>;
  cancel: () => void;
};

/**
 * Reveals streamed text at a readable pace via requestAnimationFrame.
 * Speeds up when backlog grows so catch-up stays smooth without dumping all text.
 */
export function createStreamReveal(
  onChunk: (chunk: string) => void,
  scheduleFrame: (cb: FrameRequestCallback) => number = requestAnimationFrame,
  cancelFrame: (id: number) => void = cancelAnimationFrame,
): StreamReveal {
  let pending = "";
  let frameId: number | null = null;
  let cancelled = false;
  let streamEnded = false;
  let resolveDone: (() => void) | null = null;

  const finishIfReady = () => {
    if (streamEnded && pending.length === 0 && resolveDone) {
      const resolve = resolveDone;
      resolveDone = null;
      resolve();
    }
  };

  const tick: FrameRequestCallback = () => {
    frameId = null;
    if (cancelled) {
      return;
    }

    if (pending.length === 0) {
      finishIfReady();
      return;
    }

    const charsThisFrame = Math.min(
      MAX_CHARS_PER_FRAME,
      BASE_CHARS_PER_FRAME +
        Math.floor(pending.length / BACKLOG_SOFT_LIMIT),
    );
    const chunk = pending.slice(0, charsThisFrame);
    pending = pending.slice(charsThisFrame);
    onChunk(chunk);

    if (pending.length > 0) {
      frameId = scheduleFrame(tick);
    } else {
      finishIfReady();
    }
  };

  const ensureScheduled = () => {
    if (cancelled || frameId !== null || pending.length === 0) {
      return;
    }
    frameId = scheduleFrame(tick);
  };

  return {
    push(text: string) {
      if (cancelled || !text) {
        return;
      }
      pending += text;
      ensureScheduled();
    },
    done() {
      streamEnded = true;
      if (cancelled || pending.length === 0) {
        return Promise.resolve();
      }
      return new Promise<void>((resolve) => {
        resolveDone = resolve;
        ensureScheduled();
      });
    },
    cancel() {
      cancelled = true;
      pending = "";
      if (frameId !== null) {
        cancelFrame(frameId);
        frameId = null;
      }
      if (resolveDone) {
        const resolve = resolveDone;
        resolveDone = null;
        resolve();
      }
    },
  };
}

export function removeIdFromAllSlots<T extends string>(
  assignments: Record<string, T[]>,
  id: T,
): Record<string, T[]> {
  const next: Record<string, T[]> = {};
  for (const key of Object.keys(assignments)) {
    next[key] = assignments[key].filter((existingId) => existingId !== id);
  }
  return next;
}

export function removeValueFromRecord<T extends string>(
  assignments: Record<string, T>,
  value: T,
): Record<string, T> {
  const next: Record<string, T> = {};
  for (const key of Object.keys(assignments)) {
    if (assignments[key] !== value) {
      next[key] = assignments[key];
    }
  }
  return next;
}

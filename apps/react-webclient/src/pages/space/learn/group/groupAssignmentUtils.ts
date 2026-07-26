export function resolveDropLabel(
  overId: string,
  assignments: Record<string, string[]>,
  labels: string[],
): string | "key-pool" | null {
  if (overId === "key-pool") return "key-pool";
  if (overId.startsWith("label-")) {
    const index = Number(overId.slice("label-".length));
    return labels[index] ?? null;
  }
  for (const label of labels) {
    if ((assignments[label] ?? []).includes(overId)) return label;
  }
  return null;
}

export function buildGroupAnswer(
  assignments: Record<string, string[]>,
  labels: string[],
  keyById: Map<string, string>,
): Record<string, string[]> {
  const answer: Record<string, string[]> = {};
  for (const label of labels) {
    answer[label] = (assignments[label] ?? []).map(
      (id) => keyById.get(id) as string,
    );
  }
  return answer;
}

export function getAssignedKeyStatus(
  id: string,
  label: string,
  submitted: boolean,
  assignments: Record<string, string[]>,
  correctGroups: Record<string, string[]>,
  keyById: Map<string, string>,
): "correct" | "incorrect" | null {
  if (!submitted) return null;
  const assignedIds = assignments[label] ?? [];
  const remaining = [...(correctGroups[label] ?? [])];
  for (const assignedId of assignedIds) {
    const value = keyById.get(assignedId) as string;
    const matchIndex = remaining.indexOf(value);
    if (assignedId === id) {
      return matchIndex >= 0 ? "correct" : "incorrect";
    }
    if (matchIndex >= 0) remaining.splice(matchIndex, 1);
  }
  return null;
}

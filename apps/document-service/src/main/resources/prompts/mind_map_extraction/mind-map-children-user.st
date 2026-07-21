Select the immediate direct child labelIds for PARENT_LABEL from REMAINING_LABELS.

Task:
Given PARENT_LABEL, REMAINING_LABELS, and LABEL_TOPIC_GROUPS, return the labelIds that should be attached as direct children of PARENT_LABEL.

Hard constraints:
- Every returned labelId must come from REMAINING_LABELS.
- Return only immediate direct children of PARENT_LABEL.
- Do not return labels that belong under a different branch.
- Do not return labels that should be deeper descendants of a child of PARENT_LABEL.
- Do not return the parent labelId.
- "labelIds" may be empty if no remaining label is a direct child of PARENT_LABEL.
- Output ONLY valid JSON, matching exactly the schema below.
- Output no prose, no markdown, no code fences.

How to choose:
- Compare each remaining label name against PARENT_LABEL.
- Prefer narrower, dependent, illustrative, or applied labels as direct children when they are conceptually contained by PARENT_LABEL.
- If a remaining label belongs under another branch, omit it.
- If a remaining label belongs under a child of PARENT_LABEL rather than directly under PARENT_LABEL, omit it.

Return only the final JSON object.
When writing string values, never use double quotes (") inside the text.
Use single quotes or rewrite the sentence to avoid quoting words entirely.

<format>

PARENT_LABEL:
<parent_label>

REMAINING_LABELS:
<remaining_labels>

LABEL_TOPIC_GROUPS:
<label_topic_groups>

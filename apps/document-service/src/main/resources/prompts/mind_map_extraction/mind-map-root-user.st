Select exactly one root labelId from INPUT_LABELS.

Task:
Given INPUT_LABELS and LABEL_TOPIC_GROUPS, return the single labelId that best represents the broadest subject.

Hard constraints:
- "labelId" must come from INPUT_LABELS.
- Do not invent any labelId.
- Output ONLY valid JSON, matching exactly the schema below.
- Output no prose, no markdown, no code fences.

How to choose:
- Prefer the broadest, most umbrella-like label name.
- Use LABEL_TOPIC_GROUPS as secondary evidence of topical scope.
- The root should be the label under which the other labels can plausibly be organized.

Return only the final JSON object.
When writing string values, never use double quotes (") inside the text.
Use single quotes or rewrite the sentence to avoid quoting words entirely.

<format>

INPUT_LABELS:
<labels>

LABEL_TOPIC_GROUPS:
<label_topic_groups>

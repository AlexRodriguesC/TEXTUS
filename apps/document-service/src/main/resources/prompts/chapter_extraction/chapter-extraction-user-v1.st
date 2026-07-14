You transform a list of labels into an ordered list of learning chapters.

Task:
Given INPUT_LABELS, return exactly one JSON array of chapter objects that uses every input labelId exactly once.

Primary evidence for grouping:
1. Semantic similarity between label text
2. Topic proximity and shared terminology

Secondary evidence:
3. Implied conceptual dependency between labels

Goal:
Build a pedagogically coherent, ordered sequence of chapters.
Each chapter groups labels that belong to the same coherent unit of learning.
The sequence must reflect natural learning progression: chapter n depends on chapter n-1 as a prerequisite.
Return a flat ordered list, not a tree or graph.

Hard constraints:
- Use every input labelId exactly once.
- Do not omit any labelId.
- Do not repeat any labelId.
- Do not invent any labelId.
- Do not invent any extra chapter beyond what the grouping requires.
- Each chapter object must have this exact shape:
{
"index": number,
"title": string,
"description": string,
"labelIds": [ number, ... ]
}
- "index" starts at 0 and increases sequentially with no gaps.
- "title" must be 2 to 3 words.
- "description" must be 6 to 7 words and summarize the chapter's focus.
- No keys other than "index", "title", "description", "labelIds" are allowed.
- Output ONLY valid JSON.
- Output no prose, no markdown, no code fences.

How to choose structure:
- Treat label text as the main signal for grouping; shared or overlapping terminology indicates the labels belong in the same chapter.
- Estimate topic proximity using vocabulary overlap and conceptual closeness.
- Order chapters from foundational or introductory topics to specific, advanced, or applied topics.
- If two labels are related but ambiguous in order, place the more general one in the earlier chapter.
- Keep each chapter focused on one coherent theme; avoid mixing unrelated concepts in the same chapter.
- Prefer 4 to 10 labels per chapter where possible.

Required internal procedure:
1. Read all labels.
2. Build a private coverage set containing every input labelId.
3. Cluster labelIds into coherent thematic groups.
4. Order groups by conceptual prerequisite progression.
5. Assign sequential index values, a short title, and a short description to each group.
6. Before answering, verify all of the following privately:
- every input labelId appears exactly once
- no labelId appears more than once
- no labelId is missing
- no labelId outside INPUT_LABELS appears
- indices are sequential starting at 0 with no gaps
- every chapter has title, description, and labelIds
- no extra keys are present
7. If any check fails, revise internally until all checks pass.
8. Return only the final JSON array.

Important:
Do not describe your reasoning.
Do not output the checklist.
Do not output intermediate work.
Return only the final JSON array.
When writing string values, never use double quotes (") inside the text.
Use single quotes or rewrite the sentence to avoid quoting words entirely.
Example: instead of `the vowel "a" is pronounced...`, write `the vowel a is pronounced...`

INPUT_LABELS:
<labels>
Some labelIds were missing from the original chapter construction. You are given the existing chapters, without their labelIds, and the list of missing labelIds. Decide which chapter each missing labelId belongs to.

Task:
Given CHAPTERS and MISSING_LABEL_IDS, return a JSON array assigning every missing labelId to exactly one existing chapter based on semantic fit with that chapter's title and description.

Hard constraints:
- Every labelId in MISSING_LABEL_IDS must be assigned to exactly one chapter.
- Do not assign a labelId to more than one chapter.
- Do not invent labelIds or chapters.
- Do not modify, rename, redescribe, reorder, split, or merge any chapter; only decide where each missing labelId goes.
- Each output object must have this exact shape:
{
"index": number,
"labelIds": [ number, ... ]
}
- "index" must be one of the chapter indices provided in CHAPTERS.
- Do not include an output object for a chapter that received no missing labelIds.
- No keys other than "index" and "labelIds" are allowed.
- Output ONLY valid JSON.
- Output no prose, no markdown, no code fences.

How to assign:
- For each labelId in MISSING_LABEL_IDS, compare it against every chapter's title and description and pick the single best semantic match.
- If a label could plausibly fit multiple chapters, prefer the more specific chapter over a more general one.
- You are not responsible for chapter size limits, chapter order, or creating new chapters; the existing chapters and their order are already correct.

Required internal procedure:
1. Read CHAPTERS and MISSING_LABEL_IDS.
2. For each missing labelId, evaluate semantic fit against every chapter's title and description.
3. Assign each labelId to its best-fitting chapter.
4. Group the assignments by chapter index.
5. Before answering, verify all of the following privately:
- every labelId in MISSING_LABEL_IDS appears exactly once across the output
- no labelId outside MISSING_LABEL_IDS appears
- no labelId is duplicated
- every index used matches a chapter provided in CHAPTERS
- no extra keys are present
6. If any check fails, revise internally until all checks pass.
7. Return only the final JSON array.

Important:
Do not describe your reasoning.
Do not output the checklist.
Do not output intermediate work.
Return only the final JSON array.

CHAPTERS:
<chapters>

MISSING_LABEL_IDS:
<missing_label_ids>
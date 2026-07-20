Your previous output was missing some labelIds. Fix it by inserting the missing labelIds into the most semantically related existing chapter, without altering anything else that was already correct.

Task:
Given PREVIOUS_OUTPUT and MISSING_LABEL_IDS, return exactly one corrected JSON array of chapter objects that uses every labelId from both exactly once.

Hard constraints:
- Every labelId already present in PREVIOUS_OUTPUT must remain in the output exactly once.
- Every labelId in MISSING_LABEL_IDS must be added to the output exactly once.
- Do not remove, duplicate, or invent any labelId.
- Do not change chapter titles, descriptions, or groupings that are unaffected by the missing labelIds.
- Only create a new chapter if no existing chapter is a reasonable semantic fit for one or more missing labelIds.
- Every chapter must contain at least 3 labelIds; never produce a chapter with fewer than 3.
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

How to fix:
- For each labelId in MISSING_LABEL_IDS, find the existing chapter whose theme is most semantically related to that label's text and add it there.
- Only create a new chapter if truly no existing chapter fits; if so, group unfit missing labels together, respecting the 3-label minimum, and place it where it best preserves the learning progression.
- Leave the order and content of unaffected chapters untouched.

Required internal procedure:
1. Read PREVIOUS_OUTPUT and MISSING_LABEL_IDS.
2. Build a private coverage set containing every labelId from both.
3. Assign each missing labelId to its most semantically related existing chapter, or a new chapter if none fits.
4. Merge any resulting chapter with fewer than 3 labelIds into its most semantically related neighboring chapter.
5. Reassign sequential index values only if chapters were added.
6. Before answering, verify all of the following privately:
- every labelId from PREVIOUS_OUTPUT and MISSING_LABEL_IDS appears exactly once
- no labelId appears more than once
- no labelId is missing
- no labelId outside PREVIOUS_OUTPUT and MISSING_LABEL_IDS appears
- every chapter has at least 3 labelIds
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

PREVIOUS_OUTPUT:
<previous_output>

MISSING_LABEL_IDS:
<missing_label_ids>
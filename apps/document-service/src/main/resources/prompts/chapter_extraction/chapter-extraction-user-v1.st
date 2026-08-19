You detect a single chapter boundary inside an ordered window of topics.

Task:
Given INPUT_TOPICS, return exactly one JSON object with a chapter title, description, and inclusive cutting point.

Input format:
Each entry is an ordered topic in the current window with:
- "position": 1-based index within this window
- "labels": space-separated short labels for that topic

Primary evidence:
1. Thematic continuity across consecutive topics
2. Shared terminology in the label strings
3. A clear shift to a new theme at the cutting point

Goal:
Choose the latest coherent prefix of the window that forms one learning chapter.
Topics from position 1 through cuttingPoint inclusive belong to this chapter.
Topics after cuttingPoint remain for later processing.

Hard constraints:
- Return exactly one JSON object with this shape:
{
"title": string,
"description": string,
"cuttingPoint": number
}
- "title" must be 2 to 3 words.
- "description" must be 6 to 7 words and summarize the chapter's focus.
- "cuttingPoint" must be an integer between 1 and the window size inclusive.
- Prefer chapters of at least 10 to 15 topics when the window is large enough.
- Do not cut earlier than position 10 unless the window itself has fewer than 10 topics.
- If the window has fewer than 10 topics, set cuttingPoint to the window size.
- If no earlier meaningful thematic shift exists, set cuttingPoint to the window size.
- No keys other than "title", "description", and "cuttingPoint" are allowed.
- Output ONLY valid JSON.
- Output no prose, no markdown, no code fences.

Required internal procedure:
1. Read all INPUT_TOPICS in order.
2. Locate the first meaningful thematic shift after a coherent chapter has formed.
3. Prefer a cutting point around positions 10 to 15 or later when the window allows it.
4. Assign title and description for the selected prefix.
5. Before answering, verify privately:
- cuttingPoint is between 1 and the window size inclusive
- title is 2 to 3 words
- description is 6 to 7 words
- no extra keys are present
6. If any check fails, revise internally until all checks pass.
7. Return only the final JSON object.

Important:
Do not describe your reasoning.
Do not output the checklist.
Do not output intermediate work.
Return only the final JSON object.
When writing string values, never use double quotes (") inside the text.
Use single quotes or rewrite the sentence to avoid quoting words entirely.

INPUT_TOPICS:
<topics>

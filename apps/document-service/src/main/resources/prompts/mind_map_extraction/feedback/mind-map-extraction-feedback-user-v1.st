Some pageIds were missing from the original mind map construction. You are given the existing tree nodes, without their pageIds, and the list of missing pageIds. Decide which existing parent node each missing pageId belongs under.

Task:
Given NODES, INPUT_PAGES, and MISSING_PAGE_IDS, return a JSON array assigning every missing pageId to exactly one existing parent node based on semantic fit.

Hard constraints:
- Every pageId in MISSING_PAGE_IDS must be assigned to exactly one parent node.
- Do not assign a pageId to more than one parent node.
- Do not invent pageIds or nodes.
- Do not modify, reorder, split, or merge any existing node; only decide where each missing pageId goes as a new child.
- Each output object must have this exact shape:
{
"parentNodeIndex": number,
"pageIds": [ number, ... ]
}
- "parentNodeIndex" must be one of the nodeIndex values provided in NODES.
- Do not include an output object for a parent node that received no missing pageIds.
- No keys other than "parentNodeIndex" and "pageIds" are allowed.
- Output ONLY valid JSON.
- Output no prose, no markdown, no code fences.

How to assign:
- Look up each missing pageId's title and summary in INPUT_PAGES.
- Compare it against every node's pageTitle and pick the single best semantic parent.
- If a page could plausibly fit multiple parents, prefer the more specific parent over a more general one.
- You are not responsible for tree balance or creating new nodes; the existing tree is already correct.

Required internal procedure:
1. Read NODES, INPUT_PAGES, and MISSING_PAGE_IDS.
2. For each missing pageId, evaluate semantic fit against every node in NODES.
3. Assign each pageId to its best-fitting parentNodeIndex.
4. Group the assignments by parentNodeIndex.
5. Before answering, verify all of the following privately:
- every pageId in MISSING_PAGE_IDS appears exactly once across the output
- no pageId outside MISSING_PAGE_IDS appears
- no pageId is duplicated
- every parentNodeIndex used matches a node provided in NODES
- no extra keys are present
6. If any check fails, revise internally until all checks pass.
7. Return only the final JSON array.

Important:
Do not describe your reasoning.
Do not output the checklist.
Do not output intermediate work.
Return only the final JSON array.

NODES:
<nodes>

INPUT_PAGES:
<pages>

MISSING_PAGE_IDS:
<missing_page_ids>

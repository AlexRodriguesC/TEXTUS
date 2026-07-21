Some labelIds were missing from the original mind map construction. You are given the existing tree nodes, without their labelIds, and the list of missing labelIds. Decide which existing parent node each missing labelId belongs under.

Task:
Given NODES, INPUT_LABELS, and MISSING_LABEL_IDS, return a JSON array assigning every missing labelId to exactly one existing parent node based on semantic fit.

Hard constraints:
- Every labelId in MISSING_LABEL_IDS must be assigned to exactly one parent node.
- Do not assign a labelId to more than one parent node.
- Do not invent labelIds or nodes.
- Do not modify, reorder, split, or merge any existing node; only decide where each missing labelId goes as a new child.
- Each output object must have this exact shape:
{
"parentNodeIndex": number,
"labelIds": [ number, ... ]
}
- "parentNodeIndex" must be one of the nodeIndex values provided in NODES.
- Do not include an output object for a parent node that received no missing labelIds.
- No keys other than "parentNodeIndex" and "labelIds" are allowed.
- Output ONLY valid JSON.
- Output no prose, no markdown, no code fences.

How to assign:
- Look up each missing labelId's name in INPUT_LABELS.
- Compare it against every node's labelName and pick the single best semantic parent.
- If a label could plausibly fit multiple parents, prefer the more specific parent over a more general one.
- You are not responsible for tree balance or creating new nodes; the existing tree is already correct.

Required internal procedure:
1. Read NODES, INPUT_LABELS, and MISSING_LABEL_IDS.
2. For each missing labelId, evaluate semantic fit against every node in NODES.
3. Assign each labelId to its best-fitting parentNodeIndex.
4. Group the assignments by parentNodeIndex.
5. Before answering, verify all of the following privately:
- every labelId in MISSING_LABEL_IDS appears exactly once across the output
- no labelId outside MISSING_LABEL_IDS appears
- no labelId is duplicated
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

INPUT_LABELS:
<labels>

MISSING_LABEL_IDS:
<missing_label_ids>

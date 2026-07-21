You transform a list of labels and their associated topics into exactly one rooted mind map tree.

Task:
Given INPUT_LABELS and LABEL_TOPIC_GROUPS, return exactly one JSON tree that uses every input labelId exactly once.

Primary evidence for hierarchy:
1. Label text (semantic similarity and conceptual containment)

Secondary evidence:
2. Topical overlap in LABEL_TOPIC_GROUPS (topics sharing labels indicate related scope)

Goal:
Build a pedagogically coherent broad-to-specific hierarchy.
Prefer conceptual prerequisite/containment structure over document order.
Return a tree, not a graph.

Hard constraints:
- Use every input labelId exactly once.
- Do not omit any labelId.
- Do not repeat any labelId.
- Do not invent any labelId.
- Do not invent any extra node.
- There must be exactly one root node.
- "children" must always be present on every node, even if empty.
- Output ONLY valid JSON, matching exactly the schema below.
- Output no prose, no markdown, no code fences.

How to choose structure:
- Treat label names as the main signal; shared or overlapping terminology indicates close conceptual relation.
- Use LABEL_TOPIC_GROUPS to estimate topical scope and specificity, not to determine sibling order.
- Prefer broader labels as parents.
- Put narrower, dependent, illustrative, or applied labels under broader parent labels.
- If two labels are siblings, they should be as conceptually parallel as possible.
- If two labels are related but neither contains the other, place both under the nearest broader label.
- The root must be the single label that best represents the broadest subject among all provided labels.

Required internal procedure:
1. Read INPUT_LABELS and LABEL_TOPIC_GROUPS.
2. Build a private coverage set containing every input labelId.
3. Select exactly one root labelId from that set.
4. Attach every remaining labelId exactly once somewhere under the root.
5. Before answering, verify all of the following privately:
- every input labelId appears exactly once
- no labelId appears more than once
- no labelId is missing
- no labelId outside INPUT_LABELS appears
- the result is a single connected tree
- there is exactly one root
- no node appears as a descendant of itself
- every node has a children array
- the output conforms exactly to the provided schema
6. If any check fails, revise internally until all checks pass.
7. Return only the final JSON tree.

Important:
Do not describe your reasoning.
Do not output the checklist.
Do not output intermediate work.
Return only the final JSON object.
When writing string values, never use double quotes (") inside the text.
Use single quotes or rewrite the sentence to avoid quoting words entirely.

<format>

INPUT_LABELS:
<labels>

LABEL_TOPIC_GROUPS:
<label_topic_groups>

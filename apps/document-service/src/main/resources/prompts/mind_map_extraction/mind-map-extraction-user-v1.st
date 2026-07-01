You transform a list of textbook chunks into exactly one rooted mind map tree.

Task:
Given INPUT_CHUNKS, return exactly one JSON tree that uses every input chunkId exactly once.

Primary evidence for hierarchy:
1. key
2. related_keys
3. keywords

Secondary evidence:
4. title
5. summary

Goal:
Build a pedagogically coherent broad-to-specific hierarchy.
Prefer conceptual prerequisite/containment structure over document order.
Return a tree, not a graph.

Hard constraints:
- Use every input chunkId exactly once.
- Do not omit any chunkId.
- Do not repeat any chunkId.
- Do not invent any chunkId.
- Do not invent any extra node.
- There must be exactly one root node.
- Every node must have this exact shape:
  {
    "chunkId": number,
    "children": [ ... ]
  }
- "children" must always be present, even if empty.
- No keys other than "chunkId" and "children" are allowed.
- Output ONLY valid JSON.
- Output no prose, no markdown, no code fences.

How to choose structure:
- Treat "key" as the main topic identity.
- Use "related_keys" as evidence of semantic linkage such as prerequisite, dependency, application, contrast, extension, or example.
- Use "keywords" to estimate topical overlap, scope, and specificity.
- Use "title" and "summary" only to break ties or disambiguate vague keys.
- Prefer broader topics as parents.
- Put narrower, dependent, illustrative, or applied topics under broader parent topics.
- If two chunks are siblings, they should be as conceptually parallel as possible.
- If two chunks are related but neither contains the other, place both under the nearest broader topic.
- The root must be the single chunk that best represents the broadest topic among all provided chunks.

Required internal procedure:
1. Read all chunks.
2. Build a private coverage set containing every input chunkId.
3. Select exactly one root chunkId from that set.
4. Attach every remaining chunkId exactly once somewhere under the root.
5. Before answering, verify all of the following privately:
   - every input chunkId appears exactly once
   - no chunkId appears more than once
   - no chunkId is missing
   - no chunkId outside INPUT_CHUNKS appears
   - the result is a single connected tree
   - there is exactly one root
   - no node appears as a descendant of itself
   - every node has a children array
   - no extra keys are present
6. If any check fails, revise internally until all checks pass.
7. Return only the final JSON tree.

Important:
Do not describe your reasoning.
Do not output the checklist.
Do not output intermediate work.
Return only the final JSON object.

INPUT_CHUNKS:
<chunks>
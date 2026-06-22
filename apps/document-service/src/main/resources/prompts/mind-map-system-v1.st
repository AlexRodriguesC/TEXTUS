Your task is to transform a list of textbook chunks into a single rooted mind map tree.

You must infer parent -> children relationships primarily from:
1. key
2. related_keys
3. keywords
and secondarily from:
4. title
5. summary

Objective:
- Build a pedagogically coherent hierarchy of topics.
- Group more specific topics under broader prerequisite or containing topics.
- Prefer conceptual hierarchy over document order.
- Return a tree, not a graph.
- Every chunk must appear exactly once as exactly one node in the tree.
- There must be exactly one root node.
- The root should be the chunk that best represents the broadest topic among the provided chunks.
- Children must be semantically narrower, more specific, dependent on, or illustrative of their parent.
- Sibling nodes should be conceptually parallel when possible.

Interpretation rules:
- Use "key" as the primary identity of the chunk’s topic.
- Use "related_keys" as evidence of semantic linkage, such as prerequisite, dependency, application, contrast, extension, or example.
- Use "keywords" to estimate topical overlap and specificity.
- Use "title" and "summary" only to disambiguate hierarchy when keys are too similar or too vague.
- Prefer broad-to-specific organization.
- If two chunks describe the same topic at different granularity, place the more specific one under the more general one.
- If two chunks are closely related but neither clearly contains the other, attach both under the nearest broader topic.
- Do not invent extra nodes.
- Do not omit any chunk.
- Do not duplicate any chunk.
- Do not return explanations.

Output requirements:
- Return ONLY valid JSON.
- The JSON must match this shape exactly:
{
  "chunkId": <number>,
  "children": [ ... same object shape ... ]
}

Validation rules before answering:
- Every input chunkId appears exactly once.
- No chunkId appears more than once.
- No child references its ancestor.
- The output is a single connected tree.
- All children arrays must be present, even if empty.
- No extra keys are allowed.
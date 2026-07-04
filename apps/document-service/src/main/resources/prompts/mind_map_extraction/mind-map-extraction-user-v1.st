You transform a list of textbook topics into exactly one rooted mind map tree.

Task:
Given INPUT_TOPICS, return exactly one JSON tree that uses every input topicId exactly once.

Primary evidence for hierarchy:
1. labels

Secondary evidence:
2. title
3. summary

Goal:
Build a pedagogically coherent broad-to-specific hierarchy.
Prefer conceptual prerequisite/containment structure over document order.
Return a tree, not a graph.

Hard constraints:
- Use every input topicId exactly once.
- Do not omit any topicId.
- Do not repeat any topicId.
- Do not invent any topicId.
- Do not invent any extra node.
- There must be exactly one root node.
- Every node must have this exact shape:
  {
    "topicId": number,
    "children": [ ... ]
  }
- "children" must always be present, even if empty.
- No keys other than "topicId" and "children" are allowed.
- Output ONLY valid JSON.
- Output no prose, no markdown, no code fences.

How to choose structure:
- Treat "labels" as the main topic identity; shared or overlapping labels between topics indicate close conceptual relation.
- Use overlap in "labels" across topics as evidence of semantic linkage such as prerequisite, dependency, application, contrast, extension, or example.
- Use the number and specificity of shared labels to estimate topical overlap, scope, and specificity.
- Use "title" and "summary" only to break ties or disambiguate when labels are insufficient.
- Prefer broader topics as parents.
- Put narrower, dependent, illustrative, or applied topics under broader parent topics.
- If two topics are siblings, they should be as conceptually parallel as possible.
- If two topics are related but neither contains the other, place both under the nearest broader topic.
- The root must be the single topic that best represents the broadest topic among all provided topics.

Required internal procedure:
1. Read all topics.
2. Build a private coverage set containing every input topicId.
3. Select exactly one root topicId from that set.
4. Attach every remaining topicId exactly once somewhere under the root.
5. Before answering, verify all of the following privately:
   - every input topicId appears exactly once
   - no topicId appears more than once
   - no topicId is missing
   - no topicId outside INPUT_TOPICS appears
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
When writing string values, never use double quotes (") inside the text.
Use single quotes or rewrite the sentence to avoid quoting words entirely.
Example: instead of `the vowel "a" is pronounced...`, write `the vowel a is pronounced...`

INPUT_TOPICS:
<topics>
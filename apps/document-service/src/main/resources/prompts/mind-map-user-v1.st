Build a single rooted mind map from the following chunks.

Each chunk has this meaning:
- title: concise topic title
- summary: semantic synopsis of what is taught
- keywords: important domain terms
- key: primary subtopic label
- related_keys: semantically related subtopics

Use key, related_keys, and keywords as the main evidence for hierarchy.
Use title and summary only as supporting evidence.

Return only valid JSON in this format:
{
  "chunkId": number,
  "children": [
    {
      "chunkId": number,
      "children": []
    }
  ]
}

Rules:
- Use every chunk exactly once.
- Produce exactly one root node.
- Do not invent nodes.
- Do not output markdown.
- Do not output commentary.

Chunks:
[
  {
    "chunkId": 1,
    "title": "...",
    "summary": "...",
    "keywords": ["..."],
    "key": "...",
    "related_keys": ["..."]
  }
]
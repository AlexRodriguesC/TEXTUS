Analyze this text from a document and extract structured metadata for a RAG system.

Previous Page Summary: <prev_summary>

Page Text:
<page_text>

Rolling Keys (specific subtopics seen in previous chunks):
<rolling_keys>

Extract the following in a single JSON response:
{
  "title": "Concise, information-dense title (3-8 words). Preserve the primary concept, method, phenomenon, process, or topic discussed. Avoid generic wording and prefer terminology that would help a retrieval system identify the chunk's subject without additional context.",

  "summary": "Self-contained semantic synopsis (40-100 words). Capture the main claims, concepts, mechanisms, relationships, reasoning, conclusions, definitions, procedures, examples, constraints, assumptions, and outcomes discussed in the chunk. Preserve information needed to understand the content without reading surrounding chunks. Prioritize educational value and retrieval relevance over brevity.",

  "keywords": [
    "5-12 high-signal terms, phrases, jargon, concepts, methods, entities, technologies, frameworks, metrics, processes, or domain vocabulary explicitly present or strongly implied in the chunk. Include both broad and specific terms when useful for retrieval."
  ],

  "entities": [
    {
      "name": "Canonical entity name as stated or unambiguously implied in the text. Use the most specific identifiable form.",
      "type": "PERSON | ORG | LOC | TECH | CONCEPT | EVENT | METRIC"
    }
  ],

  "questions": [
    "3-6 concrete questions that can be answered primarily from this chunk. Cover definitions, mechanisms, reasoning, procedures, comparisons, implications, and practical applications where relevant. Phrase questions as realistic information-seeking queries likely to be asked by users."
  ],

  "key": "Specific subtopic label (2-5 words, lowercase). Represent the primary knowledge unit taught by the chunk. Prefer stable conceptual names over document-specific wording.",

  "related_keys": [
    "0-5 semantically related keys that are prerequisites, dependencies, extensions, contrasts, alternatives, examples, or applications of the current key. Include relationships that would help graph-based retrieval and educational navigation."
  ]
}

Rules:
- "key" must distinguish this chunk; never use a single word or a broad document topic.
- "related_keys" must be a subset of the provided rolling keys.
- Return only valid JSON, with no extra text.
- All text must be in English.
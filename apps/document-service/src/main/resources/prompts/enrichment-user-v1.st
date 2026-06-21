Analyze this text from a document and extract structured metadata for a RAG system.

Previous Page Summary: <prev_summary>

Page Text:
<page_text>

Rolling Keys (specific subtopics seen in previous chunks):
<rolling_keys>

Extract the following in a single JSON response:
{
  "title": "Concise, information-dense title (3-8 words). Name the main concept, rule, topic, phenomenon, process, or knowledge unit being taught. Do not describe the page format, exercise type, worksheet style, or classroom activity.",

  "summary": "Self-contained semantic synopsis (40-100 words). Summarize what the learner is meant to understand, know, explain, recognize, or apply from this chunk. Focus on the concepts taught, definitions introduced, relationships explained, procedures demonstrated, examples used to clarify meaning, and conclusions or rules conveyed. Do not describe the teaching format, page instructions, exercise mechanics, or layout unless they are necessary to understand the underlying concept.",

  "keywords": [
    "5-12 high-signal terms, phrases, concepts, rules, methods, entities, processes, or domain vocabulary that represent what is being taught. Prefer content terms over pedagogical terms. Avoid labels about exercise type, worksheet structure, or classroom activity unless they are themselves the subject being taught."
  ],

  "entities": [
    {
      "name": "Canonical entity name as stated or unambiguously implied in the text. Use the most specific identifiable form.",
      "type": "PERSON | ORG | LOC | TECH | CONCEPT | EVENT | METRIC"
    }
  ],

  "questions": [
    "3-6 concrete questions that can be answered primarily from this chunk about the knowledge being taught. Focus on meaning, definitions, mechanisms, reasoning, distinctions, implications, and applications. Do not generate questions about what the student is asked to do on the page unless that action is itself the topic being taught."
  ],

  "key": "Specific subtopic label (2-5 words, lowercase). Represent the primary concept or skill being taught, not the exercise format or page task. Prefer stable conceptual names over document-specific wording.",

  "related_keys": [
    "0-5 semantically related keys that represent prerequisites, dependencies, extensions, contrasts, examples, or applications of the current key in terms of subject matter being taught. Do not include relationships based only on page structure or activity format."
  ]
}

Rules:
- Extract the underlying knowledge, not the pedagogical wrapper.
- Ignore whether the page teaches through a writing exercise, spelling task, quiz, prompt, worksheet, dialogue, or similar format unless that format is itself the topic.
- Do not summarize the page as a page; summarize the concepts being taught.
- Prefer subject-matter descriptions over instructional descriptions.
- "key" must distinguish this chunk; never use a single word or a broad document topic.
- "related_keys" must be a subset of the provided rolling keys.
- Return only valid JSON, with no extra text.
- All text must be in English.
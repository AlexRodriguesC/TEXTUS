Analyze the following document and extract distinct topics using a question-answer-summary approach.

Existing labels:
<existing_labels>

Steps:
1. Identify distinct topics covered in the document.
2. For each topic, generate 2-3 questions capturing its most critical factual information.
3. Answer each question using ONLY information present in the document. Be specific and precise.
4. Assign labels: labels must be maximally descriptive and information-dense, so that from reading only the label one can infer as much as possible about the topic's specific subject (e.g. named entities, exact concept, specific process, or precise scope) rather than a generic category. Labels must also be extremely granular and specific to the exact subject matter—two topics should share a label ONLY if they cover the exact same narrow subject, not merely a related or broader theme. Reuse a label from the existing labels list only if it matches this precisely; otherwise create a new short, descriptive label specific to this topic.
5. Write a concise title for each topic.
6. Using the answers from step 3, write a factual summary for each topic as direct declarative statements only—no "this topic discusses" phrasing.
7. Classify each topic into one or more exerciseTypes (LIST, GROUP, PAIR, QUESTION) using the shape criteria from the system prompt. Include every valid shape. Reject LIST when order is arbitrary or presentational; reject PAIR when mappings are many-to-many or fuzzy; reject GROUP when categories overlap or items could belong to more than one bucket. If the topic is not structurally decomposable, use QUESTION rather than stretching it into list/pair/group.

Document:
<page_text>

<format>

When writing string values, never use double quotes (") inside the text.
Use single quotes or rewrite the sentence to avoid quoting words entirely.
Example: instead of `the vowel "a" is pronounced...`, write `the vowel a is pronounced...`

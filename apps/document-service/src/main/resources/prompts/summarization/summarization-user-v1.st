Analyze the following document page.

Step 1 – Classify Page: Decide whether the page contains substantive educational or informational content.
Set contentPage to false for covers, title pages, copyright or imprint pages, tables of contents, indexes, acknowledgments, dedications, bibliographies or reference lists, glossaries, appendices, and blank or boilerplate-only pages.
If contentPage is false, return empty strings for title and summary and stop.

Step 2 – Generate Questions: Identify 3–5 questions that capture the most critical factual information in the document.

Step 3 – Answer Questions: Answer each question using ONLY information present in the document. Be specific and precise.

Step 4 – Generate Title: Create a concise title (5–15 words) that accurately reflects the document's main subject.

Step 5 – Generate Summary: Write a factual summary that presents the key information directly. Do NOT use introductory phrases like "the document covers," "this article discusses," or "the text describes." Instead, state the facts directly as declarative statements. For example, write "Water has a planar geometry with two bonding and two lone pairs" rather than "The document discusses water's geometry."

<format>

Document: <document_text>

Output formatting rules (strict):
- Every string value in the output MUST be wrapped in double quotes (").
- Never leave a string value unquoted, even if it contains only a single word or number-like text (e.g. write "42" not 42, write "+3" not +3).
- When writing the content of a string value, never use double quotes (") inside the text itself.
- Use single quotes or rewrite the sentence to avoid quoting words entirely.
  Example: instead of `the vowel "a" is pronounced...`, write `the vowel a is pronounced...`
- Any mathematical expression, symbol, or number with a sign (e.g. +5, -3, x^2) that appears as part of a string value must remain inside the surrounding double quotes as plain text, never as a bare token outside quotes.
- When contentPage is false, title and summary MUST be empty strings ("").

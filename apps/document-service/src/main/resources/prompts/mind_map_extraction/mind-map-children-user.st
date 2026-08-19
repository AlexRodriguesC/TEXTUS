Select the immediate direct child pageIds for PARENT_PAGE from REMAINING_PAGES.

Task:
Given PARENT_PAGE, REMAINING_PAGES, and PAGE_TOPIC_GROUPS, return the pageIds that should be attached as direct children of PARENT_PAGE.

Hard constraints:
- Every returned pageId must come from REMAINING_PAGES.
- Return only immediate direct children of PARENT_PAGE.
- Do not return pages that belong under a different branch.
- Do not return pages that should be deeper descendants of a child of PARENT_PAGE.
- Do not return the parent pageId.
- "pageIds" may be empty if no remaining page is a direct child of PARENT_PAGE.
- Output ONLY valid JSON, matching exactly the schema below.
- Output no prose, no markdown, no code fences.

How to choose:
- Compare each remaining page title and summary against PARENT_PAGE.
- Prefer narrower, dependent, illustrative, or applied pages as direct children when they are conceptually contained by PARENT_PAGE.
- If a remaining page belongs under another branch, omit it.
- If a remaining page belongs under a child of PARENT_PAGE rather than directly under PARENT_PAGE, omit it.

Return only the final JSON object.
When writing string values, never use double quotes (") inside the text.
Use single quotes or rewrite the sentence to avoid quoting words entirely.

<format>

PARENT_PAGE:
<parent_page>

REMAINING_PAGES:
<remaining_pages>

PAGE_TOPIC_GROUPS:
<page_topic_groups>

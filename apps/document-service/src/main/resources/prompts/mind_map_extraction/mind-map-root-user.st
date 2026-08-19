Select exactly one root pageId from INPUT_PAGES.

Task:
Given INPUT_PAGES and PAGE_TOPIC_GROUPS, return the single pageId that best represents the broadest subject.

Hard constraints:
- "pageId" must come from INPUT_PAGES.
- Do not invent any pageId.
- Output ONLY valid JSON, matching exactly the schema below.
- Output no prose, no markdown, no code fences.

How to choose:
- Prefer the broadest, most umbrella-like page based on its title and summary.
- Use PAGE_TOPIC_GROUPS as secondary evidence of topical scope.
- The root should be the page under which the other pages can plausibly be organized.

Return only the final JSON object.
When writing string values, never use double quotes (") inside the text.
Use single quotes or rewrite the sentence to avoid quoting words entirely.

<format>

INPUT_PAGES:
<pages>

PAGE_TOPIC_GROUPS:
<page_topic_groups>

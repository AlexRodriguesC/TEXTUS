"""TEXTUS-specific GEval metric presets for document pipeline evaluation."""

from dataclasses import dataclass

from deepeval.metrics.g_eval import Rubric
from deepeval.test_case import SingleTurnParams


@dataclass(frozen=True)
class TextusGEvalPreset:
  """Fixed criteria and evaluation params for a named TEXTUS GEval metric."""

  criteria: str
  evaluation_params: list[SingleTurnParams]
  evaluation_steps: list[str] | None = None
  rubric: list[Rubric] | None = None
  default_threshold: float = 0.7


TEXTUS_GEVAL_PRESETS: dict[str, TextusGEvalPreset] = {
  "consistency": TextusGEvalPreset(
    criteria=(
      "Consistency (1-5) - the factual alignment between Actual Output and Input. "
      "A factually consistent Actual Output contains only statements that are "
      "entailed by Input. Penalize Actual Output that contains hallucinated, "
      "unsupported, or contradicted facts. Do not penalize Actual Output for "
      "omitting information present in Input; omissions affect relevance, not "
      "consistency."
    ),
    evaluation_params=[SingleTurnParams.INPUT, SingleTurnParams.ACTUAL_OUTPUT],
    evaluation_steps=[
      "Read Input carefully and identify the main facts and details it presents.",
      (
        "Read Actual Output and check each statement it makes against Input. "
        "Flag any statement that is not entailed by Input, i.e. that is "
        "contradicted by Input or has no support in it."
      ),
      (
        "Ignore any facts or details present in Input but not mentioned in "
        "Actual Output; missing information should not lower the score, since "
        "consistency only concerns whether what is stated is accurate, not "
        "whether everything has been covered."
      ),
      (
        "Assign a consistency score from 1 to 5 using the evaluation criteria "
        "and rubric, based solely on the unsupported or hallucinated statements "
        "identified in the previous step."
      ),
    ],
    # Score-band wording is a TEXTUS adaptation; official SummEval prompts
    # define the 1-5 scale without per-score descriptors.
    rubric=[
      Rubric(
        score_range=(1, 1),
        expected_outcome=(
          "largely unsupported; most statements are hallucinated or contradict Input."
        ),
      ),
      Rubric(
        score_range=(2, 2),
        expected_outcome=(
          "weak alignment; several statements are unsupported or contradicted by Input."
        ),
      ),
      Rubric(
        score_range=(3, 3),
        expected_outcome=(
          "mostly accurate, but at least one statement is unsupported or contradicted by Input."
        ),
      ),
      Rubric(
        score_range=(4, 4),
        expected_outcome=(
          "accurate overall, with only minor imprecision and no unsupported claim or hallucination."
        ),
      ),
      Rubric(
        score_range=(5, 5),
        expected_outcome=(
          "every statement is entailed by Input; no unsupported, contradicted, or hallucinated facts."
        ),
      ),
    ],
  ),
  "relevance": TextusGEvalPreset(
    criteria=(
      "Relevance (1-5) - the selection of important content from Input. Actual "
      "Output should include only important information from Input. Penalize "
      "missing major topics, irrelevant or excess information, and redundancies."
    ),
    evaluation_params=[SingleTurnParams.INPUT, SingleTurnParams.ACTUAL_OUTPUT],
    evaluation_steps=[
      "Read Actual Output and Input carefully.",
      "Compare Actual Output with Input and identify the main points of Input.",
      (
        "Assess how well Actual Output covers those main points, including any "
        "major omissions."
      ),
      (
        "Assess how much irrelevant, excessive, or redundant information Actual "
        "Output contains."
      ),
      (
        "Assign a relevance score from 1 to 5 using the evaluation criteria and "
        "rubric."
      ),
    ],
    # Score-band wording is a TEXTUS adaptation; official SummEval prompts
    # define the 1-5 scale without per-score descriptors.
    rubric=[
      Rubric(
        score_range=(1, 1),
        expected_outcome=(
          "misses most main points and contains substantial irrelevant, excessive, "
          "or redundant content."
        ),
      ),
      Rubric(
        score_range=(2, 2),
        expected_outcome=(
          "misses several important points and/or contains notable irrelevant or "
          "redundant content."
        ),
      ),
      Rubric(
        score_range=(3, 3),
        expected_outcome=(
          "covers important content partially, with a material omission or addition."
        ),
      ),
      Rubric(
        score_range=(4, 4),
        expected_outcome=(
          "covers nearly all important content with only a minor omission, excess, "
          "or redundancy."
        ),
      ),
      Rubric(
        score_range=(5, 5),
        expected_outcome=(
          "covers all main important content and contains no irrelevant, excessive, "
          "or redundant information."
        ),
      ),
    ],
  ),
  # custom TEXTUS-specific metric
  "topic_granularity": TextusGEvalPreset(
    criteria=(
      "Assess whether each topic in the list is distinct, non-redundant, "
      "and specific, rather than vague, overly broad, or overlapping in "
      "scope with other topics in the list."
    ),
    evaluation_params=[SingleTurnParams.ACTUAL_OUTPUT],
    rubric=[
      Rubric(
        score_range=(0, 3),
        expected_outcome=(
          "Multiple topics are vague, overly broad, or overlap "
          "significantly in scope with other topics in the list."
        ),
      ),
      Rubric(
        score_range=(4, 7),
        expected_outcome=(
          "Most topics are distinct and specific, but at least one "
          "topic is too vague, too broad, or overlaps in scope with "
          "another topic in the list."
        ),
      ),
      Rubric(
        score_range=(8, 10),
        expected_outcome=(
          "Every topic is distinct, non-redundant, and specific, "
          "with no meaningful overlap in scope between any two "
          "topics in the list."
        ),
      ),
    ],
  ),
}

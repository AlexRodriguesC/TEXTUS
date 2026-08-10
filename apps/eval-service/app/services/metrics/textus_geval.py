"""TEXTUS-specific GEval metric presets for document pipeline evaluation."""

from dataclasses import dataclass

from deepeval.metrics.g_eval import Rubric
from deepeval.test_case import SingleTurnParams


@dataclass(frozen=True)
class TextusGEvalPreset:
  """Fixed criteria and evaluation params for a named TEXTUS GEval metric."""

  criteria: str
  evaluation_params: list[SingleTurnParams]
  rubric: list[Rubric] | None = None
  default_threshold: float = 0.7


TEXTUS_GEVAL_PRESETS: dict[str, TextusGEvalPreset] = {
  "topic_coverage": TextusGEvalPreset(
    criteria=(
      "Assess whether the topic list covers all distinct subjects/themes "
      "mentioned in the summary, with no major topic omitted."
    ),
    evaluation_params=[SingleTurnParams.INPUT, SingleTurnParams.ACTUAL_OUTPUT],
    rubric=[
      Rubric(
        score_range=(0, 0),
        expected_outcome=(
          "The topic list omits at least one major subject or theme present "
          "in the summary."
        ),
      ),
      Rubric(
        score_range=(10, 10),
        expected_outcome=(
          "The topic list covers all distinct subjects/themes present in the "
          "summary, with no major topic omitted."
        ),
      ),
    ],
  ),
  "topic_granularity": TextusGEvalPreset(
    criteria=(
      "Assess whether each topic in the list is distinct, non-redundant, and "
      "specific enough to generate a meaningful question, rather than vague or "
      "overlapping with other topics."
    ),
    evaluation_params=[SingleTurnParams.ACTUAL_OUTPUT],
    rubric=[
      Rubric(
        score_range=(0, 3),
        expected_outcome=(
          "Multiple topics are vague, redundant, or overlap significantly "
          "with other topics, making them unsuitable for generating a "
          "distinct question each."
        ),
      ),
      Rubric(
        score_range=(4, 7),
        expected_outcome=(
          "Most topics are distinct and specific, but at least one topic is "
          "too vague, overlaps with another, or is not specific enough to "
          "generate a clear, unique question."
        ),
      ),
      Rubric(
        score_range=(8, 10),
        expected_outcome=(
          "Every topic is distinct, non-redundant, and specific enough to "
          "generate a clear, meaningful question with no overlap with other "
          "topics."
        ),
      ),
    ],
  ),
  "question_relevance": TextusGEvalPreset(
    criteria=(
      "Assess whether the generated question is directly answerable from and "
      "clearly relevant to the given topic, without introducing unrelated content."
    ),
    evaluation_params=[SingleTurnParams.INPUT, SingleTurnParams.ACTUAL_OUTPUT],
    rubric=[
      Rubric(
        score_range=(0, 3),
        expected_outcome=(
          "The question is not answerable from the given topic and/or "
          "introduces significant unrelated content."
        ),
      ),
      Rubric(
        score_range=(4, 7),
        expected_outcome=(
          "The question is mostly answerable from the topic but includes "
          "minor unrelated content or is only loosely relevant."
        ),
      ),
      Rubric(
        score_range=(8, 10),
        expected_outcome=(
          "The question is directly answerable from and clearly relevant to "
          "the given topic, with no unrelated content introduced."
        ),
      ),
    ],
  ),
}

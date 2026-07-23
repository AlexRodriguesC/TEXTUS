"""TEXTUS-specific GEval metric presets for document pipeline evaluation."""

from dataclasses import dataclass

from deepeval.test_case import SingleTurnParams


@dataclass(frozen=True)
class TextusGEvalPreset:
  """Fixed criteria and evaluation params for a named TEXTUS GEval metric."""

  criteria: str
  evaluation_params: list[SingleTurnParams]
  default_threshold: float = 0.7


TEXTUS_GEVAL_PRESETS: dict[str, TextusGEvalPreset] = {
  "topic_coverage": TextusGEvalPreset(
    criteria=(
      "Assess whether the topic list covers all distinct subjects/themes "
      "mentioned in the summary, with no major topic omitted."
    ),
    evaluation_params=[SingleTurnParams.INPUT, SingleTurnParams.ACTUAL_OUTPUT],
  ),
  "topic_granularity": TextusGEvalPreset(
    criteria=(
      "Assess whether each topic in the list is distinct, non-redundant, and "
      "specific enough to generate a meaningful question, rather than vague or "
      "overlapping with other topics."
    ),
    evaluation_params=[SingleTurnParams.ACTUAL_OUTPUT],
  ),
  "question_relevance": TextusGEvalPreset(
    criteria=(
      "Assess whether the generated question is directly answerable from and "
      "clearly relevant to the given topic, without introducing unrelated content."
    ),
    evaluation_params=[SingleTurnParams.INPUT, SingleTurnParams.ACTUAL_OUTPUT],
  ),
}

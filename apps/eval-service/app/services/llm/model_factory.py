"""Factory for building DeepEval LLM instances configured for LLMGateway."""

from functools import lru_cache

from deepeval.models import GPTModel
from deepeval.models.base_model import DeepEvalBaseLLM

from app.core.config import Settings


@lru_cache
def build_evaluation_llm(
  api_key: str | None,
  base_url: str | None,
  model: str,
) -> DeepEvalBaseLLM:
  return GPTModel(
    model=model,
    api_key=api_key,
    base_url=base_url,
    temperature=0,
    default_headers={"x-no-cache": "true"},
  )


def get_evaluation_llm(settings: Settings) -> DeepEvalBaseLLM:
  return build_evaluation_llm(
    settings.openai_api_key,
    settings.openai_base_url,
    settings.default_llm_model,
  )

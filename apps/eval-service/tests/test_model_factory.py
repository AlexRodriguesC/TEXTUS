"""Tests for LLM factory wiring."""

from deepeval.models import GPTModel

from app.core.config import Settings
from app.services.llm.model_factory import build_evaluation_llm, get_evaluation_llm


def test_build_evaluation_llm_returns_gpt_model_with_config() -> None:
  llm = build_evaluation_llm("test-key", "https://gateway/v1", "my-model")

  assert isinstance(llm, GPTModel)
  assert llm.get_model_name() == "my-model"
  assert llm.api_key is not None
  assert llm.api_key.get_secret_value() == "test-key"
  assert llm.base_url == "https://gateway/v1"


def test_build_evaluation_llm_is_cached() -> None:
  first = build_evaluation_llm("key", "https://gateway/v1", "model-a")
  second = build_evaluation_llm("key", "https://gateway/v1", "model-a")

  assert first is second


def test_get_evaluation_llm_uses_settings() -> None:
  settings = Settings(
    OPENAI_API_KEY="settings-key",
    OPENAI_BASE_URL="https://settings/v1",
    EVALUATION_LLM_MODEL="settings-model",
  )

  llm = get_evaluation_llm(settings)

  assert isinstance(llm, GPTModel)
  assert llm.get_model_name() == "settings-model"
  assert llm.api_key is not None
  assert llm.api_key.get_secret_value() == "settings-key"
  assert llm.base_url == "https://settings/v1"

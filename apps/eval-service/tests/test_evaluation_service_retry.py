"""Tests for evaluation service retry behavior."""

from __future__ import annotations

import logging
from unittest.mock import AsyncMock, MagicMock

import httpx
import pytest
from deepeval.test_case import LLMTestCase
from openai import APIStatusError, RateLimitError
from tenacity import Future, RetryError, wait_fixed

from app.services.evaluation_service import _run_single_metric
from app.services.llm import retry as retry_module
from app.services.llm.retry import MAX_ATTEMPTS, is_retryable_llm_error, measure_with_retry


def _rate_limit_error() -> RateLimitError:
  request = httpx.Request("POST", "https://api.example.com/v1/chat/completions")
  response = httpx.Response(429, request=request)
  return RateLimitError("rate limit exceeded", response=response, body=None)


def _api_status_error(status_code: int) -> APIStatusError:
  request = httpx.Request("POST", "https://api.example.com/v1/chat/completions")
  response = httpx.Response(status_code, request=request)
  return APIStatusError("provider unavailable", response=response, body=None)


def _retry_error(cause: BaseException) -> RetryError:
  future = Future(attempt_number=MAX_ATTEMPTS)
  future.set_exception(cause)
  return RetryError(future)


@pytest.fixture(autouse=True)
def no_retry_wait(monkeypatch: pytest.MonkeyPatch) -> None:
  monkeypatch.setattr(
    retry_module,
    "wait_exponential",
    lambda **_kwargs: wait_fixed(0),
  )


def test_is_retryable_llm_error_detects_rate_limit() -> None:
  assert is_retryable_llm_error(_rate_limit_error()) is True
  assert is_retryable_llm_error(_retry_error(_rate_limit_error())) is True
  assert is_retryable_llm_error(_api_status_error(503)) is True


def test_is_retryable_llm_error_rejects_validation_error() -> None:
  assert is_retryable_llm_error(ValueError("invalid metric config")) is False
  assert is_retryable_llm_error(_api_status_error(400)) is False


@pytest.mark.asyncio
async def test_run_single_metric_retries_then_succeeds() -> None:
  metric = MagicMock()
  metric.score = 0.9
  metric.reason = "good"
  metric.success = True
  metric.a_measure = AsyncMock(
    side_effect=[_rate_limit_error(), _rate_limit_error(), None],
  )
  test_case = LLMTestCase(input="in", actual_output="out")

  result = await _run_single_metric("hallucination", metric, test_case)

  assert result.success is True
  assert result.score == 0.9
  assert metric.a_measure.await_count == MAX_ATTEMPTS


@pytest.mark.asyncio
async def test_run_single_metric_logs_and_fails_after_max_attempts(caplog) -> None:
  metric = MagicMock()
  metric.a_measure = AsyncMock(side_effect=_rate_limit_error())
  test_case = LLMTestCase(input="in", actual_output="out")

  with caplog.at_level(logging.WARNING):
    result = await _run_single_metric("hallucination", metric, test_case)

  assert result.success is False
  assert metric.a_measure.await_count == MAX_ATTEMPTS
  assert caplog.text.count("Metric evaluation failed, retrying") == MAX_ATTEMPTS - 1
  assert "Metric evaluation failed after 3 attempts" in caplog.text


@pytest.mark.asyncio
async def test_run_single_metric_no_retry_on_non_transient() -> None:
  metric = MagicMock()
  metric.a_measure = AsyncMock(side_effect=ValueError("bad input"))
  test_case = LLMTestCase(input="in", actual_output="out")

  result = await _run_single_metric("hallucination", metric, test_case)

  assert result.success is False
  assert result.error == "bad input"
  assert metric.a_measure.await_count == 1


@pytest.mark.asyncio
async def test_measure_with_retry_reraises_non_retryable_immediately() -> None:
  measure = AsyncMock(side_effect=ValueError("bad input"))

  with pytest.raises(ValueError, match="bad input"):
    await measure_with_retry(measure, metric_name="hallucination")

  assert measure.await_count == 1

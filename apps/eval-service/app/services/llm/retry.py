"""Retry helpers for transient LLM provider failures during metric evaluation."""

from __future__ import annotations

import logging
from collections.abc import Awaitable, Callable

from openai import APIConnectionError, APIStatusError, APITimeoutError, RateLimitError
from tenacity import (
  AsyncRetrying,
  RetryCallState,
  RetryError,
  retry_if_exception,
  stop_after_attempt,
  wait_exponential,
)

logger = logging.getLogger(__name__)

MAX_ATTEMPTS = 3
BACKOFF_MULTIPLIER = 2
BACKOFF_MIN_SECONDS = 2
BACKOFF_MAX_SECONDS = 60
RETRYABLE_HTTP_STATUSES = frozenset({429, 502, 503})
PROVIDER_UNAVAILABLE_MARKERS = (
  "429",
  "rate limit",
  "502",
  "503",
  "unavailable",
  "overloaded",
)


def is_retryable_llm_error(exc: BaseException) -> bool:
  """Return True when an exception represents a transient LLM provider failure."""
  current: BaseException | None = exc
  seen: set[int] = set()

  while current is not None and id(current) not in seen:
    seen.add(id(current))

    if isinstance(current, RetryError):
      last_attempt = current.last_attempt
      if last_attempt.failed:
        nested = last_attempt.exception()
        if nested is not None and nested is not current:
          return is_retryable_llm_error(nested)
      return False

    if isinstance(current, (RateLimitError, APIConnectionError, APITimeoutError)):
      return True

    if isinstance(current, APIStatusError):
      status_code = _extract_status_code(current)
      if status_code is not None:
        return status_code in RETRYABLE_HTTP_STATUSES

    status_code = _extract_status_code(current)
    if status_code is not None:
      return status_code in RETRYABLE_HTTP_STATUSES

    if _indicates_provider_unavailability(str(current)):
      return True

    current = current.__cause__ or current.__context__

  return False


def _extract_status_code(exc: BaseException) -> int | None:
  status_code = getattr(exc, "status_code", None)
  if isinstance(status_code, int):
    return status_code

  response = getattr(exc, "response", None)
  if response is not None:
    response_status = getattr(response, "status_code", None)
    if isinstance(response_status, int):
      return response_status

  return None


def _indicates_provider_unavailability(message: str) -> bool:
  normalized = message.lower()
  return any(marker in normalized for marker in PROVIDER_UNAVAILABLE_MARKERS)


def _make_before_sleep(metric_name: str) -> Callable[[RetryCallState], None]:
  def _log_before_retry(retry_state: RetryCallState) -> None:
    outcome = retry_state.outcome
    error = outcome.exception() if outcome is not None and outcome.failed else None
    next_wait = retry_state.next_action.sleep if retry_state.next_action is not None else None

    logger.warning(
      "Metric evaluation failed, retrying (attempt %s/%s, next_wait=%ss): %s",
      retry_state.attempt_number,
      MAX_ATTEMPTS,
      next_wait,
      error,
      extra={"metric_name": metric_name, "error": str(error) if error else None},
    )

  return _log_before_retry


async def measure_with_retry(
  measure: Callable[[], Awaitable[None]],
  *,
  metric_name: str,
) -> None:
  """Run an async metric measurement with exponential backoff on transient failures."""
  async for attempt in AsyncRetrying(
    stop=stop_after_attempt(MAX_ATTEMPTS),
    wait=wait_exponential(
      multiplier=BACKOFF_MULTIPLIER,
      min=BACKOFF_MIN_SECONDS,
      max=BACKOFF_MAX_SECONDS,
    ),
    retry=retry_if_exception(is_retryable_llm_error),
    reraise=True,
    before_sleep=_make_before_sleep(metric_name),
  ):
    with attempt:
      await measure()

"""Domain-specific exceptions raised by the service layer."""


class EvaluationServiceError(Exception):
  """Base exception for evaluation service errors."""


class MetricNotFoundError(EvaluationServiceError):
  """Raised when a requested metric name is not registered."""


class InvalidMetricConfigError(EvaluationServiceError):
  """Raised when a metric configuration is missing required parameters."""


class EvaluationNotFoundError(EvaluationServiceError):
  """Raised when an evaluation record cannot be found."""

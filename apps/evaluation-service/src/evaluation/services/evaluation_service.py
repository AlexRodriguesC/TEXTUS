from deepeval.metrics import AnswerRelevancyMetric, GEval
from deepeval.test_case import LLMTestCase

from src.evaluation.core.evaluation import Evaluation


async def evaluate(eval: Evaluation):
    metric = AnswerRelevancyMetric(
        threshold=0.7,
        model="gpt-4.1",
        include_reason=True
    )

    test_case = LLMTestCase(
        input=eval.input,
        actual_output=eval.actual_output
    )

    metric.measure(test_case)

    GEval(
        name=""
    )

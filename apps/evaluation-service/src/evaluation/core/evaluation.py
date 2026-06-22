from pydantic import BaseModel


class Evaluation(BaseModel):
    input: str
    expected_output: str
    actual_output: str
    context: str

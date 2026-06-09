from http import HTTPStatus
from starlette.testclient import TestClient

from backend.main import app

client = TestClient(app)

def test_should_initialise_app():
    assert app.container is not None

def test_should_return_ok_if_healthy():
    expected_response = {"status": HTTPStatus.OK}

    actual_response = client.get("/health")

    assert actual_response.status_code == HTTPStatus.OK
    assert actual_response.json() == expected_response
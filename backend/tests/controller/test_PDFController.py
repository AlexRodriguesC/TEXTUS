from http import HTTPStatus
from unittest.mock import AsyncMock

import pytest
from fastapi.testclient import TestClient

from backend.config import ApplicationSettings
from backend.main import app

client = TestClient(app)


@pytest.fixture(autouse=True)
def mock_pdf_service():
    mock_service = AsyncMock()
    with app.container.pdf_service.override(mock_service):
        yield mock_service


@pytest.fixture(autouse=True)
def tiny_settings():
    settings = ApplicationSettings(max_upload_size_mb=32, api_key="random", provider_url="https://example")
    with app.container.application_settings.override(settings):
        yield settings


@pytest.fixture
def zero_settings():
    settings = ApplicationSettings(max_upload_size_mb=0, api_key="random", provider_url="https://example")
    with app.container.application_settings.override(settings):
        yield settings


def books(content=None, files=None):
    return client.post("/api/v1/books", content=content, files=files)


def test_should_return_error_if_wrong_type(mock_pdf_service):
    response = books(files={"file": ("book.pdf", b"fake content", "app")})

    assert response.status_code == 415
    assert response.json()["detail"] == "Only PDF files are supported!"
    mock_pdf_service.handle_upload.assert_not_called()


def test_should_return_error_if_above_max_size(mock_pdf_service, zero_settings):
    response = books(files={"file": ("book.pdf", b"fake content", "application/pdf")})

    assert response.status_code == HTTPStatus.BAD_REQUEST
    assert "exceeds max allowed size" in response.json()["detail"]
    mock_pdf_service.handle_upload.assert_not_called()


def test_should_return_created(mock_pdf_service):
    mock_pdf_service.handle_upload = AsyncMock(return_value={"id": "abc"})

    response = books(files={"file": ("book.pdf", b"fake content", "application/pdf")})

    assert response.status_code == HTTPStatus.CREATED
    assert response.json() == {"id": "abc"}
    mock_pdf_service.handle_upload.assert_awaited_once()

import openai
from dependency_injector import containers, providers

from backend.config import ApplicationSettings
from backend.service.PDFService import PDFService


class Container(containers.DeclarativeContainer):

    application_settings = providers.Singleton(
        ApplicationSettings
    )

    ai_client = providers.Singleton(
        openai.OpenAI,
        api_key=application_settings.provided.api_key,
        base_url=application_settings.provided.provider_url
    )

    pdf_service = providers.Singleton(
        PDFService
    )

import openai
import redis
from dependency_injector import containers, providers

from backend.config import ApplicationSettings
from backend.data_access.PDFDataAccess import PDFDataAccess
from backend.data_access.PDFFileStorage import PDFFileStorage
from backend.service.PDFService import PDFService


class Container(containers.DeclarativeContainer):
    application_settings = providers.Singleton(
        ApplicationSettings
    )

    r = providers.Singleton(
        redis.Redis,
        host=application_settings.provided.redis_host,
        port=application_settings.provided.redis_port,
        password=application_settings.provided.redis_password,
        decode_responses=True,
    )

    pdf_data_access = providers.Singleton(
        PDFDataAccess,
        redis=r
    )

    pdf_file_storage = providers.Singleton(
        PDFFileStorage
    )

    ai_client = providers.Singleton(
        openai.OpenAI,
        api_key=application_settings.provided.api_key,
        base_url=application_settings.provided.provider_url
    )

    pdf_service = providers.Singleton(
        PDFService,
        pdf_data_access=pdf_data_access,
        pdf_file_storage=pdf_file_storage
    )

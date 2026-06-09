from pydantic_settings import BaseSettings, SettingsConfigDict

class ApplicationSettings(BaseSettings):
    api_key: str = None
    provider_url: str = "https://api.llmgateway.io/v1/"
    max_upload_size_mb: int = 256

    model_config = SettingsConfigDict(
        env_file=".env"
    )



from huey import RedisHuey
from backend.config import ApplicationSettings

settings = ApplicationSettings()

huey = RedisHuey(
    "TEXTUS",
    host=settings.redis_host,
    port=settings.redis_port,
)

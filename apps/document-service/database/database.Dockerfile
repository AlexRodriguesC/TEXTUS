FROM pgvector/pgvector:pg16

ENV POSTGRES_USER=appuser
ENV POSTGRES_PASSWORD=appsecret
ENV POSTGRES_DB=documents

HEALTHCHECK --interval=5s --timeout=5s --retries=10 \
  CMD pg_isready -U appuser -d documents

EXPOSE 5432
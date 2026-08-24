# TEXTUS Evaluation Service

<div align="center">
  <p>FastAPI microservice for evaluating LLM-generated TEXTUS content with G-Eval metrics.</p>

  <p>
    <a href="#getting-started">Get started</a>
    ·
    <a href="#api-reference">API reference</a>
    ·
    <a href="#testing-and-quality">Testing and quality</a>
  </p>
</div>

## Table of Contents

- [About the Project](#about-the-project)
- [Built With](#built-with)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
  - [Run with Docker Compose](#run-with-docker-compose)
  - [Run locally](#run-locally)
- [Usage](#usage)
- [API Reference](#api-reference)
- [Testing and Quality](#testing-and-quality)
- [Acknowledgments](#acknowledgments)

## About the Project

The evaluation service is the LLM-quality sidecar for TEXTUS. It receives generated learning content from the document service and evaluates it asynchronously, so evaluation latency or failures do not block document ingestion.

It persists evaluation records in PostgreSQL and provides configurable built-in and TEXTUS-specific G-Eval metrics.

The document service calls this API at `/api/v1/evaluations`.

### Built With

[![Python][Python-shield]][Python-url]
[![FastAPI][FastAPI-shield]][FastAPI-url]
[![DeepEval][DeepEval-shield]][DeepEval-url]
[![PostgreSQL][PostgreSQL-shield]][PostgreSQL-url]
[![SQLAlchemy][SQLAlchemy-shield]][SQLAlchemy-url]
[![Alembic][Alembic-shield]][Alembic-url]
[![uv][uv-shield]][uv-url]

## Getting Started

### Prerequisites

- Python 3.14 or later
- [uv](https://docs.astral.sh/uv/) for dependency management and commands
- PostgreSQL for local execution, or Docker Desktop and Docker Compose for the containerized stack
- An OpenAI-compatible inference provider for G-Eval metrics

### Configuration

For local execution, create `apps/eval-service/.env`. The service reads this file automatically and it must not contain committed secrets.

```properties
# Database: use either a complete async SQLAlchemy URL ...
EVALUATION_DATABASE_URL=postgresql+asyncpg://appuser:appsecret@localhost:5432/documents

# ... or let the service derive it from the Spring JDBC URL.
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/documents
DATABASE_USERNAME=appuser
DATABASE_PASSWORD=appsecret
EVALUATION_DB_SCHEMA=evaluations

# OpenAI-compatible LLM provider
OPENAI_API_KEY=replace-with-provider-key
OPENAI_BASE_URL=https://your-openai-compatible-provider/v1
EVALUATION_LLM_MODEL=claude-haiku-4-5-free
```

`EVALUATION_DATABASE_URL` takes precedence over `SPRING_DATASOURCE_URL`. If neither is set, the service defaults to a local PostgreSQL database at `localhost:5432/documents` with the `appuser` credentials. The schema name defaults to `evaluations`.

### Run with Docker Compose

The stack definition is maintained by the document service. From `apps/document-service`, start PostgreSQL, the Spring backend, and this evaluation service:

```sh
docker compose up --build
```

Docker Compose reads environment variables from the repository-root `.env` file and makes the service available at [http://localhost:8000](http://localhost:8000). Its container applies Alembic migrations before starting Uvicorn.

### Run locally

Install locked dependencies:

```sh
uv sync --frozen
```

Apply migrations, then start the development server:

```sh
uv run alembic upgrade head
uv run uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

The service verifies the database connection during startup. Check [http://localhost:8000/health](http://localhost:8000/health) after it starts.

## Usage

The document service submits generated content to `POST /api/v1/evaluations`. Evaluation records can then be retrieved or listed, and `GET /api/v1/metrics` exposes the registered metric definitions.

## API Reference

With the service running, use [FastAPI’s interactive documentation](http://localhost:8000/docs) as the canonical API reference. The generated OpenAPI schema is also available at [http://localhost:8000/openapi.json](http://localhost:8000/openapi.json).

## Testing and Quality

Run the test suite:

```sh
uv run pytest
```

Check linting and formatting:

```sh
uv run ruff check .
uv run ruff format --check .
```

## Acknowledgments

- [Best README Template](https://github.com/othneildrew/Best-README-Template), the structural basis for this README
- [FastAPI](https://fastapi.tiangolo.com/), which provides the web API and generated documentation
- [DeepEval](https://github.com/confident-ai/deepeval), which provides LLM evaluation primitives
- [uv](https://docs.astral.sh/uv/), used for dependency management and execution

[Python-shield]: https://img.shields.io/badge/Python-3.14-3776AB?style=for-the-badge&logo=python&logoColor=white
[Python-url]: https://www.python.org/
[FastAPI-shield]: https://img.shields.io/badge/FastAPI-0.115+-009688?style=for-the-badge&logo=fastapi&logoColor=white
[FastAPI-url]: https://fastapi.tiangolo.com/
[DeepEval-shield]: https://img.shields.io/badge/DeepEval-LLM_Evaluation-4B32C3?style=for-the-badge
[DeepEval-url]: https://github.com/confident-ai/deepeval
[PostgreSQL-shield]: https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white
[PostgreSQL-url]: https://www.postgresql.org/
[SQLAlchemy-shield]: https://img.shields.io/badge/SQLAlchemy-2.0-D71F00?style=for-the-badge&logo=sqlalchemy&logoColor=white
[SQLAlchemy-url]: https://www.sqlalchemy.org/
[Alembic-shield]: https://img.shields.io/badge/Alembic-Migrations-4B32C3?style=for-the-badge
[Alembic-url]: https://alembic.sqlalchemy.org/
[uv-shield]: https://img.shields.io/badge/uv-Dependency_Management-DE5FE9?style=for-the-badge&logo=astral&logoColor=white
[uv-url]: https://docs.astral.sh/uv/

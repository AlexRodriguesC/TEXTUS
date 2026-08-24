<div align="center">
  <h1>TEXTUS Document Service</h1>
  <p>Spring Boot backend for turning PDF learning material into structured, adaptive learning experiences.</p>

  <p>
    <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
    <img src="https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4.0.6">
    <img src="https://img.shields.io/badge/PostgreSQL-pgvector-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL with pgvector">
    <img src="https://img.shields.io/badge/Gradle-8-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle">
  </p>

  <a href="#getting-started">Get started</a>
  ·
  <a href="#api-reference">API reference</a>
  ·
  <a href="#testing">Testing</a>
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
- [Testing](#testing)
- [Acknowledgments](#acknowledgments)

## About the Project

The document service is the core backend of TEXTUS. It exposes a JWT-protected REST API and server-sent events (SSE) for managing learning spaces and transforming uploaded PDF documents into reusable learning material.

When a document is uploaded, a Spring Batch pipeline extracts text, applies OCR where needed, summarizes pages, extracts topics, and stores embeddings in PostgreSQL with pgvector. The service then supports mind-map generation, adaptive learning paths and exercises, and retrieval-augmented chat over the uploaded material.

The service also sends generated-content evaluation requests asynchronously to the repository's `eval-service`.

### Built With

[![Java][Java-shield]][Java-url]
[![Spring Boot][SpringBoot-shield]][SpringBoot-url]
[![Spring Batch][SpringBatch-shield]][SpringBatch-url]
[![Spring AI][SpringAI-shield]][SpringAI-url]
[![PostgreSQL][PostgreSQL-shield]][PostgreSQL-url]
[![pgvector][pgvector-shield]][pgvector-url]
[![Flyway][Flyway-shield]][Flyway-url]
[![Tesseract OCR][Tesseract-shield]][Tesseract-url]
[![Gradle][Gradle-shield]][Gradle-url]

## Getting Started

### Prerequisites

- Java 21 (required for the Gradle toolchain)
- Docker Desktop and Docker Compose for the containerized workflow
- An OpenAI-compatible inference provider with chat and embedding support
- Tesseract language data when running the application directly on the host

### Configuration

Docker Compose and the integration-test task read `.env` from the repository root (`../../.env` from this directory). The application also imports a `.env` file from its current working directory, so copy the root file to this directory—or export the variables in your shell—when using `bootRun`. These files are ignored by Git and must not contain committed secrets.

```properties
# Required for local application startup
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/documents
ALLOWED_ORIGINS=http://localhost:5173
OPENAI_API_KEY=replace-with-provider-key
OPENAI_BASE_URL=https://your-openai-compatible-provider/v1
JWT_SECRET_KEY=replace-with-a-long-random-secret
TESSERACT_DATAPATH=/path/to/tessdata

# Optional; defaults to http://localhost:8000
EVAL_SERVICE_BASE_URL=http://localhost:8000

# Optional; defaults to true. Set to false to skip evaluation requests to eval-service
EVAL_SERVICE_ENABLED=true
```

`OPENAI_BASE_URL` must point to an OpenAI-compatible API. The configured embedding model uses 1,536 dimensions, so replacing it requires compatible pgvector configuration. Docker provides `TESSERACT_DATAPATH` for the backend container automatically.

### Run with Docker Compose

From this directory, start PostgreSQL with pgvector, the document service, and the evaluation service:

```sh
docker compose up --build
```

The backend listens on [http://localhost:8080](http://localhost:8080), PostgreSQL on `localhost:5432`, and the evaluation service on `localhost:8000`. Flyway applies database migrations when the backend starts.

Stop the stack with:

```sh
docker compose down
```

Add `-v` only when you intentionally want to remove the persisted database volume.

### Run locally

First start PostgreSQL with pgvector. You can start only the database through Docker Compose:

```sh
docker compose up -d db
```

Then run the Spring Boot service from this directory:

```sh
./gradlew bootRun
```

On Windows PowerShell, use:

```powershell
.\gradlew.bat bootRun
```

## Usage

1. Register a user or authenticate through `/api/v1/auth/register` or `/api/v1/auth/login`.
2. Use the returned JWT as a bearer token for later requests.
3. Create a space, upload a PDF to that space, and monitor the document-processing status.
4. Once processing completes, explore extracted pages and topics, generate mind maps or learning paths, and use the space chat endpoint for grounded questions.

Uploaded documents are limited to 500 MB. The document pipeline creates temporary files under `documents/` and cleans them up after processing.

## API Reference

With the service running, use [Swagger UI](http://localhost:8080/swagger-ui/index.html) as the canonical API reference. It documents request and response schemas for authentication, spaces, documents, pages, topics, mind maps, paths, lessons, chats, and available models.

Login and registration are public. All other endpoints require an `Authorization: Bearer <token>` header. Chat responses are streamed using SSE.

## Testing

Run unit tests:

```sh
./gradlew test
```

Run integration tests:

```sh
./gradlew integrationTest
```

Integration tests use Testcontainers with a `pgvector/pgvector:pg16` PostgreSQL container. The integration-test Gradle task loads variables from the repository-root `.env` file when it exists.

Test reports are written under `build/reports/`.

## Acknowledgments

- [Best README Template](https://github.com/othneildrew/Best-README-Template), the structural basis for this README
- [Springdoc OpenAPI](https://springdoc.org/), which provides the interactive Swagger UI
- [Testcontainers](https://testcontainers.com/), used for PostgreSQL integration tests

[Java-shield]: https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[Java-url]: https://openjdk.org/projects/jdk/21/
[SpringBoot-shield]: https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white
[SpringBoot-url]: https://spring.io/projects/spring-boot
[SpringBatch-shield]: https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white
[SpringBatch-url]: https://spring.io/projects/spring-batch
[SpringAI-shield]: https://img.shields.io/badge/Spring_AI-6DB33F?style=for-the-badge&logo=spring&logoColor=white
[SpringAI-url]: https://spring.io/projects/spring-ai
[PostgreSQL-shield]: https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white
[PostgreSQL-url]: https://www.postgresql.org/
[pgvector-shield]: https://img.shields.io/badge/pgvector-PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white
[pgvector-url]: https://github.com/pgvector/pgvector
[Flyway-shield]: https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white
[Flyway-url]: https://documentation.red-gate.com/flyway
[Tesseract-shield]: https://img.shields.io/badge/Tesseract_OCR-5A5A5A?style=for-the-badge
[Tesseract-url]: https://tesseract-ocr.github.io/
[Gradle-shield]: https://img.shields.io/badge/Gradle-8-02303A?style=for-the-badge&logo=gradle&logoColor=white
[Gradle-url]: https://gradle.org/

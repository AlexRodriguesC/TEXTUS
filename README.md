<div align="center">
  <h1>TEXTUS</h1>
  <p>Turn PDF learning material into structured, adaptive learning experiences.</p>

  <p>
    <a href="https://react.dev/"><img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=white" alt="React 19"></a>
    <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4.0.6"></a>
    <a href="https://spring.io/projects/spring-ai"><img src="https://img.shields.io/badge/Spring_AI-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring AI"></a>
    <a href="https://fastapi.tiangolo.com/"><img src="https://img.shields.io/badge/FastAPI-0.115+-009688?style=for-the-badge&logo=fastapi&logoColor=white" alt="FastAPI"></a>
    <a href="https://github.com/confident-ai/deepeval"><img src="https://img.shields.io/badge/DeepEval-LLM_Evaluation-4B32C3?style=for-the-badge" alt="DeepEval"></a>
    <a href="https://vercel.com/"><img src="https://img.shields.io/badge/Vercel-Deployment-000000?style=for-the-badge&logo=vercel&logoColor=white" alt="Vercel"></a>
    <a href="https://fly.io/"><img src="https://img.shields.io/badge/Fly.io-Deployment-7B3FE4?style=for-the-badge&logo=flydotio&logoColor=white" alt="Fly.io"></a>
  </p>

  <a href="#getting-started">Get started</a>
  ·
  <a href="#architecture">Architecture</a>
  ·
  <a href="#services">Services</a>
  ·
  <a href="#deployment">Deployment</a>
</div>

## Table of Contents

- [About the Project](#about-the-project)
- [Architecture](#architecture)
- [Services](#services)
- [Getting Started](#getting-started)
- [Deployment](#deployment)
- [Acknowledgments](#acknowledgments)

## About the Project

TEXTUS is a learning platform that transforms uploaded PDF material into reusable, adaptive learning content. It extracts and enriches document text, generates summaries, topics, mind maps, learning paths, exercises, and retrieval-grounded chat experiences.

The Spring document service owns authentication, document processing, and the primary API. The React web client provides the learning interface, while the FastAPI evaluation service assesses generated content asynchronously with G-Eval metrics and meta-evaluation workflows.

## Architecture

The end-to-end flow starts with a learner uploading a PDF through the web client. The document service processes the file, stores structured learning material and embeddings, and exposes it through its REST API and server-sent events. It also submits generated content to the evaluation service without blocking document ingestion.

For the complete system design, see the [architecture chapter](docs/thesis/11-architecture.md).

## Services

| Service | Responsibility | Local address | Documentation |
| --- | --- | --- | --- |
| Document service | Spring Boot API, PDF processing, learning-content generation, and RAG chat | `http://localhost:8080` | [README](apps/document-service/README.md) |
| Evaluation service | FastAPI sidecar for DeepEval metrics and meta-evaluation | `http://localhost:8000` | [README](apps/eval-service/README.md) |
| Web client | React single-page application for learning spaces and source material | `http://localhost:5173` | [README](apps/react-webclient/README.md) |
| PostgreSQL | Shared database with pgvector for the local stack | `localhost:5432` | [Document service setup](apps/document-service/README.md#getting-started) |

## Getting Started

### Prerequisites

- Docker Desktop and Docker Compose
- Node.js 22 or later with npm
- An OpenAI-compatible inference provider

For direct, service-specific development, also install Java 21 for the document service and Python 3.14 with `uv` for the evaluation service. Each service README lists its full requirements and configuration.

### Run the local stack

1. Copy the shared environment template and complete the required values:

   ```powershell
   Copy-Item .env.example .env
   ```

   The root template is shared by Docker Compose, but does not include every optional or service-specific setting. Review the linked service READMEs before starting a component directly.

2. Start PostgreSQL, the document service, and the evaluation service:

   ```powershell
   cd apps/document-service
   docker compose up --build
   ```

3. In a second terminal, install and run the React client:

   ```powershell
   cd apps/react-webclient
   npm install
   npm run dev
   ```

Open the URL printed by Vite, normally [http://localhost:5173](http://localhost:5173). The document-service [Swagger UI](http://localhost:8080/swagger-ui/index.html) and evaluation-service [API documentation](http://localhost:8000/docs) are available after the stack starts.

## Deployment

[`.github/workflows/deploy.yml`](.github/workflows/deploy.yml) runs on pushes to `main` and by manual dispatch. It deploys the PostgreSQL database to Fly.io first, then deploys the Spring document service to Fly.io. The React web client is the intended Vercel deployment target.

## Acknowledgments

- [Best README Template](https://github.com/othneildrew/Best-README-Template), the structural basis for the service documentation
- [Spring Boot](https://spring.io/projects/spring-boot) and [Spring AI](https://spring.io/projects/spring-ai)
- [React](https://react.dev/), [FastAPI](https://fastapi.tiangolo.com/), and [DeepEval](https://github.com/confident-ai/deepeval)

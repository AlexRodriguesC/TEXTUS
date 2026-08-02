<div align="center">
  <h1>TEXTUS Web Client</h1>
  <p>React single-page application for creating learning spaces and exploring documents, topics, mind maps, learning paths, and grounded chat.</p>

  <p>
    <img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=white" alt="React 19">
    <img src="https://img.shields.io/badge/TypeScript-6-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript 6">
    <img src="https://img.shields.io/badge/Vite-8-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite 8">
    <img src="https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" alt="Tailwind CSS 4">
  </p>

  <a href="#getting-started">Get started</a>
  ·
  <a href="#usage">Usage</a>
  ·
  <a href="#testing">Testing</a>
</div>

## Table of Contents

- [About the Project](#about-the-project)
- [Built With](#built-with)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
  - [Run locally](#run-locally)
- [Usage](#usage)
- [API Client](#api-client)
- [Testing](#testing)
- [Acknowledgments](#acknowledgments)

## About the Project

The web client is the browser interface for TEXTUS. It connects to the repository's `document-service` REST API to authenticate users and manage learning spaces built from uploaded PDF material.

Authenticated users can inspect document processing, browse extracted topics, generate and edit mind maps, follow adaptive learning paths, complete lessons, and chat with their uploaded material. TanStack Query keeps server data synchronized across the application, while TanStack Router protects the private routes.

### Built With

[![React][React-shield]][React-url]
[![TypeScript][TypeScript-shield]][TypeScript-url]
[![Vite][Vite-shield]][Vite-url]
[![Tailwind CSS][Tailwind-shield]][Tailwind-url]
[![TanStack Query][TanStackQuery-shield]][TanStackQuery-url]
[![TanStack Router][TanStackRouter-shield]][TanStackRouter-url]
[![shadcn/ui][shadcn-shield]][shadcn-url]
[![Vitest][Vitest-shield]][Vitest-url]
[![Orval][Orval-shield]][Orval-url]

## Getting Started

### Prerequisites

- Node.js 22 or later
- npm
- A running instance of the repository's [document service](../document-service/README.md)

### Configuration

Create `.env.local` in this directory with the document service URL:

```properties
VITE_API_BASE_URL=http://localhost:8080
```

Vite exposes only variables prefixed with `VITE_` to browser code. Do not place secrets in this file because the values become part of the built client bundle.

### Run locally

Install dependencies and start the development server:

```sh
npm install
npm run dev
```

Vite prints the local URL, normally [http://localhost:5173](http://localhost:5173). To use the production API configuration while developing, run:

```sh
npm run dev-remote
```

Create an optimized production build with:

```sh
npm run build
```

Preview the generated `dist/` directory locally:

```sh
npm run preview
```

## Usage

1. Start the document service and the web client.
2. Register an account or sign in.
3. Create a learning space and upload a PDF document.
4. Wait for document processing to complete, then review pages and topics.
5. Use the space views to generate mind maps and learning paths, complete lessons, or chat with the source material.

The document service owns authentication, processing, and persisted data. The web client stores the returned JWT in the browser and sends it with protected API requests.

## API Client

The client uses Axios for HTTP requests and Orval to generate typed React Query hooks from the document service OpenAPI specification. With `VITE_API_BASE_URL` set and the backend running, regenerate the API client with:

```sh
npm run generate:api
```

The command reads `${VITE_API_BASE_URL}/v3/api-docs`. Review generated changes before committing them.

## Testing

Run the test suite:

```sh
npm test
```

Run static checks:

```sh
npm run lint
npm run typecheck
```

`typecheck` currently allows the command to complete when TypeScript reports errors. Use the compiler output to address type issues before release.

## Acknowledgments

- [Best README Template](https://github.com/othneildrew/Best-README-Template), the structural basis for this README
- [Vite](https://vite.dev/), the development server and build tool
- [shadcn/ui](https://ui.shadcn.com/), used for accessible UI primitives
- [Orval](https://orval.dev/), used to generate the OpenAPI client

[React-shield]: https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=white
[React-url]: https://react.dev/
[TypeScript-shield]: https://img.shields.io/badge/TypeScript-6-3178C6?style=for-the-badge&logo=typescript&logoColor=white
[TypeScript-url]: https://www.typescriptlang.org/
[Vite-shield]: https://img.shields.io/badge/Vite-8-646CFF?style=for-the-badge&logo=vite&logoColor=white
[Vite-url]: https://vite.dev/
[Tailwind-shield]: https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white
[Tailwind-url]: https://tailwindcss.com/
[TanStackQuery-shield]: https://img.shields.io/badge/TanStack_Query-FF4154?style=for-the-badge&logo=reactquery&logoColor=white
[TanStackQuery-url]: https://tanstack.com/query/latest
[TanStackRouter-shield]: https://img.shields.io/badge/TanStack_Router-FF4154?style=for-the-badge&logo=reactrouter&logoColor=white
[TanStackRouter-url]: https://tanstack.com/router/latest
[shadcn-shield]: https://img.shields.io/badge/shadcn%2Fui-000000?style=for-the-badge&logo=shadcnui&logoColor=white
[shadcn-url]: https://ui.shadcn.com/
[Vitest-shield]: https://img.shields.io/badge/Vitest-3-6E9F18?style=for-the-badge&logo=vitest&logoColor=white
[Vitest-url]: https://vitest.dev/
[Orval-shield]: https://img.shields.io/badge/Orval-8-FF5F57?style=for-the-badge
[Orval-url]: https://orval.dev/

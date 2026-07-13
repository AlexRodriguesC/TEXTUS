import { defineConfig } from 'orval';
import { loadEnv } from 'vite';

const mode = process.env.NODE_ENV || 'development';
const env = loadEnv(mode, process.cwd(), 'VITE_');

export default defineConfig({
  textus: {
    input: {
      target: `${env.VITE_API_BASE_URL}/v3/api-docs`,
    },
    output: {
      mode: 'tags-split',
      target: 'src/client/generated',
      schemas: 'src/client/model',
      client: 'react-query',
      clean: true,
      override: {
        mutator: {
          path: 'src/client/mutator.ts',
          name: 'customInstance',
        },
        query: {
          version: 5,
        },
      },
    },
  },
});
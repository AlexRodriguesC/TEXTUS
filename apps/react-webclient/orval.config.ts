import { defineConfig } from 'orval';

export default defineConfig({
    textus: {
        input: {
            target: 'http://localhost:8080/v3/api-docs',
        },
        output: {
            mode: 'tags-split',           // one file per API tag
            target: 'src/api/generated',
            schemas: 'src/api/model',
            client: 'react-query',
            clean: true,
            override: {
                mutator: {
                    path: 'src/api/mutator.ts',
                    name: 'customInstance',
                },
                query: {
                    version: 5,               // TanStack Query v5
                },
            },
        }
    },
});
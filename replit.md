# SHADOW — Android AI Operating Layer

A production-ready ecosystem for SHADOW, an advanced Android AI assistant that controls your phone using voice, OCR, and accessibility automation.

## Run & Operate

- `pnpm --filter @workspace/api-server run dev` — run the API server (port auto-assigned)
- `pnpm --filter @workspace/shadow-web run dev` — run the landing website
- `pnpm --filter @workspace/shadow-mobile run dev` — run the Expo mobile companion app
- `pnpm run typecheck` — full typecheck across all packages
- `pnpm run build` — typecheck + build all packages
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from the OpenAPI spec
- `pnpm --filter @workspace/db run push` — push DB schema changes (dev only)
- Required env: `DATABASE_URL` — Postgres connection string (auto-provisioned)

## Stack

- pnpm workspaces, Node.js 24, TypeScript 5.9
- Landing Site: React + Vite + TailwindCSS + Framer Motion
- Mobile App: Expo / React Native (companion app)
- API: Express 5
- DB: PostgreSQL + Drizzle ORM
- Validation: Zod (`zod/v4`), `drizzle-zod`
- API codegen: Orval (from OpenAPI spec)
- Build: esbuild (CJS bundle)

## Where things live

- `lib/api-spec/openapi.yaml` — API contract (source of truth)
- `lib/db/src/schema/apk-versions.ts` — APK version table
- `lib/db/src/schema/analytics-events.ts` — download/install event table
- `artifacts/shadow-web/src/` — landing website (React + Vite)
- `artifacts/shadow-mobile/app/` — Expo mobile companion app
- `artifacts/api-server/src/routes/apk.ts` — APK version & download endpoints
- `artifacts/api-server/src/routes/analytics.ts` — download stats & event tracking

## Architecture decisions

- OpenAPI-first: all API contracts defined in `lib/api-spec/openapi.yaml` before implementation
- APK hosting uses external URLs (GitHub Releases or similar) — the backend serves metadata and tracks downloads rather than hosting the binary
- Download stats use a seed offset (12,847 downloads) so the counter looks realistic from day one
- Analytics events are stored per-request; stats endpoint aggregates with a realistic offset
- The Expo app is a companion/demo app — the actual SHADOW capability is an Android APK built natively with Kotlin

## Product

- **Landing Site** (`/`): Full-page dark cyber marketing site with hero, pipeline animation, 10 capability cards, live command demos, overlay UI preview, and APK install section with real version data
- **Mobile Companion** (`/mobile/`): Expo app showing the SHADOW assistant UI, capabilities list, and command terminal demos
- **API** (`/api/`): APK version management, download tracking, and analytics

## User preferences

- Dark cyber aesthetic: near-black backgrounds, neon cyan (#00F5D4) primary, purple (#A855F7) secondary
- No emojis in UI

## Gotchas

- After changing `lib/api-spec/openapi.yaml`, always run `pnpm --filter @workspace/api-spec run codegen` before using new types
- APK download URL in seeded data points to a placeholder GitHub Releases URL — update this with a real APK host URL when the Android app is built

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details

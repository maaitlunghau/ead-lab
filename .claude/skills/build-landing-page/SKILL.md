---
name: build-landing-page
description: >-
  Build the Heatwave Showcase #3 APOCALYPSE landing page. Orchestrates parallel sub-agents
  to create 13 React components, 2 hooks, global CSS, and App shell from TASK.md specs.
  Use when: "build landing page", "build heatwave page", "build SHC3 page", "tạo landing page",
  "xây dựng trang landing", "thực hiện task", "build components", "xây trang sự kiện".
  Also triggers on: "rebuild landing page", "update landing page", "fix landing page",
  "chạy lại landing", "sửa landing page".
---

# Build Landing Page — Orchestrator

Build the Heatwave SHC3 APOCALYPSE landing page using parallel sub-agents.

## Execution Mode: Sub-agent (fan-out/fan-in)

No inter-agent communication needed — each component is fully specified in TASK.md.

## Phase 0: Context Check

1. Check if `frontend/src/components/Nav.tsx` exists
   - Exists + user requests partial rebuild → rebuild only requested components
   - Exists + user requests full rebuild → rebuild all
   - Not exists → initial build

2. Check if `frontend/public/assets/images/` exists
   - Exists → skip asset copy
   - Not exists → run setup phase

## Phase 1: Setup (Sequential)

Spawn `landing-setup` agent (model: opus):
- Copy assets from source folder to `frontend/public/assets/`
- Write `frontend/src/index.css` (global tokens from TASK.md)
- Write hooks: `useCountdown.ts`, `useScrollReveal.ts`
- Write `frontend/src/App.tsx` shell

**Data source:** TASK.md at project root
**Asset source:** `C:\Users\MSI\Downloads\FStyle Crew Design System\assets\element\`

Wait for setup completion before Phase 2.

## Phase 2: Build Components (Parallel Fan-out)

Spawn 4 `landing-builder` agents in parallel (model: opus, run_in_background: true):

| Agent | Components |
|-------|-----------|
| Builder A | Nav, Hero, ParticleCanvas, MarqueeBand |
| Builder B | About, Concept, Teams |
| Builder C | FCode, ShowcaseNight, Timeline |
| Builder D | Partners, Awards, Club, Footer |

Each agent reads TASK.md for its component specs and writes .tsx files.

**Grouping rationale:** Components grouped by page section for natural context locality.

## Phase 3: Assembly (Sequential)

After all builders complete:
1. Verify all 13 component files exist
2. Update App.tsx if needed (should already be correct from Phase 1)
3. Clean up any temporary files

## Phase 4: QA (Sequential)

Spawn `landing-qa` agent (model: opus):
- TypeScript build check
- File completeness verification
- Asset reference check
- Fix any compilation errors

## Error Handling

- Builder fails → re-run that builder only with error context
- Asset copy fails → report missing files, continue with available assets
- Build fails → QA agent fixes TypeScript errors
- After 1 retry failure → report to user with error details

## Test Scenarios

**Happy path:**
1. Setup copies assets, writes CSS/hooks/App
2. 4 builders create 13 components in parallel
3. QA verifies build passes
4. Dev server shows complete landing page

**Missing assets:**
1. Setup reports missing pptx-images
2. Builders use placeholder comments for missing images
3. QA reports missing assets list
4. User provides missing files manually

## File Output Map

```
frontend/
├── public/assets/
│   ├── images/    (all .png files)
│   └── fonts/     (all .otf/.ttf files)
└── src/
    ├── index.css
    ├── App.tsx
    ├── main.tsx
    ├── hooks/
    │   ├── useCountdown.ts
    │   └── useScrollReveal.ts
    └── components/
        ├── Nav.tsx
        ├── Hero.tsx
        ├── ParticleCanvas.tsx
        ├── MarqueeBand.tsx
        ├── About.tsx
        ├── Concept.tsx
        ├── Teams.tsx
        ├── FCode.tsx
        ├── ShowcaseNight.tsx
        ├── Timeline.tsx
        ├── Partners.tsx
        ├── Awards.tsx
        ├── Club.tsx
        └── Footer.tsx
```

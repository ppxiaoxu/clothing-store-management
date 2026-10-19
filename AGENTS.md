# Repository guide

## Scope and conventions

- Backend: `Cloth_management_System/` (Java 21, Spring Boot, MyBatis-Plus). Frontend: `cloth-store-frontend/` (Vue 3, Element Plus).
- Read the relevant Controller, Service, Mapper and neighboring UI before changing a behavior. Reuse `Result`, DTO validation and the global exception handler.
- Inventory changes belong in the shared `StockService`. Consider the business record, SKU quantity and stock record together; do not bypass this boundary with a second stock update.
- Keep each change within the user's task. Preserve existing local changes; do not stage all files, publish credentials, or include unrelated business files. Commit/push only within the user's current authorization; do not request approval again when it was already given.
- Record verification commands, observed results and the behavior each check covers. Use business acceptance scenarios for inventory, permissions and concurrency, alongside builds and application startup checks.

## Agent skills

The six engineering workflows are in `.agents/skills/`: `grill-with-docs`, `to-spec`, `to-tickets`, `tdd`, `code-review`, `diagnosing-bugs`. Their referenced helpers are included. See `docs/ai-coding/README.md` for provenance, examples and installation checks.

Read a selected skill and only the supporting resources needed for the task. Skills guide work; they do not grant permission to change scope or publish externally. Preserve upstream explicit-invocation settings. Use the host's skill selector, or explicitly name the skill and its `SKILL.md` path if necessary.

### Issue tracker

Use local Markdown for learning and project tasks: `.scratch/<feature>/spec.md` and one file per ticket under `.scratch/<feature>/issues/`. See `docs/agents/issue-tracker.md`. Do not translate a skill's “publish” step into creating remote GitHub issues without an explicit request.

### Domain docs

This is a single-domain project. Read `GLOSSARY.md` and any relevant existing ADRs, as described in `docs/agents/domain.md`.

## Verification

- Skills-only changes: `python scripts/verify_agent_skills.py` (Python 3.10+, standard library).
- Backend behavior changes: select meaningful tests for the changed behavior. `mvn test` runs from `Cloth_management_System/`; the current single `contextLoads` test may require the configured database.
- Frontend behavior changes: `npm run build` from `cloth-store-frontend/`, plus the affected UI/API acceptance scenarios. There is currently no frontend test command.
- The public snapshot has missing business source dependencies compared with the local working tree. Check tracked/untracked files before claiming a clean clone can build; do not automatically publish missing local files to fix that difference.

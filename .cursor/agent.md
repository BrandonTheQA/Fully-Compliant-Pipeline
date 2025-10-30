# Run tests before every commit (Cursor Agent Policy)

## Goal

Ensure all unit tests pass before any commit. The agent must run tests across the monorepo, attempt automated fixes on failures, then block the commit if failures remain.

## Scope

- UI: `ui/` (Vite + React + Jest/RTL)
- Java Azure Functions: `api/functionapp/order`, `api/functionapp/product`, `api/functionapp/user` (Maven unit tests)

## Trigger

- Before committing code (commit/create PR actions in Cursor)

## Test Commands

- UI
  - `cd ui`
  - `npm ci --silent`
  - `npm run test:ci --if-present || npm test -- --ci --passWithNoTests`
- Java (run for each module containing `pom.xml` under `api/functionapp/*`)
  - `mvn -q -B -DskipITs -DfailIfNoTests=false test`

## Parallelization

- Run UI and each Java module tests in parallel where possible to reduce time.

## Auto-fix Strategy on Failure
1. Collect failures and stack traces. Identify failing tests and implicated source files.
2. Quick repairs, then re-run relevant tests:
   - UI (best-effort):
     - `npm run lint -- --fix --if-present`
     - `npm run fmt --if-present` (or `prettier --write .` if configured)
     - Re-run `npm test -- --ci`
     - If snapshot failures are the only failures and changes are intentional, update snapshots: `npm test -- -u` (only if diff shows trivial render changes)
   - Java (best-effort):
     - Rebuild modules: `mvn -q -B -DskipITs -DfailIfNoTests=false -DskipTests=false -Dspotless.apply verify` if Spotless is configured; otherwise `mvn -q -B -DskipITs test`
3. Targeted code fixes:
   - Prefer minimal, localized edits to satisfy failing assertions without broad refactors.
   - Maintain type safety and existing public APIs.
   - Add/adjust tests only when spec is clearly encoded in existing tests.
4. Re-run only affected test suites first; if passing, re-run full test suite across all projects to confirm.

## Commit Gate

- If any unit tests still fail after auto-fix attempts, abort the commit and present a concise failure report with suggested edits.
- If all tests pass, proceed with committing.

## Skip Mechanisms (rare)

- Allow skip only if:
  - Commit message contains `[skip-tests]`, or
  - Env var `SKIP_TESTS=1` is set.
- When skipped, warn and include in the commit description that tests were skipped intentionally.

## Caching/Environment

- Prefer `npm ci` over `npm install` for reproducibility.
- Use Maven batch mode `-B` and quiet `-q` to keep logs readable.

## Output Requirements

- Summarize actions taken, duration, and results per project.
- On failures, include:
  - Failing test names/files
  - Error messages and top frames
  - Proposed fix summaries and diffs (minimal edits)
  - Next steps if auto-fix was insufficient

## Safety/Boundaries

- Don’t introduce large refactors automatically.
- Don’t change external APIs without explicit confirmation.
- Don’t update Jest snapshots unless failure is snapshot-only and code changes clearly intended.


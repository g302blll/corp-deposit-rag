# Frontend Split and One-Click Startup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split the repository into independent Vue and Java projects and make the complete demo start from one Windows BAT file.

**Architecture:** Vue 3/Vite runs on port 5173 and proxies `/api` to the mock-first assistant on port 8084. A BAT entry point delegates process management to a PowerShell script that builds, launches, waits, records PIDs, and opens the browser.

**Tech Stack:** Java 17, Maven Wrapper, Spring Boot, Vue 3, Vite, Vitest, Windows Batch and PowerShell.

---

### Task 1: Split the backend tree

**Files:** move `pom.xml`, `.mvn`, `mvnw.cmd`, `common-*`, `*-service`, and `docs/sql` into `corp-deposit-rag-server`; modify Maven-relative documentation.

- [ ] Move tracked backend files with Git history preserved.
- [ ] Run `corp-deposit-rag-server\mvnw.cmd test` from the server directory.
- [ ] Confirm all existing tests pass and commit.

### Task 2: Build the Vue test console with TDD

**Files:** create `corp-deposit-rag-web/package.json`, `vite.config.js`, `index.html`, `src/api.js`, `src/api.test.js`, `src/App.vue`, `src/main.js`, and `src/styles.css`.

- [ ] Write API tests asserting POST payloads for `/api/v1/assistant/plans` and `/api/v1/assistant/intentions` plus readable error propagation.
- [ ] Run `npm test` and confirm failure because `src/api.js` is absent.
- [ ] Implement the API module and run tests to green.
- [ ] Implement the Vue page that consumes the tested API, then run `npm run build`.
- [ ] Commit the frontend.

### Task 3: Implement one-click lifecycle scripts

**Files:** create `start-all.bat`, `stop-all.bat`, `corp-deposit-rag-server/scripts/start-all.ps1`, and `corp-deposit-rag-server/scripts/stop-all.ps1`; modify `.gitignore`.

- [ ] Add preflight checks, clean builds, PID/log persistence, readiness polling, browser launch and failure cleanup.
- [ ] Add safe PID ownership validation to the stop script.
- [ ] Run `start-all.bat` with `MYSQL_PASSWORD` set and verify ports 5173/8081-8084.
- [ ] Execute recommendation and intention API smoke tests through the Vite proxy.
- [ ] Run `stop-all.bat` and confirm all five ports close.
- [ ] Commit the scripts.

### Task 4: Documentation and final verification

**Files:** modify `README.md`, `ARCHITECTURE.md`, and `PROJECT_STATE.md`.

- [ ] Document the two-folder layout and double-click workflow.
- [ ] Run `mvnw.cmd clean package`, `npm test`, and `npm run build` fresh.
- [ ] Check `git diff --check` and ensure no credentials or runtime artifacts are tracked.
- [ ] Commit, fast-forward `main`, and push GitHub.


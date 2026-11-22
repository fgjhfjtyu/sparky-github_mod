# Contributing to Sparky

Thank you for contributing. This document describes the workflow, standards,
and process for adding code to the project.

---

## Table of Contents

1. [Branch Strategy](#1-branch-strategy)
2. [Commit Conventions](#2-commit-conventions)
3. [Pull Request Process](#3-pull-request-process)
4. [Development Workflow](#4-development-workflow)
5. [Checklist Before Opening a PR](#5-checklist-before-opening-a-pr)
6. [Adding a New Feature](#6-adding-a-new-feature)
7. [Bug Fix Workflow](#7-bug-fix-workflow)
8. [Release Process](#8-release-process)

---

## 1. Branch Strategy

We use **Git Flow** adapted for a single-team Android project.

### Persistent branches

| Branch    | Purpose                                         | Protected |
|-----------|-------------------------------------------------|-----------|
| `main`    | Stable, released code only                      | Yes       |
| `develop` | Integration branch — all features merge here    | Yes       |

### Working branches

| Pattern                    | Example                              | Purpose              |
|----------------------------|--------------------------------------|----------------------|
| `feature/<ticket>-slug`    | `feature/SP-12-undo-redo`            | New functionality    |
| `fix/<ticket>-slug`        | `fix/SP-34-crash-on-empty-circuit`   | Bug fixes            |
| `refactor/<slug>`          | `refactor/extract-canvas-renderer`   | Code quality         |
| `docs/<slug>`              | `docs/engine-math-reference`         | Documentation only   |
| `release/<version>`        | `release/1.2.0`                      | Release preparation  |
| `hotfix/<version>-slug`    | `hotfix/1.1.1-export-null-crash`     | Emergency prod fixes |

### Rules

- Always branch from `develop` (except hotfixes — branch from `main`)
- Never commit directly to `main` or `develop`
- Delete the working branch after merge
- One logical change per branch

---

## 2. Commit Conventions

We follow **Conventional Commits** (https://www.conventionalcommits.org).

### Format

```
<type>(<scope>): <short description>

[optional body]

[optional footer: BREAKING CHANGE, Closes #issue]
```

### Types

| Type       | When to use                                          |
|------------|------------------------------------------------------|
| `feat`     | New feature (adds functionality visible to the user) |
| `fix`      | Bug fix                                              |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `test`     | Adding or updating tests                             |
| `docs`     | Documentation only                                   |
| `chore`    | Build scripts, Gradle, CI config                     |
| `perf`     | Performance improvement                              |
| `style`    | Formatting, no logic change                          |

### Scope (optional but recommended)

Use the layer or module affected:

```
feat(engine): add AC analysis frequency sweep
fix(editor): prevent NaN on empty resistance field
refactor(canvas): extract ComponentRenderer from CircuitCanvas
docs(engine): document transient companion model math
chore(gradle): upgrade Compose BOM to 2025.05
```

### Examples

```
feat(engine): implement DC operating point analysis with MNA

Uses Modified Nodal Analysis with Gaussian elimination.
Supports R, C (open), L (short), V source, I source.

Closes #12
```

```
fix(export): prevent crash when circuit has no components

PdfExporter called drawText on null canvas rect.
Added early return with AppResult.Error instead.

Closes #45
```

### Rules

- Subject line: max 72 characters
- Subject: imperative mood ("add", "fix", "remove" — not "added", "fixes")
- Body: explain **why**, not what (the diff shows what)
- Reference issues/tickets in footer

---

## 3. Pull Request Process

### Opening a PR

1. Open the PR from your feature branch → `develop`
2. Fill in the PR template (see below)
3. Assign yourself and at least one reviewer
4. Link the issue/ticket in the PR description

### PR Template

```markdown
## Summary
<!-- What does this PR do? One paragraph. -->

## Changes
- [ ] feature/fix/refactor description
- [ ] tests added or updated
- [ ] documentation updated (if applicable)

## Testing
<!-- How was this tested? Which devices/emulators? -->

## Screenshots (if UI change)
<!-- Before / After -->

## Checklist
- [ ] Code follows CODE_STANDARDS.md
- [ ] No files > 200 lines
- [ ] No business logic in Composables
- [ ] AppResult used for error handling — no raw exceptions
- [ ] New public APIs have KDoc
- [ ] Tests pass locally
```

### Review standards

- At least **1 approval** required to merge
- Reviewer must run the build and tests locally for changes to `engine/`
- Reviewer can request up to 2 rounds of changes — after that, escalate to team discussion
- Author resolves all comments before merging (mark as resolved, not just replied)

### Merge strategy

- **Squash merge** into `develop` for features/fixes — keeps history linear
- **Regular merge** (no squash) into `main` from `release/*` branches — preserves release boundary

### Draft PRs

Open a draft PR early if you want feedback on architecture before the implementation is complete.
Label it `[WIP]` and convert to ready when done.

---

## 4. Development Workflow

```
1. Pick a ticket / create an issue
2. Create branch: git checkout -b feature/SP-XX-my-feature develop
3. Implement, committing small atomic commits
4. Run tests: ./gradlew test
5. Run lint: ./gradlew lint
6. Push and open PR → develop
7. Address review comments
8. Squash merge after approval
9. Delete branch
```

### Local setup

```bash
git clone https://github.com/jackby03/sparky.git
cd sparky

# Open in Android Studio Meerkat or later
# Sync Gradle, then build
./gradlew assembleDebug

# Run all unit tests
./gradlew test

# Run lint
./gradlew lint

# Run connected (device) tests
./gradlew connectedAndroidTest
```

### Environment requirements

- Android Studio Meerkat (2024.3+)
- JDK 11 (bundled with AS)
- Android SDK 36
- Emulator: API 29+ (Pixel 5 recommended for phone, Pixel Tablet for tablet layout)

---

## 5. Checklist Before Opening a PR

Copy this checklist into your PR description.

**Code quality**
- [ ] No file exceeds 200 lines
- [ ] No function exceeds 20 lines
- [ ] No `!!` non-null assertion outside tests
- [ ] No business logic in `*Screen.kt` Composables
- [ ] No Android imports in `domain/` or `engine/`

**Architecture**
- [ ] Use cases are in `domain/usecase/`
- [ ] Repository calls go through use cases, not directly from ViewModel
- [ ] New Hilt bindings are in the appropriate `core/di/` module
- [ ] New component types have a stamp in `engine/stamp/`

**Naming**
- [ ] Files match the class/object/interface they define
- [ ] Use cases end in `UseCase`
- [ ] ViewModels end in `ViewModel`
- [ ] States end in `State`, Intents end in `Intent`

**Testing**
- [ ] Unit tests added for engine changes
- [ ] Unit tests added for new use cases
- [ ] All existing tests pass (`./gradlew test`)

**Documentation**
- [ ] KDoc added for non-obvious public APIs
- [ ] `ARCHITECTURE.md` updated if a structural decision changed
- [ ] `ENGINE.md` updated if solver logic changed
- [ ] `ROADMAP.md` updated if a milestone was completed

---

## 6. Adding a New Feature

Follow the layered order — never skip layers:

```
1. Domain model        domain/model/...
2. Use case            domain/usecase/...
3. Repository interface (if new data source)
4. Engine changes      engine/stamp/ or engine/solver/
5. Data implementation data/repository/ or data/export/
6. Hilt wiring         core/di/
7. ViewModel           presentation/screen/.../...ViewModel.kt
8. State + Intent      presentation/screen/.../...State.kt + ...Intent.kt
9. Composable UI       presentation/screen/.../...Screen.kt
10. Unit tests for 1–7
```

This order ensures each layer compiles cleanly before the next is built on it.

---

## 7. Bug Fix Workflow

```
1. Reproduce the bug — write a failing test first
2. Identify the layer where the defect lives
3. Fix the minimal amount of code necessary
4. Verify the test now passes
5. Check for similar bugs in adjacent code
6. Open PR with fix and test together
```

Never fix a symptom without understanding the cause.
Never add error handling for a case that "cannot happen" — understand why it can.

---

## 8. Release Process

### Creating a release

```bash
# From develop
git checkout -b release/1.2.0

# Bump version in app/build.gradle.kts
# versionCode += 1
# versionName = "1.2.0"

# Update ROADMAP.md — mark completed items
# Run full test suite
./gradlew test connectedAndroidTest

# Merge to main
git checkout main
git merge --no-ff release/1.2.0
git tag -a v1.2.0 -m "Release 1.2.0"

# Back-merge to develop
git checkout develop
git merge --no-ff release/1.2.0

git branch -d release/1.2.0
```

### Hotfix

```bash
git checkout -b hotfix/1.1.1-crash main
# Fix, test, bump versionCode + versionName
git checkout main
git merge --no-ff hotfix/1.1.1-crash
git tag -a v1.1.1 -m "Hotfix 1.1.1"
git checkout develop
git merge --no-ff hotfix/1.1.1-crash
git branch -d hotfix/1.1.1-crash
```

### Version numbering

`MAJOR.MINOR.PATCH` — Semantic Versioning:
- `MAJOR`: breaking change to saved circuit format or public API
- `MINOR`: new feature, backward compatible
- `PATCH`: bug fix, no new features

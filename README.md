# Sparky — Electromechanical Systems Simulator

> A professional-grade Android application for designing and simulating electromechanical systems.
> Build electrical diagrams using basic components such as relays, switches, pushbuttons, contactors,
> timers, motors, and more — powered by Jetpack Compose, Clean Architecture, and a custom
> Modified Nodal Analysis engine.

---

## Table of Contents

- [Overview](#overview)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Engine](#engine)
- [Export](#export)
- [Documentation](#documentation)
- [Roadmap](#roadmap)
- [Contributing](#contributing)

---

## Overview

Sparky helps engineers, students, and hobbyists:

- **Design** electromechanical diagrams on an infinite canvas with snap-to-grid
- **Build** using components like relays, switches, pushbuttons, contactors, timers, and motors
- **Simulate** using Modified Nodal Analysis (DC, AC, Transient)
- **Inspect** node voltages and branch currents directly on the schematic
- **Export** to PDF, PNG, SVG, or DXF (CAD)
- **Adapt** seamlessly between phone and tablet layouts

The UI is dark-first: black canvas, neon circuit elements, and zero visual noise —
optimized for schematic work, not a general-purpose app.

---

## Expanded component library

The editor palette includes **592 component types** (the original seven simulation elements plus a broad catalog grouped as Protection, Control, Machines & Drives, Automation, Measurement, Electronics, and Wiring. Added symbols include MCB/MCCB/ACB/RCCB/RCBO breakers, fuses, contactors, NO/NC contacts, relays, timers, pushbuttons, E-stop, selector/limit/pressure/float switches, SSR, motors, transformers, VFD, servo/stepper drives, PLC I/O, sensors, CT/PT, meters, semiconductor devices, lamps, buzzers, terminals, and cable links).

**Simulation scope:** original resistor, capacitor, inductor, voltage/current source, and diode models retain their existing solver behavior. Newly added catalog devices are schematic symbols only; they can be placed, wired, moved, rotated, saved, and exported, but are excluded from electrical simulation until dedicated device models are implemented and validated.

## Tech Stack

| Concern               | Technology                        | Version   |
|-----------------------|-----------------------------------|-----------|
| UI                    | Jetpack Compose + Material 3      | BOM 2025.02 |
| Architecture pattern  | MVI + Clean Architecture          | —         |
| Dependency injection  | Hilt                              | 2.56      |
| Navigation            | Navigation Compose                | 2.8.9     |
| Local storage         | Room                              | 2.7.0     |
| Async                 | Kotlin Coroutines + Flow          | 1.10.1    |
| Serialization         | kotlinx.serialization             | 1.8.0     |
| Adaptive layouts      | WindowSizeClass (Material 3)      | 1.4.0     |
| Build system          | Gradle (Kotlin DSL) + KSP         | 9.3.1     |
| Min SDK               | 29 (Android 10)                   | —         |
| Target SDK            | 36                                | —         |
| Language              | Kotlin 2.0.21                     | —         |

---

## Architecture

Sparky follows **Clean Architecture** with a strict **MVI** presentation pattern.
Dependencies only flow inward — the domain and engine layers have zero Android imports.

```
┌─────────────────────────────────────────────────────────┐
│                     PRESENTATION                        │
│  Screen → Intent → ViewModel → State → Composable       │
│  (Jetpack Compose, Hilt, Navigation, WindowSizeClass)   │
├─────────────────────────────────────────────────────────┤
│                       DOMAIN                            │
│  Use Cases  ←  Repository interfaces  ←  Domain models  │
│  (Pure Kotlin — no Android, no framework deps)          │
├──────────────────────┬──────────────────────────────────┤
│        ENGINE        │              DATA                │
│  MNA Solver          │  Room (SQLite)                   │
│  DC / AC / Transient │  CircuitRepositoryImpl           │
│  Stamp pattern       │  PDF / Image / SVG exporters     │
│  (Pure Kotlin)       │  kotlinx.serialization JSON      │
└──────────────────────┴──────────────────────────────────┘
                        ↑
                   CORE (cross-cutting)
         AppResult · Extensions · DI Modules · Constants
```

### Layer responsibilities

| Layer        | Rule                                                          |
|--------------|---------------------------------------------------------------|
| `core`       | Cross-cutting utilities. No business logic, no UI.            |
| `domain`     | Business rules only. No Android imports. Fully unit-testable. |
| `engine`     | Pure math. Stateless. No coroutines, no Android.              |
| `data`       | I/O only. Implements domain interfaces. Room, JSON, File.     |
| `presentation` | Renders state. Emits intents. Zero business logic.          |

---

## Project Structure

```
app/src/main/java/com/jackby03/sparky/
│
├── SparkApp.kt                          Hilt application class
├── MainActivity.kt                      Single activity entry point
│
├── core/
│   ├── constants/AppConstants.kt        Canvas, simulation, export constants
│   ├── di/                              Hilt modules (DB, Engine, Repository)
│   ├── extensions/                      Flow, Math, Compose extension functions
│   └── result/AppResult.kt             Sealed Result<T> + AppException hierarchy
│
├── domain/
│   ├── model/
│   │   ├── circuit/                     Circuit, Node, Wire, Waypoint
│   │   ├── component/                   CircuitComponent sealed class + subtypes
│   │   ├── simulation/                  SimulationConfig, SimulationResult
│   │   ├── export/                      ExportConfig, ExportFormat, PageSize
│   │   └── project/                     Project
│   ├── repository/                      CircuitRepository, ProjectRepository (interfaces)
│   └── usecase/
│       ├── circuit/                     Create, Save, Load, Add, Remove, Connect, Validate
│       ├── simulation/                  RunSimulation
│       └── export/                      ExportToPdf, ExportToImage, ExportToSvg
│
├── engine/
│   ├── model/                           Matrix, SolverResult, FailureReason
│   ├── solver/                          CircuitSolver, MnaMatrixBuilder,
│   │                                    GaussianElimination, DcAnalyzer
│   ├── stamp/                           ComponentStamp (interface), ResistorStamp,
│   │                                    VoltageSourceStamp, CurrentSourceStamp
│   └── validator/                       CircuitValidator, ValidationError
│
├── data/
│   ├── local/
│   │   ├── dao/                         CircuitDao, ProjectDao
│   │   ├── entity/                      CircuitEntity, ProjectEntity
│   │   └── database/SparkDatabase.kt   Room database
│   ├── mapper/                          CircuitMapper, SerializableComponent
│   ├── repository/                      CircuitRepositoryImpl
│   └── export/                          PdfExporter, ImageExporter, SvgExporter
│
└── presentation/
    ├── theme/                           Color, Typography, Shape, SparkTheme
    ├── navigation/                      AppNavGraph, Screen
    ├── components/
    │   ├── canvas/                      CircuitCanvas, ComponentRenderer,
    │   │                                WireRenderer, GridOverlay
    │   ├── toolbar/                     ComponentPalette, SimulationControls, ZoomControls
    │   └── common/                      SparkButton, SparkDialog, PropertyPanel
    └── screen/
        ├── projects/                    ProjectsScreen + ViewModel + State
        ├── editor/                      EditorScreen + ViewModel + State + Intent
        ├── simulation/                  SimulationScreen + ViewModel + State
        └── export/                      ExportScreen + ExportViewModel + ExportState
```

---

## Getting Started

### Prerequisites

- Android Studio Meerkat (2024.3+) or later
- JDK 11
- Android SDK 36
- Gradle 9.3.1 (managed by wrapper)

### Clone and run

```bash
git clone https://github.com/jackby03/sparky.git
cd sparky
```

Open in Android Studio → Run on device or emulator (API 29+).

### Build variants

| Variant | Notes                              |
|---------|------------------------------------|
| `debug` | `applicationId` suffix `.debug`, minification off |
| `release` | ProGuard/R8 enabled               |

---

## Engine

Sparky uses **Modified Nodal Analysis (MNA)** to solve circuits.

The solver pipeline:

```
Circuit → Validate → Build MNA matrix → Gaussian Elimination → Solution vector
                                                                      ↓
                                              nodeVoltages + branchCurrents
```

Each component implements the `ComponentStamp` interface and writes its own
contribution into the conductance matrix `G` and the current vector `I`.
The solver never knows what component it is talking to.

See [`docs/ENGINE.md`](docs/ENGINE.md) for the full mathematical reference.

---

## Export

| Format | Output                  | Status  |
|--------|-------------------------|---------|
| PDF    | A4/A3/Letter schematic  | v1      |
| PNG    | Raster image up to 4K   | v1      |
| JPEG   | Compressed raster        | v1      |
| SVG    | Scalable vector          | v1      |
| DXF    | CAD-compatible           | v2      |

---

## Documentation

| Document                                     | Description                              |
|----------------------------------------------|------------------------------------------|
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Layer design, patterns, decision log   |
| [`docs/ENGINE.md`](docs/ENGINE.md)             | MNA math, stamp pattern, solver details |
| [`docs/CODE_STANDARDS.md`](docs/CODE_STANDARDS.md) | Naming, file rules, SOLID guidelines |
| [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md) | Branch strategy, PR process, style     |
| [`docs/ROADMAP.md`](docs/ROADMAP.md)           | Versioned feature roadmap              |

---

## Roadmap

See [`docs/ROADMAP.md`](docs/ROADMAP.md) for the full versioned plan.

**v1.0 — Foundation** · Canvas editor, DC simulation, PDF/image export
**v2.0 — Analysis** · AC, Transient, oscilloscope view, DXF export
**v3.0 — Platform** · Cloud sync, component library, script engine

---

## Contributing

See [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md).

---

## License

MIT License — see `LICENSE` file.


## Expanded component catalog

The schematic palette now contains **592 component types** across protection and switchgear,
industrial control, motors and drives, PLC/automation, process instrumentation, metering,
power distribution and wiring, building services, and electronic circuits.

Many of the additional catalog entries are IEC-inspired schematic placeholders (`SymbolComponent`).
They can be placed, moved, rotated, labeled, connected as two-terminal symbols, saved, and exported.
They are **not** automatically validated electrical models: simulation behavior must be implemented
per device before using them for electrical calculations or safety decisions.


## Schematic symbols

Catalog symbols are drawn from vector definitions in `core/symbols` (IEC 60617 style) instead of
labelled boxes: contacts (NO/NC/changeover), pushbuttons, e-stop, selector/limit/pressure/float
switches, breakers (MCB/MCCB/ACB/RCCB/RCBO), disconnectors, fuses, coils, timers, motors, meters,
transformers, semiconductors, logic gates, sensors, lamps and wiring items. The same definitions
drive both the editor canvas and the PDF/PNG/SVG export. Devices without a standardised graphical
symbol (PLC modules, gateways, HVAC units, ...) use a labelled box. Terminals stay at (-24, 0) and
(24, 0) for every symbol.


## Download & build

**APK:** every push to `main` builds a debug APK in GitHub Actions. Open the *Actions* tab, pick the
latest *Android CI* run and download the `sparky-debug-apk` artifact. Pushing a tag such as `v1.0.0`
attaches the APK to a GitHub Release.

**Local build:**

```bash
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # unit tests
```

Requires JDK 17 and the Android SDK (compileSdk 36). Install on a device with
`adb install app/build/outputs/apk/debug/app-debug.apk`; minimum Android version is 10 (API 29).

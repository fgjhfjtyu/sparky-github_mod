# Architecture — Sparky Electromechanical Systems Simulator

## Table of Contents

1. [Guiding Principles](#1-guiding-principles)
2. [Layer Model](#2-layer-model)
3. [Dependency Rule](#3-dependency-rule)
4. [Core Layer](#4-core-layer)
5. [Domain Layer](#5-domain-layer)
6. [Engine Layer](#6-engine-layer)
7. [Data Layer](#7-data-layer)
8. [Presentation Layer — MVI](#8-presentation-layer--mvi)
9. [Adaptive Layout Strategy](#9-adaptive-layout-strategy)
10. [Data Flow — End to End](#10-data-flow--end-to-end)
11. [Dependency Injection](#11-dependency-injection)
12. [State Management](#12-state-management)
13. [Concurrency Model](#13-concurrency-model)
14. [Decision Log](#14-decision-log)

---

## 1. Guiding Principles

| Principle | How it applies in Sparky                                   |
|-----------|------------------------------------------------------------|
| **SRP**   | One file = one responsibility. No `Manager` god-classes.   |
| **OCP**   | Adding a new component type requires only a new stamp class, no solver changes. |
| **LSP**   | All `CircuitComponent` subtypes are interchangeable wherever the sealed type is used. |
| **ISP**   | Repositories expose narrow interfaces; implementations are hidden from callers. |
| **DIP**   | ViewModels depend on use-case interfaces, not on Room or exporters directly. |
| **Immutability first** | All domain models are `data class`. State mutations return new objects. |
| **Unidirectional data flow** | UI emits `Intent` → ViewModel processes → new `State` → UI re-renders. |

---

## 2. Layer Model

```
┌────────────────────────────────────────────────────────────┐
│                       PRESENTATION                         │
│                                                            │
│   ┌─────────────┐   Intent   ┌──────────────┐              │
│   │   Screen    │ ─────────► │  ViewModel   │              │
│   │ (Composable)│ ◄───────── │  (Hilt VM)   │              │
│   └─────────────┘   State    └──────┬───────┘              │
│                                     │ suspends / collects  │
├─────────────────────────────────────┼─────────────────────-┤
│                       DOMAIN        │                      │
│                                     ▼                      │
│   ┌──────────────┐   invoke   ┌─────────────┐              │
│   │  Repository  │ ◄───────── │  Use Case   │              │
│   │  (interface) │            └──────┬──────┘              │
│   └──────────────┘                   │                     │
│         ▲                    ┌───────▼──────┐              │
│         │                    │ Domain Model │              │
│         │                    │ data class   │              │
├─────────┼────────────────────┼─────────────-┤              │
│  DATA   │         ENGINE     │              │              │
│         │                    │              │              │
│ ┌───────▼──────┐  ┌──────────▼─────┐       │               │
│ │RepositoryImpl│  │  CircuitSolver │       │               │
│ │ Room / JSON  │  │  (MNA + Gauss) │       │               │
│ │ Exporters    │  │  Stamp pattern │       │               │
│ └──────────────┘  └────────────────┘       │               │
└────────────────────────────────────────────────────────────┘
                    CORE (horizontal)
         AppResult · Constants · Extensions · DI modules
```

---

## 3. Dependency Rule

Arrows represent allowed `import` directions:

```
presentation → domain ← data
presentation → core
data         → core
domain       → core
engine       → core (math extensions only)
```

**Strictly forbidden:**
- `domain` imports from `data`, `engine`, or `presentation`
- `engine` imports Android classes
- `data` imports `presentation`

---

## 4. Core Layer

Located at `core/`. Contains shared infrastructure with no business logic.

### `core/result/AppResult.kt`

A sealed wrapper used across all layers to communicate outcomes without exceptions:

```
AppResult<T>
  ├── Success(data: T)
  ├── Error(exception: AppException)
  └── Loading
```

`AppException` subtypes: `DatabaseException`, `SimulationException`,
`ValidationException`, `ExportException`, `UnknownException`.

Extension functions `onSuccess`, `onError`, `onLoading`, `map` allow
chainable, declarative error handling in ViewModels.

### `core/di/`

Three Hilt modules, installed in `SingletonComponent`:

| Module             | Provides                                           |
|--------------------|----------------------------------------------------|
| `DatabaseModule`   | `SparkDatabase`, `CircuitDao`, `ProjectDao`        |
| `RepositoryModule` | Binds `CircuitRepositoryImpl → CircuitRepository`  |
| `EngineModule`     | `DcAnalyzer`, `CircuitValidator`                   |

### `core/constants/AppConstants.kt`

All magic numbers in one place, organized by domain:
`Canvas`, `Simulation`, `Export`, `Defaults`.

### `core/extensions/`

| File                    | Purpose                                    |
|-------------------------|--------------------------------------------|
| `FlowExtensions.kt`     | `asResult()`, `mapResult()` for Flow chains |
| `MathExtensions.kt`     | `isNearZero()`, `clamp()`, `toEngineeringString()` |
| `ComposeExtensions.kt`  | `toDp()`, `toPx()` for canvas pixel math   |

---

## 5. Domain Layer

Located at `domain/`. Pure Kotlin — zero Android imports.

### Models

All domain models are immutable `data class` objects.

```
Circuit
  ├── id: String (UUID)
  ├── name: String
  ├── components: List<CircuitComponent>  ← sealed class hierarchy
  ├── nodes: List<Node>
  └── wires: List<Wire>

CircuitComponent (sealed)
  ├── Resistor(resistanceOhm)
  ├── Capacitor(capacitanceFarad)
  ├── Inductor(inductanceHenry)
  ├── VoltageSource(voltageVolts)
  ├── CurrentSource(currentAmperes)
  ├── Diode(forwardVoltageVolts, saturationCurrentAmperes)
  └── Ground
```

`CircuitComponent` exposes `withPosition()`, `withRotation()`, `withLabel()`
as transformation helpers that return new instances — never mutate in place.

### Repository Interfaces

Interfaces only — never concrete classes:

```kotlin
interface CircuitRepository {
    fun observeAll(): Flow<List<Circuit>>
    suspend fun getById(id: String): Circuit?
    suspend fun save(circuit: Circuit)
    suspend fun delete(id: String)
    suspend fun duplicate(id: String): Circuit?
}
```

### Use Cases

**One class = one operation.** Named with a verb:

| Use Case                 | Input                      | Output                |
|--------------------------|----------------------------|-----------------------|
| `CreateCircuitUseCase`   | name, description          | `AppResult<Circuit>`  |
| `SaveCircuitUseCase`     | Circuit                    | `AppResult<Unit>`     |
| `LoadCircuitUseCase`     | id                         | `AppResult<Circuit>`  |
| `AddComponentUseCase`    | Circuit, CircuitComponent  | `AppResult<Circuit>`  |
| `RemoveComponentUseCase` | Circuit, componentId       | `AppResult<Circuit>`  |
| `ConnectNodesUseCase`    | Circuit, nodeId, nodeId    | `AppResult<Circuit>`  |
| `ValidateCircuitUseCase` | Circuit                    | `AppResult<Unit>`     |
| `RunSimulationUseCase`   | Circuit, SimulationConfig  | `AppResult<SimulationResult>` |
| `ExportToPdfUseCase`     | Circuit, File, config      | `AppResult<File>`     |
| `ExportToImageUseCase`   | Circuit, File, config      | `AppResult<File>`     |
| `ExportToSvgUseCase`     | Circuit, File, config      | `AppResult<File>`     |

Use cases are not `suspend` unless they need to call a repository.
Pure-transform use cases (`AddComponent`, `RemoveComponent`) are synchronous.

---

## 6. Engine Layer

Located at `engine/`. See [`ENGINE.md`](ENGINE.md) for the full mathematical reference.

### Stamp Pattern

The MNA system works through contribution stamps. Each component knows
how to add its own entries to the conductance matrix `G` and current vector `I`:

```
ComponentStamp (interface)
  └── stamp(G: Matrix, I: DoubleArray, nodeIndex, branchIndex)

Implementations:
  ResistorStamp      → adds conductance to node pairs
  VoltageSourceStamp → adds branch variable row (augmented MNA)
  CurrentSourceStamp → adds current directly to I vector
```

Adding a new component (e.g., `Transistor`) only requires:
1. A new `CircuitComponent` data class
2. A new `*Stamp` implementation
3. A `when` branch in `MnaMatrixBuilder`

The solver code (`GaussianElimination`, `DcAnalyzer`) is untouched.

### Solver Pipeline

```
Circuit → CircuitValidator → MnaMatrixBuilder → GaussianElimination → SolverResult
                                 builds G, I        solves [G][x]=[I]
                                                          ↓
                                               nodeVoltages (Map<nodeId, V>)
                                               branchCurrents (Map<compId, A>)
```

---

## 7. Data Layer

Located at `data/`. Implements domain interfaces using Android-specific APIs.

### Room Database

`SparkDatabase` (version 1) has two entities:

| Entity          | Maps to      | Serialization                    |
|-----------------|--------------|----------------------------------|
| `CircuitEntity` | `Circuit`    | Components/nodes/wires as JSON   |
| `ProjectEntity` | `Project`    | Circuit ID list as JSON          |

`CircuitMapper.kt` converts between entity and domain via extension functions:
`Circuit.toEntity()` / `CircuitEntity.toDomain()`.

`SerializableComponent` acts as a JSON-safe DTO with a type discriminator string,
avoiding sealed-class polymorphism complexity in serialization.

### Export Pipeline

Each exporter receives the domain `Circuit` and a format-specific config.
They output to a `File` — no Android `Uri` at this layer. The ViewModel
decides where the file lives (external storage, cache).

| Exporter        | API used                       |
|-----------------|--------------------------------|
| `PdfExporter`   | Android `PdfDocument` API      |
| `ImageExporter` | `Bitmap` + `Canvas`            |
| `SvgExporter`   | String builder (no library)    |

---

## 8. Presentation Layer — MVI

The presentation layer follows strict **MVI (Model-View-Intent)**:

```
User action (tap, drag, type)
      ↓
   Intent  (sealed class — what the user wants)
      ↓
  ViewModel  (processes intent, calls use case)
      ↓
   State  (immutable data class — snapshot of the screen)
      ↓
  Screen  (pure function of State — no logic)
```

### Contract per screen

Every screen has exactly three files:

| File           | Role                                              |
|----------------|---------------------------------------------------|
| `*State.kt`    | `data class` — everything the screen can display |
| `*Intent.kt`   | `sealed class` — every action the user can take  |
| `*ViewModel.kt`| Processes intents, exposes `StateFlow<*State>`    |
| `*Screen.kt`   | `@Composable` — renders State, emits Intent       |

The `Screen` composable never calls business logic. It only:
- Reads from `State`
- Calls `onIntent(...)` lambdas passed from the ViewModel

### Screens

| Screen      | Purpose                                         |
|-------------|-------------------------------------------------|
| `Projects`  | List all circuits, create / delete              |
| `Editor`    | Canvas-based circuit designer (adaptive layout) |
| `Simulation`| Run and display MNA results                     |
| `Export`    | Choose format and trigger file export           |

---

## 9. Adaptive Layout Strategy

`EditorScreen` reads `WindowWidthSizeClass` from `currentWindowAdaptiveInfo()`.

| Window class | Layout                                                       |
|--------------|--------------------------------------------------------------|
| `COMPACT`    | `PhoneLayout` — canvas full screen, palette as bottom sheet  |
| `MEDIUM`     | `TabletLayout` — component rail on the left                  |
| `EXPANDED`   | `TabletLayout` — rail left + property panel right            |

This decision lives exclusively in `EditorScreen.kt` — the ViewModel is
layout-agnostic and works identically across all window sizes.

---

## 10. Data Flow — End to End

**Adding a component to the canvas:**

```
1. User taps palette item (ComponentPalette)
2. EditorScreen emits EditorIntent.AddComponent(type, x, y)
3. EditorViewModel.onIntent() receives it
4. AddComponentUseCase(circuit, component) returns AppResult.Success(updatedCircuit)
5. ViewModel pushes previous circuit onto undoStack
6. ViewModel updates _state with new circuit
7. EditorScreen collects new State and re-renders CircuitCanvas
```

**Running a simulation:**

```
1. User taps "Run" (SimulationControls)
2. Navigates to SimulationScreen with circuitId
3. SimulationViewModel.init() calls LoadCircuitUseCase → RunSimulationUseCase
4. RunSimulationUseCase calls ValidateCircuitUseCase first
5. If valid: CircuitSolver.solve() → MnaMatrixBuilder.build() → GaussianElimination.solve()
6. Returns SimulationResult with nodeVoltages + branchCurrents
7. SimulationScreen renders ResultPanel with engineering-notation values
```

---

## 11. Dependency Injection

Hilt is the sole DI framework. All modules are `@InstallIn(SingletonComponent::class)`.

```
SingletonComponent
  ├── SparkDatabase       (Room, singleton)
  ├── CircuitDao          (from DB)
  ├── ProjectDao          (from DB)
  ├── CircuitRepository   (bound to CircuitRepositoryImpl)
  ├── DcAnalyzer          (singleton, stateless)
  └── CircuitValidator    (singleton, stateless)

ViewModelComponent (per ViewModel)
  └── All ViewModels via @HiltViewModel + @Inject constructor
```

Use cases use `@Inject constructor` with no Hilt annotation —
they are instantiated by Hilt automatically when injected into ViewModels.

---

## 12. State Management

`State` objects are immutable `data class`. Updates always use `.copy()`.

`EditorState.undoStack: List<Circuit>` acts as a pure undo mechanism:
- Before each mutation, the previous circuit is pushed onto the stack
- `UndoLast` intent pops the stack and restores the previous circuit
- Stack is capped at 20 entries to bound memory

`_state` is always a `MutableStateFlow` inside the ViewModel,
exposed as `StateFlow` to Screens. Never use `LiveData`.

---

## 13. Concurrency Model

| Layer       | Threading rule                                              |
|-------------|-------------------------------------------------------------|
| Domain      | Synchronous. No coroutines. Pure functions.                 |
| Engine      | Synchronous. Designed to be called from a background coroutine. |
| Data        | `suspend` functions in DAOs (Room dispatcher-aware)         |
| ViewModel   | `viewModelScope` for all coroutines. `Dispatchers.IO` for DB/file ops. |
| Screen      | Main thread only. Compose handles invalidation.             |

`CircuitSolver.solve()` is `suspend` and should be called inside
`viewModelScope.launch` so the heavy matrix math doesn't block the main thread.

---

## 14. Decision Log

### Why MVI instead of MVVM?

The circuit editor has complex, stateful interactions: multi-touch pan/zoom,
drag-and-drop, wire drawing, component selection, undo/redo. In MVVM,
individual observable properties spread across the ViewModel create
race conditions and hard-to-reason-about state combinations.

MVI centralizes the entire screen state in one `data class`, making
impossible states literally unrepresentable (e.g., `isLoading && circuit != null`
can be modeled away by design).

### Why Gaussian Elimination over existing libraries?

No Android-compatible, lightweight, license-clean linear algebra library
exists that is small enough for a mobile app. Gaussian elimination with
partial pivoting handles the matrix sizes typical in circuit simulation
(typically < 1000 nodes) with negligible overhead. The implementation
is ~60 lines and fully unit-testable.

### Why kotlinx.serialization for circuit persistence?

Circuit data (components, nodes, wires) must survive Room upgrades,
share as JSON, and be readable by export engines. `kotlinx.serialization`
handles all these without reflection, which is critical for R8/ProGuard
compatibility and performance on older devices.

### Why a single `CircuitComponent` sealed class instead of separate entity hierarchies?

Keeping all component types in one sealed class allows exhaustive `when`
expressions everywhere — particularly in `ComponentRenderer`, `MnaMatrixBuilder`,
and the mapper. This makes it impossible to add a new component type
without the compiler forcing all call sites to handle it.

### Why no Jetpack ViewModel saved-state handle for the full circuit?

Circuits can be several hundred KB in JSON. Saving them to the saved state
bundle (which has a ~500KB hard limit and is synchronous on the main thread)
would cause ANRs and TransactionTooLargeException. Instead, the circuit is
always loaded from Room using its stable `circuitId`, which is small and
safe to persist in `SavedStateHandle`.

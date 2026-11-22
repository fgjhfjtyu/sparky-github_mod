# Code Standards — Sparky

> These standards are enforced by code review. Every rule has a rationale.
> When in doubt: prefer clarity over cleverness.

---

## Table of Contents

1. [Language](#1-language)
2. [File Rules](#2-file-rules)
3. [Naming Conventions](#3-naming-conventions)
4. [Function Design](#4-function-design)
5. [Class Design](#5-class-design)
6. [SOLID in Practice](#6-solid-in-practice)
7. [Kotlin Idioms](#7-kotlin-idioms)
8. [Coroutines and Flow](#8-coroutines-and-flow)
9. [Compose Rules](#9-compose-rules)
10. [Error Handling](#10-error-handling)
11. [Testing Standards](#11-testing-standards)
12. [What Not to Do](#12-what-not-to-do)

---

## 1. Language

- All code, identifiers, comments, commit messages, and documentation are written in **English**.
- Conversations with the team may be in any language.
- KDoc is optional for public APIs but required for non-obvious behavior.

---

## 2. File Rules

| Rule                            | Limit / Guideline                               |
|---------------------------------|-------------------------------------------------|
| Lines per file                  | **200 max** — extract if exceeded              |
| Classes per file                | **1** public class per file (except sealed subclasses) |
| File name                       | Matches the primary class/object/interface name |
| Package structure               | Mirrors the layer + feature hierarchy           |

### File naming

```
ClassName.kt            — class, object, interface, sealed class
ClassNameTest.kt        — unit test for ClassName
```

Do not suffix files with `Impl` unless they are repository or exporter implementations.
Never name files `Utils`, `Helper`, `Manager`, or `Handler`.

---

## 3. Naming Conventions

### Classes and interfaces

```kotlin
class CircuitSolver          // noun — what it is
interface CircuitRepository  // noun — what it represents
sealed class AppResult       // noun — the result type
data class SimulationConfig  // noun — configuration record
```

### Use cases

```kotlin
class RunSimulationUseCase   // verb + noun + "UseCase" suffix
class AddComponentUseCase
class ExportToPdfUseCase
```

### ViewModels

```kotlin
class EditorViewModel        // screen name + "ViewModel"
```

### States and Intents

```kotlin
data class EditorState(...)  // screen name + "State"
sealed class EditorIntent    // screen name + "Intent"
```

### Functions

```kotlin
fun validateCircuit(...)     // camelCase verb
fun nodeById(id: String)     // camelCase, descriptive
fun stamp(G, I, ...)         // short only when domain-standard (MNA notation)
```

### Constants

```kotlin
object AppConstants {
    const val MAX_ZOOM = 4.0f     // SCREAMING_SNAKE_CASE for top-level constants
    object Canvas {
        const val GRID_SIZE_DP = 20f
    }
}
```

### Variables

```kotlin
val circuitId: String        // camelCase
var zoom: Float              // camelCase, `var` only when mutation is necessary
val _state = MutableStateFlow(...)  // leading underscore for mutable backing fields
```

### Packages

```
com.jackby03.sparky.core.result       // all lowercase, no underscores
com.jackby03.sparky.domain.usecase.circuit
```

---

## 4. Function Design

### Max length

**20 lines.** If a function exceeds this, extract private helper functions.

### Single responsibility

Each function does one thing. The name must describe exactly what that thing is.

```kotlin
// BAD — does two things
fun validateAndSave(circuit: Circuit): AppResult<Unit> { ... }

// GOOD — separate concerns
fun validate(circuit: Circuit): AppResult<Unit> { ... }
fun save(circuit: Circuit): AppResult<Unit> { ... }
```

### Parameters

Limit to **4 parameters**. If more are needed, group them in a data class.

```kotlin
// BAD
fun export(circuit: Circuit, file: File, width: Int, height: Int, quality: Int, dpi: Int)

// GOOD
fun export(circuit: Circuit, file: File, config: ImageExportConfig)
```

### Return types

Always declare explicit return types for public functions.
Never return `Any` or `Object`.

### Extension functions

Use extension functions to add behavior to existing types without subclassing.
Keep them in `core/extensions/` or adjacent to the type they extend.

```kotlin
// Good: keeps the call site readable
fun Double.toEngineeringString(): String = ...
fun Circuit.hasGround(): Boolean = ...
```

---

## 5. Class Design

### Data classes

All domain models are `data class`. They:
- Are immutable (all `val` properties)
- Never contain business logic
- Expose transformation helpers that return new instances

```kotlin
data class Circuit(
    val id: String,
    val name: String,
    val components: List<CircuitComponent>
) {
    // OK: pure query, returns existing data
    fun hasGround(): Boolean = nodes.any { it.isGround }

    // OK: pure transformation, returns a new instance
    fun withName(name: String): Circuit = copy(name = name)

    // NOT OK: business logic belongs in a use case
    // fun addAndValidate(component: CircuitComponent): Circuit { ... }
}
```

### Sealed classes

Use sealed classes for:
- Result types (`AppResult`, `SolverResult`)
- Error hierarchies (`AppException`, `ValidationError`)
- State variants (`when` must be exhaustive)
- Component hierarchies (`CircuitComponent`)

### Objects

`object` for stateless singletons (e.g., `GaussianElimination`, `MnaMatrixBuilder`).
Never use `object` for classes that have mutable state.

### Interfaces

Define interfaces in the **domain layer** for anything that crosses layer boundaries.
Implementations live in the **data** or **engine** layers.

---

## 6. SOLID in Practice

### Single Responsibility

- One class = one reason to change
- `MnaMatrixBuilder` builds matrices — it does not solve them
- `CircuitValidator` validates — it does not mutate the circuit

### Open / Closed

- `ComponentStamp` interface: adding a new component never modifies the solver
- `AppException`: adding a new exception type never modifies existing handlers

### Liskov Substitution

- Every `CircuitComponent` subtype is valid wherever a `CircuitComponent` is accepted
- `when(component)` must have an exhaustive branch for every subtype

### Interface Segregation

- `CircuitRepository` is narrow: observe, get, save, delete, duplicate
- DAOs are separate per entity (`CircuitDao`, `ProjectDao`) — not a single giant DAO

### Dependency Inversion

- `EditorViewModel` depends on `LoadCircuitUseCase`, not on `CircuitRepositoryImpl`
- `RunSimulationUseCase` depends on `CircuitSolver` (interface-like via injection), not on `DcAnalyzer` directly

---

## 7. Kotlin Idioms

### Prefer expression bodies for single-expression functions

```kotlin
// BAD
fun hasGround(): Boolean {
    return nodes.any { it.isGround }
}

// GOOD
fun hasGround(): Boolean = nodes.any { it.isGround }
```

### Use `when` exhaustively on sealed types

```kotlin
// BAD — falls through silently
if (component is Resistor) drawResistor(...)

// GOOD — compiler enforces exhaustiveness
when (component) {
    is Resistor      -> drawResistor(...)
    is Capacitor     -> drawCapacitor(...)
    // ... all branches required
}
```

### Avoid nullable types unless absence is meaningful

```kotlin
// BAD — what does null mean here?
val circuit: Circuit? = null

// GOOD — use Loading state to represent "not yet available"
data class EditorState(
    val circuit: Circuit? = null,
    val isLoading: Boolean = true
)
```

### Prefer `buildList`, `buildString`, `buildMap`

```kotlin
// BAD
val errors = mutableListOf<ValidationError>()
if (...) errors.add(...)
return errors

// GOOD
return buildList {
    if (...) add(ValidationError.NoGround)
}
```

### Use named arguments for data classes with 3+ parameters

```kotlin
// BAD — unclear what each value means
SimulationConfig(SimulationType.DC, DcConfig(100, 1e-9), AcConfig(), TransientConfig())

// GOOD
SimulationConfig(
    type = SimulationType.DC,
    dcConfig = DcConfig(maxIterations = 100, convergenceThreshold = 1e-9)
)
```

---

## 8. Coroutines and Flow

### ViewModel scope only

Coroutines are launched **only in ViewModels** (via `viewModelScope`) and
in Repository/DAO implementations (Room handles its own dispatcher).

Domain use cases and engine code are **synchronous**.

### Dispatchers

```kotlin
// DB operations: Room dispatches automatically
viewModelScope.launch {
    repository.save(circuit)  // no explicit dispatcher needed
}

// Heavy computation: use Default
viewModelScope.launch(Dispatchers.Default) {
    val result = solver.solve(circuit, config)
}

// File I/O: use IO
viewModelScope.launch(Dispatchers.IO) {
    exporter.export(circuit, file, config)
}
```

### Flow collection in Screen

Always use `collectAsStateWithLifecycle()` in Composables — never `collectAsState()`:

```kotlin
val state by viewModel.state.collectAsStateWithLifecycle()
```

### Never collect in init

Observation starts in `init` via `launchIn(viewModelScope)`, not by collecting inside `init`.

```kotlin
// BAD
init {
    viewModelScope.launch {
        repository.observeAll().collect { ... }
    }
}

// GOOD
init {
    repository.observeAll()
        .onEach { circuits -> ... }
        .launchIn(viewModelScope)
}
```

---

## 9. Compose Rules

### Screens are dumb

Composables only read `State` and call lambdas. Never access ViewModel directly
from a child composable — pass state and callbacks down.

```kotlin
// BAD — child composable calls ViewModel
@Composable
fun CircuitCard(viewModel: ProjectsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ...
}

// GOOD — state and callbacks passed in
@Composable
fun CircuitCard(circuit: Circuit, onOpen: () -> Unit, onDelete: () -> Unit) {
    ...
}
```

### Composable naming

`PascalCase` for composables that emit UI. `camelCase` for functions that return values.

```kotlin
@Composable fun ResultPanel(result: SimulationResult) { ... }   // UI — PascalCase
@Composable fun rememberZoomState(): ZoomState { ... }          // returns value — camelCase prefix
```

### Stable parameters

Pass only stable types to composables to avoid unnecessary recompositions.
Prefer `data class`, `List`, `String`, primitives over `Any` or mutable state.

### Keys in LazyColumn

Always provide a stable `key` in `LazyColumn.items`:

```kotlin
items(circuits, key = { it.id }) { circuit -> CircuitCard(circuit) }
```

---

## 10. Error Handling

### No raw exceptions in ViewModels

Always wrap suspending calls in try/catch and map to `AppResult.Error`:

```kotlin
// BAD
val circuit = repository.getById(id)!!

// GOOD
val result = try {
    AppResult.Success(repository.getById(id) ?: throw NotFoundException())
} catch (e: Exception) {
    AppResult.Error(AppException.DatabaseException(e.message ?: "Load failed", e))
}
```

### No silent failures

Every `AppResult.Error` must eventually surface to the user as a visible error message
or be explicitly ignored with a comment explaining why.

### No `!!` operator

The non-null assertion `!!` is banned outside of test code and `@AndroidEntryPoint`
injection sites. Use `?: return`, `?: error(...)`, or require with a descriptive message.

---

## 11. Testing Standards

### Unit test naming

```
MethodName_Condition_ExpectedResult
```

```kotlin
@Test
fun `solve_withNoGround_returnsFailure`() { ... }

@Test
fun `addComponent_toExistingCircuit_incrementsComponentCount`() { ... }
```

### Test structure — AAA

```kotlin
@Test
fun `gaussianElimination_withValidSystem_returnsCorrectSolution`() {
    // Arrange
    val A = Matrix(2, 2).apply { set(0,0,2.0); set(0,1,1.0); set(1,0,1.0); set(1,1,3.0) }
    val b = doubleArrayOf(5.0, 10.0)

    // Act
    val result = GaussianElimination.solve(A, b)

    // Assert
    assertNotNull(result)
    assertEquals(1.0, result!![0], 1e-9)
    assertEquals(3.0, result[1], 1e-9)
}
```

### What to test

| Layer       | What to test                                       |
|-------------|----------------------------------------------------|
| `engine`    | All solver and stamp logic — 100% coverage target  |
| `domain`    | All use cases — mock the repository interface      |
| `data`      | Mappers (entity ↔ domain), validator rules         |
| `presentation` | ViewModel state transitions (use fake use cases) |

### What not to test

- Composables: tested via screenshot tests in v2
- Room DAOs: tested via in-memory Room in integration tests
- Generated code: Room-generated DAOs, Hilt factories

---

## 12. What Not to Do

| Anti-pattern                     | Why banned                                               |
|----------------------------------|----------------------------------------------------------|
| God class (`CircuitManager`)     | Violates SRP; becomes untestable                         |
| `!!` non-null assertion           | Silent crash; use safe operators                         |
| Business logic in Composable     | Breaks MVI; makes logic untestable                       |
| Repository logic in ViewModel     | Bypasses domain layer; breaks DIP                        |
| `Thread.sleep()` in tests         | Non-deterministic; use `TestCoroutineDispatcher`         |
| Magic numbers inline              | Use `AppConstants` instead                               |
| Mutable state in domain models    | Immutable models prevent race conditions                 |
| `runBlocking` outside tests       | Blocks main thread; use `viewModelScope.launch`          |
| Catching `Throwable` silently     | Hides crashes; always log or propagate                   |
| Deeply nested lambdas (> 3 levels) | Extract to named functions for readability              |

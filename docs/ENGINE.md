# Engine — Modified Nodal Analysis

> The Sparky electromechanical simulation engine is pure Kotlin with zero Android dependencies.
> It is fully unit-testable and runs synchronously on whatever thread the caller provides.

## Table of Contents

1. [Overview](#1-overview)
2. [Modified Nodal Analysis — Theory](#2-modified-nodal-analysis--theory)
3. [Matrix Structure](#3-matrix-structure)
4. [Stamp Pattern](#4-stamp-pattern)
5. [Component Stamps Reference](#5-component-stamps-reference)
6. [Solver Pipeline](#6-solver-pipeline)
7. [Gaussian Elimination](#7-gaussian-elimination)
8. [Validation Rules](#8-validation-rules)
9. [Analysis Types](#9-analysis-types)
10. [Adding a New Component](#10-adding-a-new-component)
11. [Performance Notes](#11-performance-notes)
12. [Numerical Stability](#12-numerical-stability)

---

## 1. Overview

The engine resolves the node voltages and branch currents of a circuit by
building and solving a system of linear equations derived from Kirchhoff's
laws using Modified Nodal Analysis (MNA).

**Supported analyses (v1):**
- DC Operating Point

**Planned:**
- AC Frequency Sweep (v2)
- Transient (v2)
- DC Sweep (v2)

---

## 2. Modified Nodal Analysis — Theory

### Kirchhoff's Current Law (KCL)

At every node `k` (except ground):

```
Σ (currents leaving node k) = 0
```

For a resistor between nodes `i` and `j` with resistance `R`:

```
I_ij = (V_i - V_j) / R = G_ij * (V_i - V_j)   where G = 1/R (conductance)
```

### Nodal Conductance Matrix

For `n` non-ground nodes, the standard nodal admittance matrix `G` has:

```
G[k][k] += sum of conductances connected to node k   (diagonal)
G[i][j] -= conductance between nodes i and j          (off-diagonal)
```

This gives the linear system:

```
[G] * [V] = [I]
```

where `V` is the unknown node voltage vector and `I` is the known current injection vector.

### Why Modified (MNA)?

Standard nodal analysis cannot directly handle voltage sources because
current through a voltage source is unknown and a voltage source constrains
a voltage — it does not add a conductance.

MNA augments the matrix with one extra variable per voltage source (the branch current `J`):

```
┌─────┬─────┐ ┌───┐   ┌───┐
│  G  │  B  │ │ V │   │ I │
├─────┼─────┤ ├───┤ = ├───┤
│  C  │  D  │ │ J │   │ E │
└─────┴─────┘ └───┘   └───┘

G: nodal conductance sub-matrix
B: voltage source incidence (−1/+1 per source per node)
C: transpose of B
D: zeros (for ideal voltage sources)
I: current injections from current sources
E: voltage source values
V: unknown node voltages
J: unknown branch currents through voltage sources
```

---

## 3. Matrix Structure

`Matrix.kt` is a flat `DoubleArray` of size `rows * cols`, indexed as `data[row * cols + col]`.
This layout is cache-friendly for row-major access patterns (elimination sweeps rows sequentially).

Key operations:

| Operation     | Description                                           |
|---------------|-------------------------------------------------------|
| `get(r, c)`   | Read element                                          |
| `set(r, c, v)`| Write element                                         |
| `add(r, c, v)`| Accumulate (used by stamps)                           |
| `swapRows`    | In-place row swap for pivoting                        |
| `isDegenerate`| Returns true if any row is all-zero (singular matrix) |

---

## 4. Stamp Pattern

Every circuit component implements:

```kotlin
interface ComponentStamp {
    fun stamp(
        G: Matrix,
        I: DoubleArray,
        nodeIndex: Map<String, Int>,   // node ID → row in G
        branchIndex: Map<String, Int>  // component ID → augmented row
    )
}
```

`MnaMatrixBuilder` iterates all components, creates the appropriate stamp,
and calls `stamp(G, I, nodeIndex, branchIndex)`.

The solver never sees component types — it only sees the assembled matrix.

---

## 5. Component Stamps Reference

### Resistor — `ResistorStamp`

Between nodes `p` (positive) and `n` (negative) with conductance `G = 1/R`:

```
G[p][p] += G_val
G[n][n] += G_val
G[p][n] -= G_val
G[n][p] -= G_val
```

Ground node is skipped (not in `nodeIndex`).

### Voltage Source — `VoltageSourceStamp`

Augments the matrix with branch current variable `k = branchIndex[componentId]`.
Between nodes `p` (positive terminal) and `n` (negative terminal) with voltage `V_s`:

```
G[p][k] += 1      G[k][p] += 1
G[n][k] -= 1      G[k][n] -= 1
I[k]    += V_s
```

### Current Source — `CurrentSourceStamp`

From node `n` to node `p` with current `I_s`:

```
I[p] -= I_s   (current leaves positive node)
I[n] += I_s   (current enters negative node)
```

### Capacitor — DC stamp (open circuit)

In DC analysis, capacitors are open circuits. They contribute nothing to the matrix.
(In transient analysis they are replaced by a Norton equivalent using companion models.)

### Inductor — DC stamp (short circuit)

In DC analysis, inductors are short circuits — equivalent to a 0V voltage source.

---

## 6. Solver Pipeline

```
Circuit (domain model)
       │
       ▼
 CircuitValidator
       │ List<ValidationError> (empty if OK)
       ▼
 MnaMatrixBuilder.build(circuit)
       │
       ├── assigns nodeIndex: Map<nodeId, Int> (non-ground nodes only)
       ├── assigns branchIndex: Map<componentId, Int> (voltage sources)
       ├── creates Matrix G of size (n + v) × (n + v)
       │   where n = non-ground nodes, v = voltage sources
       ├── creates DoubleArray I of size (n + v)
       └── iterates components → stamp()
       │
       ▼
 MnaSystem(G, I, nodeIndex, branchIndex, groundNodeId)
       │
       ▼
 GaussianElimination.solve(G, I)
       │ returns DoubleArray x   (x = [V₁, V₂, ..., Vn, J₁, J₂, ...])
       ▼
 DcAnalyzer
       ├── nodeVoltages: map nodeId → x[nodeIndex[nodeId]]
       ├── branchCurrents: map componentId → x[branchIndex[componentId]]
       └── SolverResult.Success(nodeVoltages, branchCurrents, iterationCount=1)
```

---

## 7. Gaussian Elimination

`GaussianElimination` solves `[A][x] = [b]` in O(n³) time using:

1. **Augmented matrix** — appends `b` as the last column of `A`
2. **Partial pivoting** — at each step, finds the row with the largest absolute
   value in the current column and swaps it to the pivot position.
   This avoids division by near-zero and improves numerical stability.
3. **Forward elimination** — zeroes out entries below the diagonal
4. **Back substitution** — solves for unknowns from bottom to top

Returns `null` if the matrix is singular (no unique solution).

### Complexity

| Operation          | Complexity |
|--------------------|------------|
| Build MNA matrix   | O(c)       | c = number of components
| Gaussian elimination | O(n³)    | n = number of nodes + voltage sources
| Back substitution  | O(n²)      |

For typical student/hobbyist circuits (< 50 nodes) this is instantaneous.
For large circuits (500+ nodes), consider iterative solvers (future work).

---

## 8. Validation Rules

`CircuitValidator` checks the following before every simulation:

| Rule                    | Error                          |
|-------------------------|--------------------------------|
| At least one component  | `NoComponents`                 |
| At least one ground node | `NoGround`                    |
| All terminals connected  | `UnconnectedTerminal`          |
| Resistance > 0           | `InvalidValue`                 |
| No NaN voltage/current   | `InvalidValue`                 |

A floating node (connected to nothing) produces a singular matrix.
The validator catches this before the solver runs.

---

## 9. Analysis Types

### DC Operating Point (v1)

Solves the steady-state voltages assuming:
- Capacitors = open circuits
- Inductors = short circuits (0 V voltage sources)
- All sources at their nominal values

Result: one voltage per node, one current per voltage source.

### AC Analysis (v2)

Sweeps a frequency range and replaces:
- Capacitors with impedance `Zc = 1/(jωC)`
- Inductors with impedance `Zl = jωL`

The MNA matrix becomes complex-valued. The solver must handle `Complex<Double>`
entries. Result: magnitude and phase per frequency per node (Bode plot data).

### Transient Analysis (v2)

Discretizes time using the **Backward Euler** (BE) or **Trapezoidal** (TR) method.
At each time step:
- Capacitors: replaced by Norton equivalent `(C/h) * V_prev + I_cap_prev`
- Inductors: replaced by Norton equivalent `(h/L) * V_prev + I_ind_prev`

where `h` is the time step.

Each time step solves a new DC system. The solution is a time series of node voltages.

---

## 10. Adding a New Component

**Example: adding a `Transistor` (NPN BJT) in simplified Ebers-Moll model.**

### Step 1 — Domain model

```kotlin
// domain/model/component/CircuitComponent.kt
data class Transistor(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String = "Q1",
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val rotation: Float = 0f,
    override val terminalNodeIds: List<String> = listOf("", "", ""),  // C, B, E
    val betaDC: Double = 100.0,
    val saturationCurrentAmperes: Double = 1e-14
) : CircuitComponent()
```

### Step 2 — Stamp implementation

```kotlin
// engine/stamp/TransistorStamp.kt
class TransistorStamp(
    private val componentId: String,
    private val collectorNodeId: String,
    private val baseNodeId: String,
    private val emitterNodeId: String,
    private val betaDC: Double
) : ComponentStamp {
    override fun stamp(G: Matrix, I: DoubleArray, nodeIndex: Map<String, Int>, branchIndex: Map<String, Int>) {
        // Simplified small-signal model: current-controlled current source
        // Ic = β * Ib (from base to emitter)
        val gm = betaDC  // transconductance approximation
        val nc = nodeIndex[collectorNodeId]
        val nb = nodeIndex[baseNodeId]
        val ne = nodeIndex[emitterNodeId]
        // ... stamp the linearized model
    }
}
```

### Step 3 — Register in MnaMatrixBuilder

```kotlin
// engine/solver/MnaMatrixBuilder.kt
is Transistor -> TransistorStamp(
    componentId = component.id,
    collectorNodeId = component.terminalNodeIds[0],
    baseNodeId = component.terminalNodeIds[1],
    emitterNodeId = component.terminalNodeIds[2],
    betaDC = component.betaDC
)
```

### Step 4 — Handle in ComponentType enum and ComponentRenderer

Add `TRANSISTOR` to `ComponentType` and draw it in `ComponentRenderer`.

**The solver, validator, and all other files remain untouched.**

---

## 11. Performance Notes

- The `Matrix` class uses a flat `DoubleArray` (not `Array<DoubleArray>`) for
  CPU cache efficiency. Row sweeps hit contiguous memory.
- Stamps are stateless objects — no allocation inside the stamp loop.
- The entire MNA build + solve for a 20-component circuit runs in < 1 ms on
  a mid-range Android device.
- For circuits > 200 nodes, consider moving the solve call to `Dispatchers.Default`
  inside `viewModelScope.launch`.

---

## 12. Numerical Stability

| Issue                    | Mitigation                                          |
|--------------------------|-----------------------------------------------------|
| Near-zero pivots         | Partial pivoting in Gaussian elimination            |
| Singular matrix          | `isDegenerate()` pre-check before solve             |
| Floating-point tolerance | `isNearZero(threshold = 1e-12)` for near-zero tests |
| Very large conductances  | No special handling in v1 — use reasonable values   |
| GMIN stepping            | Planned for v2 (small conductance added to ground)  |

**GMIN stepping** (v2): Add a small conductance (e.g., 1e-12 S) from each node to ground
to prevent singular matrices in pathological topologies (e.g., floating sub-circuits).

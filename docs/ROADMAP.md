# Roadmap — Sparky Electromechanical Systems Simulator

> This document tracks the planned evolution of Sparky across three major versions.
> Items are ordered by priority within each milestone.
> Status legend: `✅ Done` · `🔄 In Progress` · `⬜ Planned` · `🔮 Future`

---

## Summary

| Version | Theme              | Target                              | Status        |
|---------|--------------------|-------------------------------------|---------------|
| **v1.0** | Foundation        | Usable circuit editor + DC simulation | ✅ Done        |
| **v1.1** | Polish            | UX refinement, performance          | ✅ Done        |
| **v2.0** | Full Analysis     | AC, Transient, oscilloscope view    | ⬜ Planned     |
| **v2.1** | CAD & Community   | DXF export, import, share           | ⬜ Planned     |
| **v3.0** | Platform          | Cloud, scripting, component library | 🔮 Future      |

---

## v1.0 — Foundation

> Goal: a working circuit editor that can design and simulate simple DC circuits,
> save them locally, and export to PDF and image.

### Architecture & Infrastructure

- ✅ Clean Architecture layer separation (core / domain / engine / data / presentation)
- ✅ MVI pattern with `State + Intent + ViewModel` per screen
- ✅ Hilt dependency injection wired up
- ✅ Room database with `CircuitEntity` and circuit JSON serialization
- ✅ `AppResult<T>` sealed class for cross-layer error propagation
- ✅ Adaptive layout strategy (phone / tablet via `WindowSizeClass`)

### Simulation Engine

- ✅ Modified Nodal Analysis (MNA) matrix builder
- ✅ Gaussian elimination with partial pivoting
- ✅ DC operating point analysis
- ✅ Component stamp pattern (`ComponentStamp` interface)
- ✅ Stamps: Resistor, Voltage Source, Current Source
- ✅ DC treatment: Capacitor (open), Inductor (short circuit)
- ✅ Circuit validator (ground check, terminal connections, value sanity)
- ✅ Ground-referenced node index assignment (production-ready)
- ✅ Basic Diode linearization (piecewise-linear model for DC)
- ✅ Capacitor stamp (open-circuit DC model)
- ✅ Inductor stamp (short-circuit / 0V voltage source DC model)

### Canvas Editor

- ✅ `CircuitCanvas` composable with pan and pinch-to-zoom
- ✅ `GridOverlay` with configurable grid size
- ✅ `ComponentRenderer` — draws all 7 component types
- ✅ `WireRenderer` — draws wires with waypoints
- ✅ `ComponentPalette` — side rail (tablet) + bottom sheet (phone)
- ✅ Component drag-and-drop from palette to canvas
- ✅ Tap to select component (`HitTester` with 32 px radius)
- ✅ Drag to reposition selected component
- ✅ Snap-to-grid on drop and drag (`SnapGrid`)
- ✅ Component rotation (90° steps via PropertyPanel Rotate button)
- ✅ Wire drawing mode (tap terminal node → tap terminal node)
- ✅ Wire routing: orthogonal auto-routing (Manhattan paths)
- ✅ Delete selected component (trash button in PropertyPanel)
- ✅ `PropertyPanel` wired to component selection (phone: bottom sheet, tablet: side panel)
- ✅ Value parser — string input → typed Double per component (`ValueParser`)
- ✅ Component label auto-numbering (R1, R2, C1, … via `AutoNumbering`)
- ✅ Terminal dots drawn on canvas for discoverability

### Simulation UI

- ✅ `SimulationScreen` with node voltage and branch current result list
- ✅ Engineering notation formatting (`1.234 kΩ`, `23.4 mA`)
- ✅ Overlay simulation results on canvas (node voltage badges via `drawVoltageLabel`)
- ✅ Color-coded wires by voltage level (cyan → orange neon gradient)
- ✅ Current arrow indicators on wires (direction + magnitude-scaled opacity)

### Screens & Navigation

- ✅ `ProjectsScreen` — circuit list, create, delete, empty state
- ✅ `EditorScreen` — adaptive phone/tablet layout (full implementation)
- ✅ `SimulationScreen` — results view
- ✅ `ExportScreen` — format selection + share sheet
- ✅ `AppNavGraph` with all routes
- ✅ Circuit rename in editor toolbar (dialog)
- ✅ Unsaved changes dialog on back navigation
- ✅ Empty state for Projects screen
- ✅ Loading skeleton for circuit list (shimmer animation)

### Export

- ✅ `CircuitRenderer` — shared drawing logic (used by PDF and image exporters)
- ✅ PDF: full schematic rendering with component symbols, labels, and title
- ✅ PNG/JPEG: full schematic rendering via `ImageExporter`
- ✅ SVG: scalable schematic with component paths, wires, and labels
- ✅ Share sheet after export (FileProvider + `ACTION_SEND`)
- ✅ File picker (Storage Access Framework) — `CreateDocument` launcher in `ExportScreen`; "Save to file…" button after export

### Theme & UX

- ✅ Dark theme: `#0A0A0A` background, neon green primary, cyan wire color
- ✅ Light theme defined
- ✅ Material 3 color scheme, typography, and shapes
- ✅ `SparkTheme` with `LocalCanvasColors` (terminal + simLabel colors added)
- ✅ Wire drawing ghost line with Manhattan routing preview
- ✅ Simulation results inline in canvas (voltage badges, colored wires)
- ✅ Undo/redo (20-step stack, toolbar button)
- ✅ System dark/light theme toggle in settings (`SettingsScreen` + `SettingsRepository`)
- ✅ Haptic feedback on component placement and wire connection
- ✅ Smooth zoom animation (spring-based with `animateFloatAsState`)

### Testing

- ✅ Unit tests: `GaussianElimination` (correctness + edge cases + singular matrix)
- ✅ Unit tests: `ResistorStamp` (two-node, node-to-ground, short-circuit)
- ✅ Unit tests: `DcAnalyzer` end-to-end (voltage divider, Kirchhoff verification)
- ✅ Unit tests: `CircuitValidator` (empty, no ground, valid circuit)
- ✅ Unit tests: `AddComponentUseCase` (auto-numbering, terminal nodes)
- ✅ Unit tests: `ConnectNodesUseCase` (connect, self-connect, duplicate)
- ✅ Unit tests: `ValueParser` (all SI suffixes, format, edge cases)
- ✅ Unit tests: `CircuitMapper` roundtrip (Circuit → Entity → Circuit) — 6 tests
- ✅ Unit tests: `EditorViewModel` state transitions — 12 tests (MockK + coroutines-test)

---

## v1.1 — Polish

> Goal: production-quality UX, performance, and stability.
> No new analysis types — purely refinement of v1.0.

### Performance

- ✅ Canvas: draw only visible components (viewport culling — `isVisible()` in render loop)
- ✅ Canvas: `drawLayer` caching via split into `StaticCircuitLayer` (wires, components, sim labels) and `DynamicCircuitLayer` (selection ring, ghost wire, guides) — static layer skips recomposition on selection/guide-only changes
- ✅ Simulation: ANR watchdog via `withTimeout(5 s)` — auto-cancel + user error message
- ✅ Room: `@Index(value = ["updatedAt"])` on `CircuitEntity`; DB v2 with `MIGRATION_1_2`
- ✅ Bitmap export: OOM-safe rendering — under 8 MP single-pass; above 8 MP records to `Picture` (display list) then scales down to fit pixel budget; JPEG uses `RGB_565` (2 bytes/px)

### UX Refinements

- ✅ Undo count badge in toolbar (Material 3 `BadgedBox`)
- ⬜ Multi-select: lasso selection on canvas
- ✅ Copy/paste selected components — `EditorIntent.CopyComponent/PasteComponent`; Ctrl+C/V keyboard shortcuts; paste offsets 32 px and auto-numbers the label
- ✅ Pinch-to-zoom with momentum (fling gesture) — `VelocityTracker` + `withFrameNanos` exponential decay fling on one-finger canvas pan; fling cancelled on next touch
- ✅ Component ghost preview while dragging from palette (`detectDragGesturesAfterLongPress` + floating overlay badge)
- ✅ Snap alignment guide lines during drag (dashed X/Y lines when component aligns within 8px of another)
- ✅ Wire highlight on hover (component connection preview) — selected component's connected wires drawn on `DynamicCircuitLayer` with thicker `wireActive` stroke
- ✅ Keyboard shortcuts (physical keyboard on tablet): Del, Ctrl+Z, Ctrl+S (`onPreviewKeyEvent` on focusable canvas Box)
- ✅ Two-finger pan lock (prevent accidental zoom during pan) — dead-zone threshold: zoom changes < 2% are clamped to 1.0 in `detectTransformGestures`
- ✅ Zoom-to-fit — `ZoomToFit` intent + `handleZoomToFit()` in ViewModel; button in ZoomControls + toolbar

### Editor Enhancements

- ✅ Component properties: resistance decade selector (1 Ω → 1 MΩ) — log-scale `Slider` in `PropertyPanel` for `Resistor`
- ✅ Label collision detection — component labels drawn on canvas for first time; greedy X+Y proximity algorithm computes per-component Y offsets to prevent overlap
- ✅ Circuit overview minimap — 120×80dp `Canvas` inset in bottom-left; dots for components, lines for wires, viewport rectangle; shown when circuit has ≥ 2 components
- ✅ Zoom-to-selection — `EditorIntent.ZoomToSelection` → `handleZoomToSelection()` in ViewModel; `FilterCenterFocus` icon appears in toolbar when a component is selected
- ✅ System dark/light theme toggle — `SettingsScreen` (SYSTEM/DARK/LIGHT radio buttons); `SettingsRepository` (SharedPreferences); `MainActivity` observes `ThemeMode` StateFlow

### Stability

- ⬜ Crash reporter integration (Firebase Crashlytics)
- ✅ ANR watchdog for simulation timeout (> 5 s → cancel + error message)
- ✅ Circuit file integrity check on load — `runCatching` around JSON deserialization in `CircuitRepositoryImpl`; corrupt circuits are logged and skipped without crashing
- ✅ Database migration test for Room version upgrades (`SparkDatabaseMigrationTest` — instrumented, validates MIGRATION_1_2)
- ✅ ProGuard rules validated — keep rules added for Room `@Entity`/`@Dao`/`@Database`, Hilt `@HiltViewModel`, kotlinx.serialization `@Serializable`

### Accessibility

- ✅ Content descriptions on all icon buttons (ZoomControls, toolbar actions)
- ⬜ Canvas: TalkBack-compatible mode (list-based component navigation)
- ✅ Minimum touch target 48dp enforced everywhere — `heightIn(min=48.dp)` added to `PaletteItem`; `IconButton` / `RadioButton` rows already compliant
- ✅ High contrast theme variant — `ThemeMode.HIGH_CONTRAST`; pure black #000 + bright yellow #FFFF00 primary; `HighContrastColorScheme` + `HighContrastCanvasColors`; selectable in Settings

---

## v2.0 — Full Analysis

> Goal: complete simulation suite — AC, Transient, and advanced visualization.

### Engine Additions

- ⬜ Complex arithmetic types for AC analysis (`ComplexDouble`)
- ⬜ AC frequency sweep solver (log scale, configurable points/decade)
- ⬜ Transient analysis — Backward Euler (BE) integration
- ⬜ Transient analysis — Trapezoidal (TR) method with LTE estimation
- ⬜ Capacitor stamp for transient (Norton companion model)
- ⬜ Inductor stamp for transient (Norton companion model)
- ⬜ GMIN stepping for convergence of pathological circuits
- ⬜ Newton-Raphson iteration for non-linear elements (Diode, BJT)
- ⬜ Diode stamp: Shockley equation with limiting
- ⬜ Simple MOSFET stamp (square-law model)

### New Component Types

- ⬜ `NpnTransistor` (Ebers-Moll simplified DC + small-signal AC)
- ⬜ `PnpTransistor`
- ⬜ `NMosfet` (square-law model)
- ⬜ `PMosfet`
- ⬜ `OpAmp` (ideal: infinite gain model for DC, finite gain for AC)
- ⬜ `Switch` (time-controlled, for transient)
- ⬜ `ACVoltageSource` (amplitude + frequency + phase)
- ⬜ `PulseSource` (for transient — rise, fall, period, duty)
- ⬜ `Potentiometer` (variable resistor with wiper position)

### Visualization

- ⬜ Oscilloscope view — time-domain waveform for transient results
- ⬜ Bode plot — magnitude and phase for AC results
- ⬜ Phasor diagram for AC steady-state
- ⬜ Node voltage heatmap overlay on canvas
- ⬜ Waveform export as CSV
- ⬜ Cursor measurements on oscilloscope (Δt, ΔV, frequency)

### Analysis Configuration

- ⬜ Analysis type picker in editor (DC / AC / Transient)
- ⬜ DC operating point with parameter sweep
- ⬜ AC config screen (start/stop frequency, points/decade)
- ⬜ Transient config screen (stop time, time step, initial conditions)
- ⬜ Probe placement — select which nodes to observe

---

## v2.1 — CAD & Community

> Goal: interoperability with professional tools and circuit sharing.

### Export

- ⬜ DXF export (AutoCAD R14 format — component outlines as polylines)
- ⬜ KiCad schematic export (`.kicad_sch`) — experimental
- ⬜ SPICE netlist export (`.cir`) — enables use with LTspice, ngspice
- ⬜ Schematic watermark / title block for PDF

### Import

- ⬜ SPICE netlist import — basic R, L, C, V, I parsing
- ⬜ JSON circuit import from file picker
- ⬜ QR code scan to load a shared circuit

### Sharing

- ⬜ Generate shareable link (encode circuit as base64 URL)
- ⬜ Share as image / PDF directly from export screen
- ⬜ Share as SPICE netlist

### Component Library

- ⬜ Built-in sub-circuit library (voltage divider, RC filter, H-bridge templates)
- ⬜ Save selection as reusable sub-circuit
- ⬜ Import sub-circuit from library into canvas

---

## v3.0 — Platform

> Goal: Sparky as a platform for circuit education and professional use.

### Cloud & Sync

- ⬜ Google account sign-in
- ⬜ Cloud backup of circuits (Google Drive or custom backend)
- ⬜ Multi-device sync with conflict resolution
- ⬜ Circuit version history (last 10 saves)

### Scripting Engine

- ⬜ Embedded Kotlin/JS scripting for parameterized circuits
- ⬜ Script editor with syntax highlighting
- ⬜ Script-driven parameter sweeps and Monte Carlo analysis
- ⬜ Script output visualization

### Collaboration

- ⬜ Read-only circuit sharing via link
- ⬜ Comment annotations on schematic
- ⬜ Revision comparison (diff view between two saved states)

### Educational Mode

- ⬜ Step-by-step guided circuit tutorials
- ⬜ Quiz mode — annotate unknowns, check answers against simulation
- ⬜ Formula display alongside simulation results (Ohm's law, KCL, KVL)
- ⬜ Voltage/current animated flow visualization (directional particles)

### Platform

- ⬜ Android tablet optimized for stylus input (palm rejection, pressure)
- ⬜ ChromeOS windowed mode support
- ⬜ Wear OS companion: view simulation status on watch
- ⬜ Widgets: show last simulation result on home screen

---

## Technical Debt Register

Items that need attention before or during v2.0:

| Item                                        | Priority | Notes                                  |
|---------------------------------------------|----------|----------------------------------------|
| ~~`ExportViewModel.handleUpdateValue()` stub~~  | ~~High~~ | ✅ Fixed — `ValueParser` implemented   |
| ~~`PhoneLayout` bottom sheet for palette~~      | ~~High~~ | ✅ Fixed — `BottomSheetScaffold` implemented |
| ~~`CircuitRenderer` shared draw logic~~         | ~~High~~ | ✅ Fixed — `CircuitRenderer` object    |
| ~~`ProjectRepository` not implemented~~         | ~~Medium~~ | ✅ Fixed — `ProjectRepositoryImpl` complete |
| Node connection validation (floating nodes) | Medium   | Validator checks terminals but not topology |
| Serialization of `UUID.randomUUID()` in `@Serializable` models | Medium | UUID default is not stable across serialization |
| ~~`SparkDatabase` schema export path~~      | ~~Low~~  | ✅ Fixed — `room.schemaLocation` in `ksp {}` block; schemas as androidTest assets |
| ~~File picker (SAF) for export target~~     | ~~Low~~  | ✅ Fixed — `CreateDocument` launcher + `saveToUri` in `ExportViewModel`; "Save to file…" button shown after export |

---

## Completed Milestones

### v1.0 — Foundation (shipped 2026-03-22)

Full circuit editor with DC simulation, export, and unit test coverage.
All 7 component types; MNA solver; Room persistence; PDF/PNG/SVG export;
adaptive phone/tablet layout; shimmer loading skeleton; spring zoom animation;
haptic feedback; current arrow overlays; 19 passing unit tests.

---

## How to Update This Document

When a feature is completed:
1. Change its bullet from `⬜ Planned` to `✅ Done`
2. If it was a milestone's last item, move the entire version to **Completed Milestones**
3. Include the completion in the PR description and commit message

When a new planned item is discovered:
1. Add it to the appropriate version section with `⬜ Planned`
2. Add it to **Technical Debt Register** if it is a known deficiency
3. Discuss scope impact in the PR if it affects the current milestone

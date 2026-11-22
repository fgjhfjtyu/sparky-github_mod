package com.jackby03.sparky.presentation

import androidx.lifecycle.SavedStateHandle
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.component.ComponentType
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.usecase.circuit.AddComponentUseCase
import com.jackby03.sparky.domain.usecase.circuit.ConnectNodesUseCase
import com.jackby03.sparky.domain.usecase.circuit.LoadCircuitUseCase
import com.jackby03.sparky.domain.usecase.circuit.RemoveComponentUseCase
import com.jackby03.sparky.domain.usecase.circuit.SaveCircuitUseCase
import com.jackby03.sparky.domain.usecase.simulation.RunSimulationUseCase
import com.jackby03.sparky.presentation.screen.editor.EditorIntent
import com.jackby03.sparky.presentation.screen.editor.EditorViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var loadCircuit:   LoadCircuitUseCase
    private lateinit var saveCircuit:   SaveCircuitUseCase
    private lateinit var addComponent:  AddComponentUseCase
    private lateinit var removeComponent: RemoveComponentUseCase
    private lateinit var connectNodes:  ConnectNodesUseCase
    private lateinit var runSimulation: RunSimulationUseCase

    private val testCircuit = Circuit(id = "c1", name = "Test Circuit")

    private fun buildViewModel(): EditorViewModel {
        val handle = SavedStateHandle(mapOf("circuitId" to "c1"))
        return EditorViewModel(handle, loadCircuit, saveCircuit, addComponent, removeComponent, connectNodes, runSimulation)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        loadCircuit    = mockk()
        saveCircuit    = mockk()
        addComponent   = AddComponentUseCase()        // real
        removeComponent = RemoveComponentUseCase()    // real
        connectNodes   = ConnectNodesUseCase()        // real
        runSimulation  = mockk()

        coEvery { loadCircuit(any()) } returns AppResult.Success(testCircuit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        val vm = buildViewModel()
        assertTrue(vm.state.value.isLoading)
    }

    @Test
    fun `circuit loads on init`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertEquals("Test Circuit", vm.state.value.circuit?.name)
    }

    @Test
    fun `AddComponent sets isDirty`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.AddComponent(ComponentType.RESISTOR, 100f, 100f))

        assertTrue(vm.state.value.isDirty)
    }

    @Test
    fun `AddComponent auto-numbers label R1`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.AddComponent(ComponentType.RESISTOR, 100f, 100f))

        val label = vm.state.value.circuit?.components?.first()?.label
        assertEquals("R1", label)
    }

    @Test
    fun `SelectComponent sets selectedComponent`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.AddComponent(ComponentType.RESISTOR, 100f, 100f))
        val id = vm.state.value.circuit?.components?.first()?.id ?: ""

        vm.onIntent(EditorIntent.SelectComponent(id))

        assertNotNull(vm.state.value.selectedComponent)
        assertEquals(id, vm.state.value.selectedComponent?.id)
    }

    @Test
    fun `SelectComponent null clears selection`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.AddComponent(ComponentType.RESISTOR, 100f, 100f))
        val id = vm.state.value.circuit?.components?.first()?.id ?: ""
        vm.onIntent(EditorIntent.SelectComponent(id))
        vm.onIntent(EditorIntent.SelectComponent(null))

        assertNull(vm.state.value.selectedComponent)
    }

    @Test
    fun `RemoveComponent removes it and clears selection`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.AddComponent(ComponentType.RESISTOR, 100f, 100f))
        val id = vm.state.value.circuit?.components?.first()?.id ?: ""
        vm.onIntent(EditorIntent.SelectComponent(id))
        vm.onIntent(EditorIntent.RemoveComponent(id))

        assertEquals(0, vm.state.value.circuit?.components?.size)
        assertNull(vm.state.value.selectedComponent)
    }

    @Test
    fun `UndoLast restores previous circuit`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.AddComponent(ComponentType.RESISTOR, 100f, 100f))
        assertEquals(1, vm.state.value.circuit?.components?.size)

        vm.onIntent(EditorIntent.UndoLast)
        assertEquals(0, vm.state.value.circuit?.components?.size)
    }

    @Test
    fun `RotateComponent advances rotation by 90 degrees`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.AddComponent(ComponentType.RESISTOR, 100f, 100f))
        val id = vm.state.value.circuit?.components?.first()?.id ?: ""

        vm.onIntent(EditorIntent.RotateComponent(id))

        val rotation = vm.state.value.circuit?.components?.first()?.rotation
        assertEquals(90f, rotation ?: 0f, 0f)
    }

    @Test
    fun `RequestBack with dirty circuit shows unsaved dialog`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.AddComponent(ComponentType.RESISTOR, 100f, 100f))
        vm.onIntent(EditorIntent.RequestBack)

        assertTrue(vm.state.value.showUnsavedDialog)
    }

    @Test
    fun `RequestBack with clean circuit does not show dialog`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.RequestBack)

        assertFalse(vm.state.value.showUnsavedDialog)
    }

    @Test
    fun `RenameCircuit updates circuit name`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.RenameCircuit("New Name"))

        assertEquals("New Name", vm.state.value.circuit?.name)
    }

    @Test
    fun `UpdateValue with valid input parses and applies`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onIntent(EditorIntent.AddComponent(ComponentType.RESISTOR, 100f, 100f))
        val id = vm.state.value.circuit?.components?.first()?.id ?: ""

        vm.onIntent(EditorIntent.UpdateValue(id, "4k7"))

        // "4k7" is not standard SI notation - falls back to null → no update
        // "4.7k" is the correct form
        vm.onIntent(EditorIntent.UpdateValue(id, "4.7k"))
        val resistance = (vm.state.value.circuit?.components?.first() as? Resistor)?.resistanceOhm
        assertEquals(4_700.0, resistance ?: 0.0, 1e-6)
    }
}

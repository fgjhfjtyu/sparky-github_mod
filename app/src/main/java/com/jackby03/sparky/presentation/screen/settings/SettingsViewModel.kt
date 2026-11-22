package com.jackby03.sparky.presentation.screen.settings

import androidx.lifecycle.ViewModel
import com.jackby03.sparky.data.repository.SettingsRepository
import com.jackby03.sparky.domain.model.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settings.themeMode

    fun setThemeMode(mode: ThemeMode) = settings.setThemeMode(mode)
}

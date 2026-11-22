package com.jackby03.sparky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jackby03.sparky.domain.model.settings.ThemeMode
import com.jackby03.sparky.presentation.navigation.AppNavGraph
import com.jackby03.sparky.presentation.screen.settings.SettingsViewModel
import com.jackby03.sparky.presentation.theme.SparkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val useDark = when (themeMode) {
                ThemeMode.DARK         -> true
                ThemeMode.LIGHT        -> false
                ThemeMode.HIGH_CONTRAST -> true   // always dark base for high contrast
                ThemeMode.SYSTEM       -> isSystemDark
            }
            SparkTheme(darkTheme = useDark, themeMode = themeMode) {
                AppNavGraph()
            }
        }
    }
}

package com.havos.lubricerp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.havos.lubricerp.core.common.ThemeMode
import com.havos.lubricerp.core.database.SecureSessionStore
import com.havos.lubricerp.core.ui.theme.GoalErpTheme
import com.havos.lubricerp.feature_reports.presentation.navigation.GoalErpNavGraph
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val secureSessionStore: SecureSessionStore by inject()
    private var themeMode: ThemeMode by mutableStateOf(ThemeMode.SYSTEM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                secureSessionStore.themeModeFlow.collect { selectedMode ->
                    themeMode = selectedMode
                }
            }
        }

        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            GoalErpTheme(darkTheme = darkTheme) {
                ThemeSwitchFadeContainer(isDark = darkTheme) {
                    GoalErpNavGraph(modifier = Modifier)
                }
            }
        }
    }
}

@Composable
private fun ThemeSwitchFadeContainer(
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    val overlayAlpha = remember { Animatable(0f) }
    val overlayColor = if (isDark) Color.Black else Color.White

    LaunchedEffect(isDark) {
        overlayAlpha.snapTo(0f)
        overlayAlpha.animateTo(
            targetValue = 0.12f,
            animationSpec = tween(durationMillis = 110)
        )
        overlayAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (overlayAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor.copy(alpha = overlayAlpha.value))
            )
        }
    }
}

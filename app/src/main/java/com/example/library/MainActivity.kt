package com.example.library

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.library.ui.screens.AppTheme
import com.example.library.ui.screens.LibraryScreen
import com.example.library.ui.theme.MyLibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            var currentTheme by remember {
                mutableStateOf(if (systemDarkTheme) AppTheme.DARK else AppTheme.LIGHT)
            }
            val view = LocalView.current
            
            MyLibraryTheme(darkTheme = currentTheme == AppTheme.DARK) {
                DisposableEffect(currentTheme) {
                    if (!view.isInEditMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val controller = WindowCompat.getInsetsController(window, view)
                        val isDark = currentTheme == AppTheme.DARK
                        controller.isAppearanceLightNavigationBars = !isDark
                    }
                    onDispose { }
                }
                
                LibraryScreen(
                    modifier = Modifier.fillMaxSize(),
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme -> currentTheme = newTheme }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    MyLibraryTheme {
        LibraryScreen(
            currentTheme = AppTheme.DARK,
            onThemeChange = {}
        )
    }
}
package com.wavely.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavely.player.ui.AppViewModel
import com.wavely.player.ui.MainScreen
import com.wavely.player.ui.theme.WavelyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val themeState by appViewModel.themeState.collectAsStateWithLifecycle()
            WavelyTheme(themeMode = themeState.themeMode, dynamicColor = themeState.dynamicColor) {
                MainScreen()
            }
        }
    }
}

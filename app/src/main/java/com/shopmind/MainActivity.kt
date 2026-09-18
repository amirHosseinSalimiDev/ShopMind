package com.shopmind

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopmind.screen.App
import com.shopmind.ui.theme.ShopMindTheme
import com.shopmind.ui.theme.indigo600
import com.shopmind.viewmodel.MainViewModel
import com.shopmind.viewmodel.StartupState
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val startupViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            startupViewModel.state.value is StartupState.Loading
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ShopMindTheme {

                val startupState by startupViewModel.state.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    when (val state = startupState) {
                        StartupState.Loading -> {
                            // This shouldn't normally be visible because
                            // SplashScreen is keeping the launch screen up.
                        }

                        else -> {
                            App(
                                paddingValues = innerPadding,
                                startupState = state
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShopMindTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->

            App(
                paddingValues = innerPadding,
                startupState = StartupState.Unauthenticated
            )
        }
    }
}
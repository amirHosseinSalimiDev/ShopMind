package com.shopmind.screen.authentication

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shopmind.navigation.routes.AuthenticationRoutes
import com.shopmind.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthNavHost(
    paddingValues: PaddingValues
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        authViewModel.authResult.collect { result ->
            result.onFailure { error ->
                snackbarHostState.showSnackbar(error.message ?: "Authentication failed")
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AuthenticationRoutes.Login,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            composable<AuthenticationRoutes.Login> {
                LoginScreen(
                    paddingValues = innerPadding,
                    onSignUpClick = {
                        navController.navigate(AuthenticationRoutes.Signup)
                    },
                    onLogin = { phoneNumber, password ->
                        authViewModel.login(phoneNumber, password)
                    }
                )
            }

            composable<AuthenticationRoutes.Signup> {
                SignupScreen(
                    paddingValues = innerPadding,
                    onLoginClick = {
                        navController.popBackStack()
                    },
                    onCreateAccount = { username, phoneNumber, password ->
                        authViewModel.signup(username, phoneNumber, password)
                    }
                )
            }
        }
    }
}

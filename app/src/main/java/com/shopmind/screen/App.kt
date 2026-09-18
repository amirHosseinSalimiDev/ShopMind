package com.shopmind.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shopmind.navigation.routes.AppRoutes
import com.shopmind.screen.authentication.AuthNavHost
import com.shopmind.viewmodel.StartupState


@Composable
fun App(
    paddingValues: PaddingValues,
    startupState: StartupState
) {
    val navController = rememberNavController()

    val startDestination = when (startupState) {
        StartupState.Authenticated -> AppRoutes.Main
        StartupState.Unauthenticated -> AppRoutes.Authentication
        StartupState.Loading -> return
    }

    NavHost(
        modifier = Modifier.padding(paddingValues),
        navController = navController,
        startDestination = startDestination,
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
        composable<AppRoutes.Main> {
            MainScreen(paddingValues)
        }

        composable<AppRoutes.Authentication> {
            AuthNavHost(paddingValues)
        }
    }
}
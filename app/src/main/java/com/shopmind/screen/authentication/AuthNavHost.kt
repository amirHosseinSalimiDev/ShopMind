package com.shopmind.screen.authentication

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shopmind.navigation.routes.AuthenticationRoutes

@Composable
fun AuthNavHost(
    paddingValues: PaddingValues
) {
    val navController = rememberNavController()

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
                paddingValues = paddingValues,
                onSignUpClick = {
                    navController.navigate(AuthenticationRoutes.Signup)
                }
            )
        }

        composable<AuthenticationRoutes.Signup> {
            SignupScreen(
                paddingValues = paddingValues,
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
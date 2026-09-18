package com.shopmind.screen.authentication

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
        startDestination = AuthenticationRoutes.Login
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
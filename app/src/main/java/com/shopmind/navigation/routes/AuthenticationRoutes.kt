package com.shopmind.navigation.routes

import kotlinx.serialization.Serializable

@Serializable
sealed class AuthenticationRoutes {

    @Serializable
    data object Login : AuthenticationRoutes()

    @Serializable
    data object Signup : AuthenticationRoutes()

}
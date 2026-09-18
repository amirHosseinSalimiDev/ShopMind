package com.shopmind.navigation.routes

import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoutes {

    @Serializable
    data object Main : AppRoutes()

    @Serializable
    data object Authentication : AppRoutes()

}
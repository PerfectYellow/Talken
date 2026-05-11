// commonMain/kotlin/com/example/cyloop/nav/Navigation.kt
package com.example.cyloop.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object Welcome : Route()

    @Serializable
    data object TabView : Route()

    @Serializable
    data object PasscodeLock : Route()

    // Add more screens as you grow
    @Serializable
    data class Detail(val id: String) : Route()
}

// Route extensions for cleaner navigation
fun Route.toNavString(): String = when (this) {
    is Route.Welcome -> "welcome"
    is Route.TabView -> "tab_view"
    is Route.PasscodeLock -> "passcode_lock"
    is Route.Detail -> "detail/${id}"
}

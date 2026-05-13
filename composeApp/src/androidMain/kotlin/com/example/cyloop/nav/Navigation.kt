// commonMain/kotlin/com/example/cyloop/nav/Navigation.kt
package com.example.cyloop.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object Welcome : Route()

    @Serializable
    data class TabView(val tab: String = "profile") : Route()

    @Serializable
    data object PasscodeLock : Route()

    @Serializable
    data class ChatDetail(val chatId: String, val chatName: String) : Route()

    @Serializable
    data object NewChat : Route()

    @Serializable
    data class Detail(val id: String) : Route()
}

fun Route.toNavString(): String = when (this) {
    is Route.Welcome -> "welcome"
    is Route.TabView -> "tab_view/${tab}"
    is Route.PasscodeLock -> "passcode_lock"
    is Route.ChatDetail -> "chat_detail/${chatId}/${chatName}"
    is Route.NewChat -> "new_chat"
    is Route.Detail -> "detail/${id}"
}

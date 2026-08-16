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
    data class ChatDetail(val chatId: String, val chatName: String, val imageUrl: String? = null, val nftAddress: String? = null) : Route()

    @Serializable
    data class UserInfo(val chatName: String, val walletAddress: String? = null, val imageUrl: String? = null, val nftAddress: String? = null) : Route()

    @Serializable
    data object NewChat : Route()

    @Serializable
    data object Detail : Route()

    @Serializable
    data class WalletDetail(val successMessage: String? = null) : Route()

    @Serializable
    data class Deposit(val walletAddress: String) : Route()

    @Serializable
    data class Send(val fromAddress: String) : Route()

    @Serializable
    data object Payment : Route()

    @Serializable
    data object NewTransactionRoute : Route()

    @Serializable
    data object BillMaker : Route()

    @Serializable
    data object UploadContent : Route()
}

fun Route.toNavString(): String = when (this) {
    is Route.Welcome -> "welcome"
    is Route.TabView -> "tab_view/${tab}"
    is Route.PasscodeLock -> "passcode_lock"
    is Route.ChatDetail -> "chat_detail/${chatId}/${chatName}?img=${imageUrl ?: ""}&nft=${nftAddress ?: ""}"
    is Route.UserInfo -> "user_info/${chatName}?addr=${walletAddress ?: ""}&img=${imageUrl ?: ""}&nft=${nftAddress ?: ""}"
    is Route.NewChat -> "new_chat"
    is Route.Detail -> "detail"
    is Route.WalletDetail -> "wallet_detail?msg=${successMessage ?: ""}"
    is Route.Deposit -> "deposit/${walletAddress}"
    is Route.Send -> "send/${fromAddress}"
    is Route.Payment -> "payment"
    is Route.NewTransactionRoute -> "new_transaction"
    is Route.BillMaker -> "bill_maker"
    is Route.UploadContent -> "upload_content"
}

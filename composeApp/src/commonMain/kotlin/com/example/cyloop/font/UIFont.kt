package com.example.cyloop.font

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object UIFont {
    // Telegram uses system fonts: Roboto on Android, San Francisco on iOS.
    // This provides the most "native" and authentic Telegram feel.
    private val TelegramFont = FontFamily.Default

    // Top Bar / Large Titles
    val LargeTitle = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )

    // Chat List Name
    val ChatName = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold
    )

    // Chat List Last Message / Preview
    val ChatMessage = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )

    // Secondary Info (Time, Unread Count, etc.)
    val Metadata = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal
    )

    // Avatar Initials
    val AvatarLabel = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium
    )

    // Small Labels / Badges
    val Badge = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )

    // Regular Body Text
    val Body = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Default
    )

    // Section Headers (Settings, etc.)
    val SectionHeader = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Default
    )
}

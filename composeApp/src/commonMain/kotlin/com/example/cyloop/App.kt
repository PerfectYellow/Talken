package com.example.cyloop

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.theme.CyLoopTheme
import com.example.cyloop.nav.rememberNavController
import com.example.cyloop.nav.NavHost

@Composable
@Preview
fun App() {
    CyLoopTheme(darkTheme = true) {
        val navController = rememberNavController()
        NavHost(navController = navController)
    }
}

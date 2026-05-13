// commonMain/kotlin/com/example/cyloop/nav/NavController.kt
package com.example.cyloop.nav

import androidx.compose.runtime.*

class NavController(
    initialRoute: Route
) {
    private val _backStack = mutableStateListOf(initialRoute)
    
    private var _currentRoute by mutableStateOf(initialRoute)
    val currentRoute: Route get() = _currentRoute

    fun navigate(route: Route, popUpTo: Route? = null, inclusive: Boolean = false) {
        popUpTo?.let { target ->
            val index = _backStack.indexOfLast { it::class == target::class }
            if (index != -1) {
                val popCount = if (inclusive) _backStack.size - index
                else _backStack.size - index - 1
                repeat(popCount) { 
                    if (_backStack.isNotEmpty()) _backStack.removeAt(_backStack.size - 1) 
                }
            }
        }
        _backStack.add(route)
        _currentRoute = route
    }

    fun replaceCurrent(route: Route) {
        if (_backStack.isNotEmpty()) {
            _backStack[_backStack.size - 1] = route
            _currentRoute = route
        }
    }

    fun navigateAndClearStack(route: Route) {
        _backStack.clear()
        _backStack.add(route)
        _currentRoute = route
    }

    fun goBack(): Boolean {
        if (_backStack.size <= 1) return false
        _backStack.removeAt(_backStack.size - 1)
        _currentRoute = _backStack.last()
        return true
    }

    fun isBackStackEmpty(): Boolean = _backStack.isEmpty()
}

@Composable
fun rememberNavController(initialRoute: Route = Route.Welcome): NavController {
    return remember { NavController(initialRoute) }
}

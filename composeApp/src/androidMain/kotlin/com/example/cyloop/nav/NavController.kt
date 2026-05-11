// commonMain/kotlin/com/example/cyloop/nav/NavController.kt
package com.example.cyloop.nav

import androidx.compose.runtime.*

class NavController(
    initialRoute: Route
) {
    private val _backStack = mutableStateListOf(initialRoute)
    
    // Explicit state for current route to ensure recomposition
    private var _currentRoute by mutableStateOf(initialRoute)
    val currentRoute: Route get() = _currentRoute

    fun navigate(route: Route, popUpTo: Route? = null, inclusive: Boolean = false) {
        popUpTo?.let { target ->
            val index = _backStack.indexOfLast { it == target }
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

    fun popUpTo(route: Route, inclusive: Boolean = false) {
        while (_backStack.isNotEmpty() && _backStack.last() != route) {
            _backStack.removeAt(_backStack.size - 1)
        }
        if (inclusive && _backStack.isNotEmpty()) {
            _backStack.removeAt(_backStack.size - 1)
        }
        _backStack.lastOrNull()?.let { _currentRoute = it }
    }
    
    fun isBackStackEmpty(): Boolean = _backStack.isEmpty()
}

@Composable
fun rememberNavController(initialRoute: Route = Route.Welcome): NavController {
    return remember { NavController(initialRoute) }
}

package com.project.vaultly.core.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

val LocalBackStack = compositionLocalOf<NavBackStack<NavKey>> {
    error("LocalBackStack not provided. Make sure to provide it in ComposeApp")
}

fun NavBackStack<NavKey>.navigateTo(route: NavKey) {
    add(route)
}

fun NavBackStack<NavKey>.navigateBack() {
    if (size > 1) {
        removeLastOrNull()
    }
}

fun NavBackStack<NavKey>.navigateAndClear(route: NavKey) {
    clear()
    add(route)
}

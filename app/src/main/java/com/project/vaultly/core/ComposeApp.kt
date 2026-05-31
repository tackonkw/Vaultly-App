package com.project.vaultly.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.project.vaultly.core.navigation.LocalBackStack
import com.project.vaultly.core.navigation.Routes
import com.project.vaultly.core.navigation.navigateBack
import com.project.vaultly.core.navigation.navigateTo
import com.project.vaultly.theme.VaultlyTheme
import com.project.vaultly.feature.auth.data.UserRepository

@Composable
fun ComposeApp() {
    val currentUserEmail = UserRepository.getCurrentUserEmail()
    val initialRoute: Routes = if (currentUserEmail != null && currentUserEmail.isNotEmpty()) {
        Routes.HomeRoute(email = currentUserEmail)
    } else {
        Routes.SplashRoute
    }

    val backStack = rememberNavBackStack(initialRoute)

    CompositionLocalProvider(LocalBackStack provides backStack) {
        VaultlyTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavDisplay(
                    backStack = backStack,
                    entryProvider = entryProvider {
                        entry<Routes.SplashRoute> {
                            com.project.vaultly.feature.splash.SplashScreen {
                                backStack.add(Routes.LoginRoute)
                            }
                        }
                        entry<Routes.LoginRoute> {
                            com.project.vaultly.feature.auth.presentation.LoginScreen()
                        }
                        entry<Routes.RegisterRoute> {
                            com.project.vaultly.feature.auth.presentation.RegisterScreen()
                        }
                        entry<Routes.HomeRoute> { route: Routes.HomeRoute ->
                            com.project.vaultly.feature.home.HomeScreen(email = route.email)
                        }
                        entry<Routes.ListTransactionRoute> {
                            com.project.vaultly.feature.transaction.presentation.ListTransactionScreen()
                        }
                        entry<Routes.CreateTransactionRoute> { route: Routes.CreateTransactionRoute ->
                            com.project.vaultly.feature.transaction.presentation.CreateTransactionScreen(
                                transactionId = route.transactionId
                            )
                        }
                        entry<Routes.DetailTransactionRoute> { route: Routes.DetailTransactionRoute ->
                            com.project.vaultly.feature.transaction.presentation.DetailTransactionScreen(
                                transactionId = route.transactionId,
                                isFromDeficit = route.isFromDeficit
                            )
                        }
                        entry<Routes.BudgetPlanningRoute> {
                            com.project.vaultly.feature.budgeting.presentation.BudgetPlanningScreen()
                        }

                        entry<Routes.DeficitAlertRoute> { route: Routes.DeficitAlertRoute ->
                            com.project.vaultly.feature.adaptive.presentation.DeficitAlertScreen(
                                deficitAmount = route.deficitAmount,
                                onDismiss = { backStack.navigateBack() },
                                onNavigateToAdaptive = {
                                    backStack.navigateTo(
                                        Routes.AdaptiveReallocationRoute(
                                            deficitAmount = route.deficitAmount,
                                            recommendations = "Kurangi pengeluaran tidak wajib bulan ini"
                                        )
                                    )
                                }
                            )
                        }

                        entry<Routes.ReportRoute> {
                            com.project.vaultly.feature.report.presentation.ReportScreen()
                        }
                    }
                )
            }
        }
    }
}

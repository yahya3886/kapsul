package com.kapsul.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kapsul.app.data.CapsuleRepository
import com.kapsul.app.ui.detail.CapsuleDetailScreen
import com.kapsul.app.ui.list.CapsuleListScreen
import com.kapsul.app.viewmodel.KapsulViewModel

/**
 * Tek activity köprüsü: liste ↔ detay navigasyonu.
 *
 * ViewModel aktivite kapsamında tekeldir; repository paylaşıldığı için
 * listede yapılan değişiklik (açma/silme) anında görünür.
 */
@Composable
fun KapsulRoot() {
    val context = LocalContext.current
    val vm: KapsulViewModel = viewModel(
        factory = KapsulViewModel.factory(CapsuleRepository.shared(context)),
    )
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            CapsuleListScreen(
                vm = vm,
                onOpen = { id -> navController.navigate("detail/$id") },
            )
        }
        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { backStackEntry ->
            CapsuleDetailScreen(
                vm = vm,
                capsuleId = backStackEntry.arguments?.getString("id"),
                onBack = { navController.popBackStack() },
            )
        }
    }
}

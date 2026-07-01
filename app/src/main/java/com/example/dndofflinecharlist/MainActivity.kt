package com.example.dndofflinecharlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dndofflinecharlist.data.preferences.AppTheme
import com.example.dndofflinecharlist.data.repository.CharacterRepository
import com.example.dndofflinecharlist.data.repository.FileCharacterRepository
import com.example.dndofflinecharlist.feature.list.CharacterListScreen
import com.example.dndofflinecharlist.feature.editor.CharacterDetailScreen
import com.example.dndofflinecharlist.navigation.Screen
import com.example.dndofflinecharlist.ui.settings.SettingsViewModel

class MainActivity : ComponentActivity() {
    private lateinit var repository: CharacterRepository
    // ✅ НОВОЕ: создаём SettingsViewModel через делегат viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = FileCharacterRepository(applicationContext)

        setContent {
            // ✅ НОВОЕ: наблюдаем за текущей темой
            val currentTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle()

            // ✅ НОВОЕ: выбираем ColorScheme в зависимости от темы
            val colorScheme = when (currentTheme) {
                AppTheme.LIGHT -> lightColorScheme()
                AppTheme.DARK -> darkColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        repository = repository,
                        onToggleTheme = { settingsViewModel.toggleTheme() }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    repository: CharacterRepository,
    onToggleTheme: () -> Unit // ✅ НОВОЕ: передаём лямбду для переключения темы
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.LIST
    ) {
        composable(Screen.LIST) {
            CharacterListScreen(
                onCharacterClick = { characterId ->
                    navController.navigate(Screen.editor(characterId))
                },
                onNewCharacterClick = {
                    navController.navigate(Screen.editor("new"))
                },
                onToggleTheme = onToggleTheme, // ✅ НОВОЕ: передаём дальше
                repository = repository
            )
        }

        composable(
            route = Screen.EDITOR,
            arguments = listOf(navArgument("characterId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId")
            val actualId = if (characterId == "new" || characterId.isNullOrBlank()) null else characterId

            CharacterDetailScreen(
                characterId = actualId,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
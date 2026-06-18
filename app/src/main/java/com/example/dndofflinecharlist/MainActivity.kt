package com.example.dndofflinecharlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dndofflinecharlist.data.repository.CharacterRepository
import com.example.dndofflinecharlist.data.repository.FileCharacterRepository
import com.example.dndofflinecharlist.dnd5e.Dnd5eCharacterViewModel
import com.example.dndofflinecharlist.dnd5e.Dnd5eEditorScreen
import com.example.dndofflinecharlist.list.CharacterListScreen
import com.example.dndofflinecharlist.navigation.Screen

class MainActivity : ComponentActivity() {

    private lateinit var repository: CharacterRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = FileCharacterRepository(applicationContext)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(repository)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(repository: CharacterRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.LIST
    ) {
        composable(Screen.LIST) {
            CharacterListScreen(
                onCharacterClick = { characterId ->
                    navController.navigate(Screen.editor(characterId))
                }
            )
        }

        composable(
            route = Screen.EDITOR,
            arguments = listOf(navArgument("characterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId") ?: return@composable

            val viewModel: Dnd5eCharacterViewModel = viewModel(
                factory = Dnd5eViewModelFactory(repository, characterId)
            )

            Dnd5eEditorScreen(viewModel = viewModel)
        }
    }
}

class Dnd5eViewModelFactory(
    private val repository: CharacterRepository,
    private val characterId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(Dnd5eCharacterViewModel::class.java)) {
            return Dnd5eCharacterViewModel(repository, characterId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
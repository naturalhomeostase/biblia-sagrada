package com.bibliasagrada.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.ui.screens.AboutScreen
import com.bibliasagrada.app.ui.screens.BackupScreen
import com.bibliasagrada.app.ui.screens.BookmarksScreen
import com.bibliasagrada.app.ui.screens.DailyVerseScreen
import com.bibliasagrada.app.ui.screens.BooksScreen
import com.bibliasagrada.app.ui.screens.ChaptersScreen
import com.bibliasagrada.app.ui.screens.FavoritesScreen
import com.bibliasagrada.app.ui.screens.HelpScreen
import com.bibliasagrada.app.ui.screens.HistoryScreen
import com.bibliasagrada.app.ui.screens.NotesScreen
import com.bibliasagrada.app.ui.screens.PromisesScreen
import com.bibliasagrada.app.ui.screens.ReaderScreen
import com.bibliasagrada.app.ui.screens.SearchScreen
import com.bibliasagrada.app.ui.screens.SettingsScreen
import com.bibliasagrada.app.ui.screens.TranslationsScreen

@Composable
fun AppNavGraph(repository: BibleRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.DAILY_VERSE) {
        composable(Routes.DAILY_VERSE) {
            DailyVerseScreen(
                repository = repository,
                onContinue = {
                    navController.navigate(Routes.BOOKS) {
                        popUpTo(Routes.DAILY_VERSE) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.BOOKS) {
            BooksScreen(
                repository = repository,
                onOpenBook = { bookId -> navController.navigate(Routes.chapters(bookId)) },
                onOpenReader = { bookId, chapter, verse ->
                    navController.navigate(Routes.reader(bookId, chapter, verse))
                },
                onSearch = { navController.navigate(Routes.SEARCH) },
                onFavorites = { navController.navigate(Routes.FAVORITES) },
                onHistory = { navController.navigate(Routes.HISTORY) },
                onNotes = { navController.navigate(Routes.NOTES) },
                onBookmarks = { navController.navigate(Routes.BOOKMARKS) },
                onTranslations = { navController.navigate(Routes.TRANSLATIONS) },
                onPromises = { navController.navigate(Routes.PROMISES) },
                onBackup = { navController.navigate(Routes.BACKUP) },
                onHelp = { navController.navigate(Routes.HELP) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onAbout = { navController.navigate(Routes.ABOUT) }
            )
        }
        composable(
            Routes.CHAPTERS,
            arguments = listOf(navArgument("bookId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: return@composable
            ChaptersScreen(
                repository = repository,
                bookId = bookId,
                onBack = { navController.popBackStack() },
                onOpenChapter = { chapter -> navController.navigate(Routes.reader(bookId, chapter)) }
            )
        }
        composable(
            Routes.READER,
            arguments = listOf(
                navArgument("bookId") { type = NavType.IntType },
                navArgument("chapter") { type = NavType.IntType },
                navArgument("verse") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: return@composable
            val chapter = backStackEntry.arguments?.getInt("chapter") ?: return@composable
            val verse = backStackEntry.arguments?.getInt("verse") ?: 0
            ReaderScreen(
                repository = repository,
                initialBookId = bookId,
                initialChapter = chapter,
                initialVerse = verse,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenReader = { bookId, chapter, verse ->
                    navController.navigate(Routes.reader(bookId, chapter, verse))
                }
            )
        }
        composable(Routes.FAVORITES) {
            FavoritesScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenReader = { bookId, chapter, verse ->
                    navController.navigate(Routes.reader(bookId, chapter, verse))
                }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenReader = { bookId, chapter ->
                    navController.navigate(Routes.reader(bookId, chapter))
                }
            )
        }
        composable(Routes.NOTES) {
            NotesScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenReader = { bookId, chapter, verse ->
                    navController.navigate(Routes.reader(bookId, chapter, verse))
                }
            )
        }
        composable(Routes.BOOKMARKS) {
            BookmarksScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenReader = { bookId, chapter ->
                    navController.navigate(Routes.reader(bookId, chapter))
                }
            )
        }
        composable(Routes.TRANSLATIONS) {
            TranslationsScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onTranslationSwitched = {
                    // Após trocar de tradução, volta para a lista de livros e limpa
                    // toda a pilha de navegação para que as telas recarreguem os
                    // dados a partir do novo banco.
                    navController.navigate(Routes.BOOKS) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(Routes.PROMISES) {
            PromisesScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenReader = { bookId, chapter, verse ->
                    navController.navigate(Routes.reader(bookId, chapter, verse))
                }
            )
        }
        composable(Routes.BACKUP) {
            BackupScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onRestored = {
                    // Depois de restaurar, reinicia toda a navegação para que
                    // as telas recarreguem favoritos/notas/marcadores do novo banco.
                    navController.navigate(Routes.BOOKS) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(Routes.HELP) {
            HelpScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenReader = { bookId, chapter, verse ->
                    navController.navigate(Routes.reader(bookId, chapter, verse))
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

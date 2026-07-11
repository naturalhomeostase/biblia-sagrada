package com.bibliasagrada.app.ui.navigation

object Routes {
    const val BOOKS = "books"
    const val CHAPTERS = "chapters/{bookId}"
    const val READER = "reader/{bookId}/{chapter}/{verse}"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val NOTES = "notes"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val BOOKMARKS = "bookmarks"
    const val TRANSLATIONS = "translations"

    fun chapters(bookId: Int) = "chapters/$bookId"
    fun reader(bookId: Int, chapter: Int, verse: Int = 0) = "reader/$bookId/$chapter/$verse"
}

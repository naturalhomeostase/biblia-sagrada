package com.bibliasagrada.app.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração 6 → 7: adiciona as tabelas de tags de estudo (`tags` e
 * `verse_tags`), sem tocar em nenhuma tabela existente. Sem isso, o Room
 * cairia no fallbackToDestructiveMigration() abaixo e apagaria TODOS os
 * dados já salvos (favoritos, notas, destaques, marcadores, histórico) no
 * primeiro app aberto depois da atualização — por isso essa migração
 * explícita é essencial, mesmo o app não estando publicado ainda.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `tags` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `color` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `verse_tags` (
                `tagId` INTEGER NOT NULL,
                `bookId` INTEGER NOT NULL,
                `chapter` INTEGER NOT NULL,
                `verse` INTEGER NOT NULL,
                `addedAt` INTEGER NOT NULL,
                PRIMARY KEY(`tagId`, `bookId`, `chapter`, `verse`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `partial_highlights` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `bookId` INTEGER NOT NULL,
                `chapter` INTEGER NOT NULL,
                `verse` INTEGER NOT NULL,
                `startOffset` INTEGER NOT NULL,
                `endOffset` INTEGER NOT NULL,
                `color` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities = [
        FavoriteEntity::class,
        HighlightEntity::class,
        NoteEntity::class,
        HistoryEntity::class,
        ReadingProgressEntity::class,
        BookmarkEntity::class,
        TagEntity::class,
        VerseTagEntity::class,
        PartialHighlightEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class UserDataDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun highlightDao(): HighlightDao
    abstract fun noteDao(): NoteDao
    abstract fun historyDao(): HistoryDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tagDao(): TagDao
    abstract fun verseTagDao(): VerseTagDao
    abstract fun partialHighlightDao(): PartialHighlightDao

    companion object {
        @Volatile private var instance: UserDataDatabase? = null
        fun getInstance(context: Context): UserDataDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    UserDataDatabase::class.java,
                    "user_data.db"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                    // Rede de segurança só para saltos de versão sem migração definida
                    // (não deveria acontecer em uso normal, já que 6→7 e 7→8 têm migração acima).
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }

        /** Fecha a conexão atual e limpa o singleton — usado antes de restaurar um backup. */
        fun closeAndReset() {
            synchronized(this) {
                instance?.close()
                instance = null
            }
        }
    }
}

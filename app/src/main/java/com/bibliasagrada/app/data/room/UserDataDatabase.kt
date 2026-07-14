package com.bibliasagrada.app.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        HighlightEntity::class,
        NoteEntity::class,
        HistoryEntity::class,
        ReadingProgressEntity::class,
        BookmarkEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class UserDataDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun highlightDao(): HighlightDao
    abstract fun noteDao(): NoteDao
    abstract fun historyDao(): HistoryDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile private var instance: UserDataDatabase? = null
        fun getInstance(context: Context): UserDataDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    UserDataDatabase::class.java,
                    "user_data.db"
                )
                    // App ainda não publicado — sem usuários com banco antigo em produção,
                    // mas mantemos a migração destrutiva como rede de segurança para futuras
                    // mudanças de schema (evita crash em vez de exigir migração manual).
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

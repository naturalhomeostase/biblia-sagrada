package com.bibliasagrada.app.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE bookId=:bookId AND chapter=:chapter AND verse=:verse)")
    suspend fun isFavorite(bookId: Int, chapter: Int, verse: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE bookId=:bookId AND chapter=:chapter AND verse=:verse")
    suspend fun delete(bookId: Int, chapter: Int, verse: Int)
}

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE bookId=:bookId AND chapter=:chapter")
    fun observeForChapter(bookId: Int, chapter: Int): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HighlightEntity)

    @Query("DELETE FROM highlights WHERE bookId=:bookId AND chapter=:chapter AND verse=:verse")
    suspend fun delete(bookId: Int, chapter: Int, verse: Int)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE bookId=:bookId AND chapter=:chapter AND verse=:verse LIMIT 1")
    suspend fun getForVerse(bookId: Int, chapter: Int, verse: Int): NoteEntity?

    @Query("SELECT * FROM notes WHERE bookId=:bookId AND chapter=:chapter")
    fun observeForChapter(bookId: Int, chapter: Int): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoteEntity): Long

    @Update
    suspend fun update(entity: NoteEntity)

    @Delete
    suspend fun delete(entity: NoteEntity)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY lastReadAt DESC LIMIT 50")
    fun observeRecent(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clear()
}

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE id=0 LIMIT 1")
    fun observe(): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE id=0 LIMIT 1")
    suspend fun get(): ReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: ReadingProgressEntity)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookId=:bookId AND chapter=:chapter ORDER BY createdAt DESC")
    fun observeForChapter(bookId: Int, chapter: Int): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookId=:bookId ORDER BY chapter ASC")
    fun observeForBook(bookId: Int): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id=:id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bookmarks WHERE bookId=:bookId AND chapter=:chapter")
    suspend fun deleteForChapter(bookId: Int, chapter: Int)
}

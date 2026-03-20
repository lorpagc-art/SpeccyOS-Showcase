package com.example.speccyose5ultrav021b

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY title ASC")
    fun getAllGames(): Flow<List<Game>>

    @Query("SELECT * FROM games ORDER BY title ASC")
    suspend fun getAllGamesList(): List<Game>

    @Query("SELECT COUNT(*) FROM games WHERE platformId = :platformId")
    suspend fun getGameCountByPlatform(platformId: String): Int

    @Query("SELECT * FROM games WHERE platformId = :platformId ORDER BY title ASC")
    fun getGamesByPlatform(platformId: String): Flow<List<Game>>

    @Query("SELECT DISTINCT platformId FROM games")
    fun getActivePlatformIds(): Flow<List<String>>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT 15")
    fun getRecentGames(): Flow<List<Game>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Game>)

    @Update
    suspend fun updateGame(game: Game)

    @Query("DELETE FROM games")
    suspend fun deleteAll()
}

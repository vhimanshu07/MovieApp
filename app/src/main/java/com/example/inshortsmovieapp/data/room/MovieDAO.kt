package com.example.inshortsmovieapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("SELECT * FROM movies WHERE category = :category ORDER BY page ASC")
    suspend fun getMoviesByCategoryOnce(category: String): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE category = :category AND page = :page ")
    suspend fun getMoviesByCategoryAndPage(category: String, page: Int): List<MovieEntity>

    @Query("DELETE FROM movies WHERE category = :category")
    suspend fun deleteMoviesByCategory(category: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaginationMetadata(metadata: PaginationMetadataEntity)

    @Query("SELECT * FROM pagination_metadata WHERE category = :category")
    suspend fun getPaginationMetadata(category: String): PaginationMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieDetail(movieDetail: MovieDetailEntity)

    @Query("SELECT * FROM movie_details WHERE id = :movieId")
    suspend fun getMovieDetailById(movieId: Int): MovieDetailEntity?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(movie: BookmarkedMovieEntity)

    @Query("DELETE FROM bookmarked_movies WHERE id = :movieId")
    suspend fun deleteBookmark(movieId: Int)

    @Query("SELECT * FROM bookmarked_movies ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedMovieEntity>>


    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_movies WHERE id = :movieId)")
    suspend fun isMovieBookmarked(movieId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_movies WHERE id = :movieId)")
    fun isMovieBookmarkedFlow(movieId: Int): Flow<Boolean>

    @Query("SELECT id FROM bookmarked_movies")
    suspend fun getAllBookmarkedIds(): List<Int>

    @Query("DELETE FROM movies WHERE category = :searchCategory")
    suspend fun clearSearchCacheForQuery(searchCategory: String)
}
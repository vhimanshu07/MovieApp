package com.example.inshortsmovieapp.data.repository

import com.example.inshortsmovieapp.constants.PaginatedResult
import com.example.inshortsmovieapp.constants.Result
import com.example.inshortsmovieapp.ui.model.Movie
import com.example.inshortsmovieapp.ui.model.MovieDetail
import kotlinx.coroutines.flow.Flow

interface MovieRepo {

    fun getMovieDetails(movieId: Int, forceRefresh: Boolean = false): Flow<Result<MovieDetail>>

    fun getBookmarkedMovies(): Flow<List<Movie>>

    suspend fun toggleBookmark(movie: Movie)

    suspend fun toggleBookmarkFromDetail(movie: MovieDetail)

    fun isMovieBookmarkedFlow(movieId: Int): Flow<Boolean>

    fun getTrendingMoviesPaginated(page: Int): Flow<Result<PaginatedResult<Movie>>>

    fun getNowPlayingMoviesPaginated(page: Int): Flow<Result<PaginatedResult<Movie>>>

    fun searchMoviesPaginated(query: String, page: Int): Flow<Result<PaginatedResult<Movie>>>
}
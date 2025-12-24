package com.example.inshortsmovieapp.data.repository

import android.util.Log
import com.example.inshortsmovieapp.constants.Constants
import com.example.inshortsmovieapp.constants.PaginatedResult
import com.example.inshortsmovieapp.constants.Result
import com.example.inshortsmovieapp.data.api.MovieApi
import com.example.inshortsmovieapp.data.models.mapper.toBookmarkedEntity
import com.example.inshortsmovieapp.data.models.mapper.toDomain
import com.example.inshortsmovieapp.data.models.mapper.toEntity
import com.example.inshortsmovieapp.data.models.mapper.toMovie
import com.example.inshortsmovieapp.data.room.MovieDAO
import com.example.inshortsmovieapp.data.room.MovieEntity
import com.example.inshortsmovieapp.data.room.PaginationMetadataEntity
import com.example.inshortsmovieapp.ui.model.Movie
import com.example.inshortsmovieapp.ui.model.MovieDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class MovieRepoImpl @Inject constructor(
    private val movieApi: MovieApi,
    private val movieDao: MovieDAO
) : MovieRepo {

    private val apiKey = Constants.API_KEY
    override fun getMovieDetails(movieId: Int, forceRefresh: Boolean): Flow<Result<MovieDetail>> =
        flow {
            val isBookmarked = movieDao.isMovieBookmarked(movieId)
            val cachedDetail = movieDao.getMovieDetailById(movieId)
            emit(Result.Loading())
            Log.d("CHECK->", "getMovieDetails : Movie id:-> $movieId isBookmarked:-> $isBookmarked")
            if (cachedDetail != null && !forceRefresh) {
                emit(Result.Success(cachedDetail.toDomain(isBookmarked)))
                if (System.currentTimeMillis() - cachedDetail.lastUpdated < Constants.CACHE_EXPIRY_TIME) {
                    return@flow
                }
            }
            try {
                val response = movieApi.getMovieDetails(movieId, apiKey)
                val movieDetailEntity = response.toEntity()
                movieDao.insertMovieDetail(movieDetailEntity)
                Log.d("CHECK->", "getMovieDetails : $response")
                emit(Result.Success(response.toDomain(isBookmarked)))
            } catch (e: Exception) {
                Log.d("CHECK->", "getMovieDetails : $e")
                if (cachedDetail != null) {
                    emit(Result.Error("Network error", cachedDetail.toDomain(isBookmarked)))
                } else {
                    emit(Result.Error("Network error"))
                }
            }
        }

    override fun getBookmarkedMovies(): Flow<List<Movie>> {
        return movieDao.getAllBookmarks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun toggleBookmark(movie: Movie) {
        val isCurrentlyBookmarked = movieDao.isMovieBookmarked(movie.id)
        if (isCurrentlyBookmarked) {
            movieDao.deleteBookmark(movie.id)
        } else {
            movieDao.insertBookmark(movie.toBookmarkedEntity())
        }
    }

    override suspend fun toggleBookmarkFromDetail(movie: MovieDetail) {
        val isCurrentlyBookmarked = movieDao.isMovieBookmarked(movie.id)
        if (isCurrentlyBookmarked) {
            movieDao.deleteBookmark(movie.id)
        } else {
            movieDao.insertBookmark(movie.toMovie().toBookmarkedEntity())
        }
    }

    override fun isMovieBookmarkedFlow(movieId: Int): Flow<Boolean> {
        return movieDao.isMovieBookmarkedFlow(movieId)
    }

    override fun getTrendingMoviesPaginated(page: Int): Flow<Result<PaginatedResult<Movie>>> =
        flow {
            emit(Result.Loading())
            val bookmarkedIds = movieDao.getAllBookmarkedIds().toSet()
            val category = Constants.CATEGORY_TRENDING
            val cachedMovies = movieDao.getMoviesByCategoryAndPage(category, page)
            val cachedMetadata = movieDao.getPaginationMetadata(category)
            try {
                val response = movieApi.getTrendingMovies(apiKey, page)
                val movieEntities = response.results.map { it.toEntity(category, page) }
                if (page == 1) {
                    movieDao.deleteMoviesByCategory(category)
                }
                movieDao.insertMovies(movieEntities)
                movieDao.insertPaginationMetadata(
                    PaginationMetadataEntity(
                        category = category,
                        currentPage = response.page,
                        totalPages = response.totalPages,
                        totalResults = response.totalResults
                    )
                )
                val movies = movieEntities.map { it.toDomain(bookmarkedIds.contains(it.id)) }
                val paginatedResult = PaginatedResult(
                    data = movies,
                    currentPage = response.page,
                    totalPages = response.totalPages,
                    totalResults = response.totalResults
                )
                Log.d("CHECK->", "getTrendingMoviesPaginated page $page: result-> $paginatedResult")
                emit(Result.Success(paginatedResult))
            } catch (e: Exception) {
                Log.d("CHECK->", "getTrendingMoviesPaginated error: $e")
                ErrorHandling(cachedMovies, bookmarkedIds, page, cachedMetadata, category)
            }
        }


    override fun getNowPlayingMoviesPaginated(page: Int): Flow<Result<PaginatedResult<Movie>>> =
        flow {
            emit(Result.Loading())
            val bookmarkedIds = movieDao.getAllBookmarkedIds().toSet()
            val category = Constants.CATEGORY_NOW_PLAYING
            val cachedMovies = movieDao.getMoviesByCategoryAndPage(category, page)
            val cachedMetadata = movieDao.getPaginationMetadata(category)
            try {
                val response = movieApi.getNowPlayingMovies(apiKey, page)
                val movieEntities = response.results.map { it.toEntity(category, page) }
                if (page == 1) {
                    movieDao.deleteMoviesByCategory(category)
                }
                movieDao.insertMovies(movieEntities)
                movieDao.insertPaginationMetadata(
                    PaginationMetadataEntity(
                        category = category,
                        currentPage = response.page,
                        totalPages = response.totalPages,
                        totalResults = response.totalResults
                    )
                )
                val movies = movieEntities.map { it.toDomain(bookmarkedIds.contains(it.id)) }
                val paginatedResult = PaginatedResult(
                    data = movies,
                    currentPage = response.page,
                    totalPages = response.totalPages,
                    totalResults = response.totalResults
                )
                Log.d("CHECK->", "getNowPlayingMoviesPaginated page $page: $response")
                emit(Result.Success(paginatedResult))
            } catch (e: Exception) {
                Log.d("CHECK->", "getNowPlayingMoviesPaginated error: $e")
                ErrorHandling(cachedMovies, bookmarkedIds, page, cachedMetadata, category)
            }
        }

    override fun searchMoviesPaginated(
        query: String,
        page: Int
    ): Flow<Result<PaginatedResult<Movie>>> = flow {
        emit(Result.Loading())
        val bookmarkedIds = movieDao.getAllBookmarkedIds().toSet()
        val searchCategory = "${Constants.CATEGORY_SEARCH}_$query"
        val cachedMovies = movieDao.getMoviesByCategoryAndPage(searchCategory, page)
        val cachedMetadata = movieDao.getPaginationMetadata(searchCategory)
        try {
            val response = movieApi.searchMovies(apiKey, query, page)
            val movieEntities = response.results.map { it.toEntity(searchCategory, page) }
            if (page == 1) {
                movieDao.clearSearchCacheForQuery(searchCategory)
            }
            movieDao.insertMovies(movieEntities)
            movieDao.insertPaginationMetadata(
                PaginationMetadataEntity(
                    category = searchCategory,
                    currentPage = response.page,
                    totalPages = response.totalPages,
                    totalResults = response.totalResults
                )
            )
            val movies = movieEntities.map { it.toDomain(bookmarkedIds.contains(it.id)) }
            val paginatedResult = PaginatedResult(
                data = movies,
                currentPage = response.page,
                totalPages = response.totalPages,
                totalResults = response.totalResults
            )
            Log.d("CHECK->", "searchMoviesPaginated query '$query' page $page: $paginatedResult")
            emit(Result.Success(paginatedResult))
        } catch (e: Exception) {
            Log.d("CHECK->", "searchMoviesPaginated error: $e")
            ErrorHandling(cachedMovies, bookmarkedIds, page, cachedMetadata, searchCategory)
        }
    }

    private suspend fun FlowCollector<Result<PaginatedResult<Movie>>>.ErrorHandling(
        cachedMovies: List<MovieEntity>,
        bookmarkedIds: Set<Int>,
        page: Int,
        cachedMetadata: PaginationMetadataEntity?,
        category: String
    ) {
        if (cachedMovies.isNotEmpty()) {
            val movies = cachedMovies.map { it.toDomain(bookmarkedIds.contains(it.id)) }
            val paginatedResult = PaginatedResult(
                data = movies,
                currentPage = page,
                totalPages = cachedMetadata?.totalPages ?: page,
                totalResults = cachedMetadata?.totalResults ?: movies.size
            )
            emit(Result.Error("Offline", paginatedResult))
        } else if (page == 1) {
            val allCachedMovies = movieDao.getMoviesByCategoryOnce(category)
            if (allCachedMovies.isNotEmpty()) {
                val movies =
                    allCachedMovies.map { it.toDomain(bookmarkedIds.contains(it.id)) }
                val paginatedResult = PaginatedResult(
                    data = movies,
                    currentPage = 1,
                    totalPages = cachedMetadata?.totalPages ?: 1,
                    totalResults = cachedMetadata?.totalResults ?: movies.size
                )
                emit(Result.Error("Offline", paginatedResult))
            } else {
                emit(Result.Error("Failed to load trending movies"))
            }
        } else {
            emit(Result.Error("Failed to load more movies"))
        }
    }


}

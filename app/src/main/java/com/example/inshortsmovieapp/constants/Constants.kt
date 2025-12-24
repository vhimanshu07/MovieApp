package com.example.inshortsmovieapp.constants

object Constants {
    const val CATEGORY_TRENDING = "trending"
    const val CATEGORY_NOW_PLAYING = "now_playing"
    const val CATEGORY_SEARCH = "search"
    const val CACHE_EXPIRY_TIME = 30 * 60 * 1000L
    const val API_KEY = "435b639d42b15cebb824e182d3572730"
    const val BASE_URL = "https://api.themoviedb.org/3/"
}

data class PaginatedResult<T>(
    val data: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val totalResults: Int
)



sealed class Result<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Result<T>(data)
    class Error<T>(message: String, data: T? = null) : Result<T>(data, message)
    class Loading<T>(data: T? = null) : Result<T>(data)
}

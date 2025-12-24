package com.example.inshortsmovieapp.ui.model

data class Movie(
    val id: Int,
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: List<Int>?,
    val originalLanguage: String?,
    val isBookmarked: Boolean = false
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }

    val year: String?
        get() = releaseDate?.take(4)

    val ratingFormatted: String
        get() = String.format("%.1f", voteAverage)
}

data class MovieDetail(
    val id: Int,
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>?,
    val runtime: Int?,
    val status: String?,
    val tagline: String?,
    val imdbId: String?,
    val originalLanguage: String?,
    val isBookmarked: Boolean = false
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/original$it" }

    val year: String?
        get() = releaseDate?.take(4)

    val ratingFormatted: String
        get() = String.format("%.1f", voteAverage)

    val runtimeFormatted: String?
        get() = runtime?.let {
            val hours = it / 60
            val minutes = it % 60
            if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }

}

data class Genre(
    val id: Int,
    val name: String
)


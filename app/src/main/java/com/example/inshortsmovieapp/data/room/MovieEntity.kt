package com.example.inshortsmovieapp.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movies",
    indices = [Index(value = ["category", "page"])]
)
data class MovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: String?,
    val originalLanguage: String?,
    val category: String,
    val page: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "pagination_metadata")
data class PaginationMetadataEntity(
    @PrimaryKey
    val category: String,
    val currentPage: Int,
    val totalPages: Int,
    val totalResults: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "movie_details")
data class MovieDetailEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: String?,
    val runtime: Int?,
    val status: String?,
    val tagline: String?,
    val imdbId: String?,
    val originalLanguage: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarked_movies")
data class BookmarkedMovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: String?,
    val originalLanguage: String?,
    val bookmarkedAt: Long = System.currentTimeMillis()
)



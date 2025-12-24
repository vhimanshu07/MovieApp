package com.example.inshortsmovieapp.data.models.mapper

import com.example.inshortsmovieapp.data.models.apiResponse.GenreDto
import com.example.inshortsmovieapp.data.models.apiResponse.MovieDetailDto
import com.example.inshortsmovieapp.data.models.apiResponse.MovieDto
import com.example.inshortsmovieapp.data.room.BookmarkedMovieEntity
import com.example.inshortsmovieapp.data.room.MovieDetailEntity
import com.example.inshortsmovieapp.data.room.MovieEntity
import com.example.inshortsmovieapp.ui.model.Genre
import com.example.inshortsmovieapp.ui.model.Movie
import com.example.inshortsmovieapp.ui.model.MovieDetail
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GsonHelper {
    val gson = Gson()
}

fun MovieDto.toEntity(category: String, page: Int = 1): MovieEntity {
    return MovieEntity(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genreIds?.joinToString(","),
        originalLanguage = originalLanguage,
        category = category,
        page = page
    )
}

fun MovieDetailDto.toEntity(): MovieDetailEntity {
    return MovieDetailEntity(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres?.let { GsonHelper.gson.toJson(it) },
        runtime = runtime,
        status = status,
        tagline = tagline,
        imdbId = imdbId,
        originalLanguage = originalLanguage
    )
}

fun MovieEntity.toDomain(isBookmarked: Boolean = false): Movie {
    return Movie(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genreIds?.split(",")?.mapNotNull { it.toIntOrNull() },
        originalLanguage = originalLanguage,
        isBookmarked = isBookmarked
    )
}

fun MovieDetailEntity.toDomain(isBookmarked: Boolean = false): MovieDetail {
    val genreType = object : TypeToken<List<GenreDto>>() {}.type
    return MovieDetail(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres?.let {
            GsonHelper.gson.fromJson<List<GenreDto>>(it, genreType)?.map { dto -> dto.toDomain() }
        },
        runtime = runtime,
        status = status,
        tagline = tagline,
        imdbId = imdbId,
        originalLanguage = originalLanguage,
        isBookmarked = isBookmarked
    )
}

fun BookmarkedMovieEntity.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genreIds?.split(",")?.mapNotNull { it.toIntOrNull() },
        originalLanguage = originalLanguage,
        isBookmarked = true
    )
}

fun MovieDetailDto.toDomain(isBookmarked: Boolean = false): MovieDetail {
    return MovieDetail(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres?.map { it.toDomain() },
        runtime = runtime,
        status = status,
        tagline = tagline,
        imdbId = imdbId,
        originalLanguage = originalLanguage,
        isBookmarked = isBookmarked
    )
}

fun GenreDto.toDomain(): Genre {
    return Genre(id = id, name = name)
}

fun Movie.toBookmarkedEntity(): BookmarkedMovieEntity {
    return BookmarkedMovieEntity(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genreIds?.joinToString(","),
        originalLanguage = originalLanguage
    )
}

fun MovieDetail.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genres?.map { it.id },
        originalLanguage = originalLanguage,
        isBookmarked = isBookmarked
    )
}



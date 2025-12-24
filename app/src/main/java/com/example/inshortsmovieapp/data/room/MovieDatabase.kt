package com.example.inshortsmovieapp.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MovieEntity::class,
        MovieDetailEntity::class,
        BookmarkedMovieEntity::class,
        PaginationMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun getDao(): MovieDAO

    companion object {
        const val DATABASE_NAME = "movieDB"

    }
}
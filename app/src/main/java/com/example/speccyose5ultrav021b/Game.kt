package com.example.speccyose5ultrav021b

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "games",
    indices = [
        Index("platformId"),
        Index("isFavorite"),
        Index("lastPlayed")
    ]
)
data class Game(
    @PrimaryKey val path: String,
    val title: String,
    val platformId: String,
    val extension: String,
    val fileName: String,
    var boxArt: String? = null,
    var videoPreview: String? = null,
    var description: String? = null,
    var developer: String? = "Desconocido",
    var genre: String? = "Retro",
    var releaseDate: String? = "N/A",
    var playCount: Int = 0,
    var lastPlayed: Long = 0,
    var isFavorite: Boolean = false
)

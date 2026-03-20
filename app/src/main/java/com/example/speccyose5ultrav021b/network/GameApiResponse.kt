package com.example.speccyose5ultrav021b.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameSearchResponse(
    @Json(name = "data") val data: GameListData,
    @Json(name = "remaining_downloads") val remainingDownloads: Int?
)

@JsonClass(generateAdapter = true)
data class GameListData(
    @Json(name = "games") val games: List<GameApiData>
)

@JsonClass(generateAdapter = true)
data class GameApiData(
    @Json(name = "id") val id: Long,
    @Json(name = "game_title") val gameTitle: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "developer") val developer: Int?,
    @Json(name = "genres") val genres: List<Int>?
)

@JsonClass(generateAdapter = true)
data class GameImagesResponse(
    @Json(name = "data") val data: GameImagesData
)

@JsonClass(generateAdapter = true)
data class GameImagesData(
    @Json(name = "base_url") val baseUrl: BaseUrlData,
    @Json(name = "images") val images: Map<String, List<GameImage>>
)

@JsonClass(generateAdapter = true)
data class BaseUrlData(
    @Json(name = "medium") val medium: String,
    @Json(name = "original") val original: String,
    @Json(name = "small") val small: String,
    @Json(name = "large") val large: String
)

@JsonClass(generateAdapter = true)
data class GameImage(
    @Json(name = "id") val id: Long,
    @Json(name = "type") val type: String,
    @Json(name = "side") val side: String?, // Añadido para distinguir front/back
    @Json(name = "filename") val filename: String
)

package es.androidtfm.gamevision.retrofit


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/01/2025
 * Descripción: 
 */

data class ApiResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?,
    @SerializedName("results") val results: List<Game>
)

data class Game(
    @SerializedName("slug") val slug: String,
    @SerializedName("name") val name: String,
    @SerializedName("playtime") val playtime: Int,
    @SerializedName("platforms") val platforms: List<Platform>,
    @SerializedName("stores") val stores: List<StoreWrapper>,
    @SerializedName("released") val released: String,
    @SerializedName("tba") val tba: Boolean,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("rating") val rating: Double,
    @SerializedName("rating_top") val ratingTop: Int,
    @SerializedName("ratings") val ratings: List<Rating>,
    @SerializedName("ratings_count") val ratingsCount: Int,
    @SerializedName("reviews_text_count") val reviewsTextCount: Int,
    @SerializedName("added") val added: Int,
    @SerializedName("added_by_status") val addedByStatus: AddedByStatus?,
    @SerializedName("metacritic") val metacritic: Int?,
    @SerializedName("suggestions_count") val suggestionsCount: Int,
    @SerializedName("updated") val updated: String,
    @SerializedName("id") val id: Int,
    @SerializedName("score") val score: String?,
    @SerializedName("clip") val clip: String?,
    @SerializedName("tags") val tags: List<Tag>,
    @SerializedName("esrb_rating") val esrbRating: EsrbRating?,
    @SerializedName("user_game") val userGame: String?,
    @SerializedName("reviews_count") val reviewsCount: Int,
    @SerializedName("saturated_color") val saturatedColor: String,
    @SerializedName("dominant_color") val dominantColor: String,
    @SerializedName("short_screenshots") val shortScreenshots: List<ShortScreenshot>,
    @SerializedName("parent_platforms") val parentPlatforms: List<ParentPlatformWrapper>,
    @SerializedName("genres") val genres: List<Genre>
)
data class Platform(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)

data class StoreWrapper(
    @SerializedName("store") val store: Store
)

data class Store(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)

data class Rating(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("count") val count: Int,
    @SerializedName("percent") val percent: Double
)

data class AddedByStatus(
    @SerializedName("yet") val yet: Int?,
    @SerializedName("owned") val owned: Int?,
    @SerializedName("beaten") val beaten: Int?,
    @SerializedName("toplay") val toplay: Int?,
    @SerializedName("dropped") val dropped: Int?,
    @SerializedName("playing") val playing: Int?
)

data class Tag(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("language") val language: String,
    @SerializedName("games_count") val gamesCount: Int,
    @SerializedName("image_background") val imageBackground: String
)

data class EsrbRating(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("name_en") val nameEn: String?,
    @SerializedName("name_ru") val nameRu: String?
)

data class ShortScreenshot(
    @SerializedName("id") val id: Int,
    @SerializedName("image") val image: String
)

data class ParentPlatformWrapper(
    @SerializedName("platform") val platform: ParentPlatform
)

data class ParentPlatform(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)

data class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)
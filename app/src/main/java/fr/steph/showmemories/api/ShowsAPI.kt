package fr.steph.showmemories.api

import fr.steph.showmemories.BuildConfig
import fr.steph.showmemories.model.tmdbmodels.TmdbTv
import fr.steph.showmemories.model.tmdbmodels.TmdbTvResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ShowsAPI {

    companion object {
        const val API_KEY = BuildConfig.API_KEY
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val STARTING_PAGE_INDEX = 1
        const val NETWORK_PAGE_SIZE = 20
    }

    @GET("trending/tv/week")
    suspend fun getWeeklyTrendingShows(
        @Query("language") language: String = "fr",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = true,
        @Query("api_key") apiKey: String = API_KEY
    ): Response<TmdbTvResponse>

    @GET("search/tv")
    suspend fun searchForShows(
        @Query("query") query: String,
        @Query("language") language: String = "fr",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = true,
        @Query("api_key") apiKey: String = API_KEY
    ): Response<TmdbTvResponse>

    @GET("tv/{id}")
    suspend fun getShowInfo(
        @Path("id") id: Int,
        @Query("language") language: String = "fr",
        @Query("include_image_language") imageLanguage: String = "fr,en,null",
        @Query("append_to_response") appendToResponse: String = "images, similar",
        @Query("api_key") apiKey: String = API_KEY,
    ): Response<TmdbTv>
}
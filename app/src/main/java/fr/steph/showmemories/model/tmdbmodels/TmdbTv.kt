package fr.steph.showmemories.model.tmdbmodels

data class TmdbTv(
    val backdrop_path: String,
    val first_air_date: String,
    val genres: List<TmdbGenre>?,
    val id: Int,
    val images: TmdbImages?,
    val in_production: Boolean?,
    val name: String,
    val number_of_seasons: Int?,
    val overview: String,
    val poster_path: String,
    val seasons: List<TmdbSeason>?,
    val similar: TmdbTvResponse?,
    val vote_average: Double,
)
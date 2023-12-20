package fr.steph.showmemories.model.tmdbmodels

data class TmdbTvResponse(
    val page: Int,
    val results: List<TmdbTv>,
    val total_pages: Int,
    val total_results: Int
)
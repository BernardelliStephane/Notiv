package fr.steph.showmemories.models

import java.io.Serializable

data class SeasonModel(
    var id: String = "",
    val name: String = "",
    val watchDate: Long = 0,
    val note: Int = 0,
    val summary: String = "",
    val review: String = "",
    val movie: Boolean = false,
    val movieDuration: Int = 0,
    val episodeCount: Int = 0
) : Serializable {
    fun getSeasonNumber(): Int? {
        if(this.movie) return null
        return this.name.split(" ")[1].toInt()
    }
}
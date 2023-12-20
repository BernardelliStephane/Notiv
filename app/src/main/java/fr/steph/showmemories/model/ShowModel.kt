package fr.steph.showmemories.model

import java.io.Serializable

data class ShowModel(
    var id: String = "",
    val name: String = "",
    val alternateNames: ArrayList<String> = ArrayList(),
    val watchDate: Long = 0,
    val note: Int = 0,
    var imageUrl: String = "",
    val synopsis: String = "",
    val review: String = "",
    val seasons: ArrayList<SeasonModel> = ArrayList(),
) : Serializable {
    fun addSeason(currentSeason: SeasonModel): Int {
        if(this.seasons.isEmpty() || currentSeason.movie){
            this.seasons.add(currentSeason)
            return this.seasons.size
        }

        this.seasons.forEachIndexed { index, season ->
            if(season.movie || season.getSeasonNumber()!! > currentSeason.getSeasonNumber()!!){
                this.seasons.add(index, currentSeason)
                return index
            }
        }
        this.seasons.add(currentSeason)
        return this.seasons.size
    }

    fun updateSeason(currentSeason: SeasonModel): Int {
        this.seasons.forEachIndexed { index, season ->
            if(season.id == currentSeason.id) {
                this.seasons[index] = currentSeason
                return index
            }
        }
        return 0
    }
}

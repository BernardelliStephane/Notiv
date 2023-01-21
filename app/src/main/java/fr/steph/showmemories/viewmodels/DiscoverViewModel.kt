package fr.steph.showmemories.viewmodels

import androidx.lifecycle.*
import androidx.paging.cachedIn
import fr.steph.showmemories.models.tmdbmodels.TmdbTv
import fr.steph.showmemories.repository.TmdbShowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.*

class DiscoverViewModel(private val repo: TmdbShowRepository = TmdbShowRepository()) : ViewModel() {

    private var currentLanguage = "en-US"
    private val currentQuery = MutableStateFlow("")

    val tmdbTv: MutableSharedFlow<Resource> = MutableSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val shows = currentQuery.flatMapLatest {
        if (it.isBlank()) getTrendingShows(currentLanguage).cachedIn(viewModelScope)
        else searchShows(it, currentLanguage).cachedIn(viewModelScope)
    }

    private fun getTrendingShows(language: String) =
        repo.getTrendingShows(language)


    private fun searchShows(query: String, language: String) =
        repo.searchShows(query, language)

    fun getShowInfo(id: Int) = viewModelScope.launch {
        tmdbTv.emit(Resource.Loading)
        val response = repo.getShowInfo(id)
        tmdbTv.emit(handleResponse(response))
    }

    private fun handleResponse(response: Response<TmdbTv>) : Resource {
        if(response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
            return Resource.Error(response.message())
        }
        return Resource.Error(response.message())
    }

    fun queryChanged(query: String) = viewModelScope.launch {
        currentQuery.emit(query)
    }

    fun setLanguage(language: Locale) {
        currentLanguage = language.language
    }

    sealed class Resource {
        object Loading: Resource()
        data class Error(val message: String): Resource()
        data class Success(val data: TmdbTv): Resource()
    }
}
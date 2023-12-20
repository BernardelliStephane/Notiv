package fr.steph.showmemories.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.steph.showmemories.form_presentation.FilterFormEvent
import fr.steph.showmemories.model.ShowModel
import kotlinx.coroutines.launch
import me.xdrop.fuzzywuzzy.FuzzySearch

class FilterViewModel : ViewModel() {
    private val _alphabeticalFilter = MutableLiveData<Boolean?>()
    val alphabeticalFilter: LiveData<Boolean?> = _alphabeticalFilter

    private val _watchDateFilter = MutableLiveData<Boolean?>()
    val watchDateFilter: LiveData<Boolean?> = _watchDateFilter

    private val _noteFilter = MutableLiveData<Boolean?>()
    val noteFilter: LiveData<Boolean?> = _noteFilter

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    private val _showList = MutableLiveData<List<ShowModel>>(emptyList())
    val showList: LiveData<List<ShowModel>> = _showList

    private val _displayList = MutableLiveData<List<ShowModel>>(emptyList())
    val displayList: LiveData<List<ShowModel>> = _displayList

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun setList(shows: List<ShowModel>) {
        _showList.value = shows
        performFiltering()
    }

    fun onEvent(event: FilterFormEvent) {
        when (event) {
            is FilterFormEvent.AlphabeticalIconClicked ->
                updateFilters(_alphabeticalFilter, _watchDateFilter, _noteFilter)

            is FilterFormEvent.WatchDateIconClicked ->
                updateFilters(_watchDateFilter, _alphabeticalFilter, _noteFilter)

            is FilterFormEvent.NoteIconClicked ->
                updateFilters(_noteFilter, _alphabeticalFilter, _watchDateFilter)

            is FilterFormEvent.QueryChangedEvent -> {
                _loading.postValue(true)
                _query.value = event.query
                performFiltering()
            }
        }
    }

    private fun updateFilters(filterA: MutableLiveData<Boolean?>, filterB: MutableLiveData<Boolean?>, filterC: MutableLiveData<Boolean?>) {
        filterA.value = when (filterA.value) {
            null -> true
            true -> false
            false -> null
        }
        filterB.value?.let { filterB.value = null }
        filterC.value?.let { filterC.value = null }
        performFiltering()
    }

    @SuppressLint("NewApi")
    private fun performSorting(filteredList: ArrayList<ShowModel>) {
        if (filteredList.isEmpty()){
            _loading.postValue(false)
            return _displayList.postValue(emptyList())
        }

        alphabeticalFilter.value?.let {
            if (it) filteredList.sortBy { show -> show.name.lowercase() }
            else filteredList.sortByDescending { show -> show.name.lowercase() }
            return _displayList.postValue(filteredList)
        }
        watchDateFilter.value?.let {
            if (it) filteredList.sortBy { show -> show.watchDate }
            else filteredList.sortByDescending { show -> show.watchDate }
            return _displayList.postValue(filteredList)
        }
        noteFilter.value?.let {
            if (it) filteredList.sortBy { show -> show.note }
            else filteredList.sortByDescending { show -> show.note }
            return _displayList.postValue(filteredList)
        }
        _loading.postValue(false)
        _displayList.postValue(filteredList)
    }

    private fun performFiltering() {
        val filteredList = ArrayList<ShowModel>(emptyList())
        viewModelScope.launch {
            if (query.value!!.isEmpty()) filteredList.addAll(showList.value!!)
            else {
                val filterPattern = query.value!!.lowercase().trim()
                for (show in showList.value!!) {
                    val names = show.alternateNames + show.name
                    for (name in names) {
                        val ratio = FuzzySearch.partialRatio(filterPattern, name.lowercase())
                        val tokenSet = FuzzySearch.tokenSetPartialRatio(filterPattern, name.lowercase())
                        val tokenSort = FuzzySearch.tokenSortPartialRatio(filterPattern, name.lowercase())
                        if (name.length + 3 > filterPattern.length && (ratio > 75 || tokenSet > 75 || tokenSort > 75)) {
                            filteredList.add(show)
                            break
                        }
                    }
                }
            }
            performSorting(filteredList)
        }
    }
}
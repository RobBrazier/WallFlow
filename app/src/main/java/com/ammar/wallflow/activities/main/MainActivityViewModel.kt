package com.ammar.wallflow.activities.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.wallflow.data.db.entity.search.toSavedSearch
import com.ammar.wallflow.data.db.entity.search.toSearch
import com.ammar.wallflow.data.preferences.Theme
import com.ammar.wallflow.data.repository.AppPreferencesRepository
import com.ammar.wallflow.data.repository.GlobalErrorsRepository
import com.ammar.wallflow.data.repository.GlobalErrorsRepository.GlobalError
import com.ammar.wallflow.data.repository.SavedSearchRepository
import com.ammar.wallflow.data.repository.SearchHistoryRepository
import com.ammar.wallflow.extensions.trimAll
import com.ammar.wallflow.model.search.RedditSearch
import com.ammar.wallflow.model.search.SavedSearch
import com.ammar.wallflow.model.search.Search
import com.ammar.wallflow.model.search.WallhavenSearch
import com.ammar.wallflow.model.search.getSupportingText
import com.ammar.wallflow.ui.common.Suggestion
import com.ammar.wallflow.ui.common.mainsearch.MainSearchBar
import com.github.materiiapps.partial.Partialize
import com.github.materiiapps.partial.getOrElse
import com.github.materiiapps.partial.getOrNull
import com.github.materiiapps.partial.partial
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val application: Application,
    private val globalErrorsRepository: GlobalErrorsRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val savedSearchRepository: SavedSearchRepository,
    appPreferencesRepository: AppPreferencesRepository,
) : AndroidViewModel(application) {
    private val localUiState = MutableStateFlow(MainUiStatePartial())

    val uiState = combine(
        localUiState,
        searchHistoryRepository.getAll(),
        globalErrorsRepository.errors,
        savedSearchRepository.observeAll(),
        appPreferencesRepository.appPreferencesFlow,
    ) { local, searchHistory, errors, savedSearchEntities, appPreferences ->
        val localQuery = local.searchBarSearch.getOrNull()?.query?.trimAll()?.lowercase() ?: ""
        local.merge(
            MainUiState(
                searchBarSuggestions = searchHistory
                    .filter {
                        localQuery.isBlank() ||
                            it.query.trimAll().lowercase().contains(localQuery)
                    }
                    .map { s ->
                        when (val search = s.toSearch()) {
                            is WallhavenSearch -> Suggestion(
                                value = search,
                                headline = s.query,
                                supportingText = search.getSupportingText(application),
                            )
                            is RedditSearch -> TODO()
                        }
                    },
                globalErrors = errors,
                savedSearches = savedSearchEntities.map { entity ->
                    entity.toSavedSearch()
                },
                theme = appPreferences.lookAndFeelPreferences.theme,
                searchBarShowNSFW = appPreferences.wallhavenApiKey.isNotBlank(),
                showLocalTab = appPreferences.lookAndFeelPreferences.showLocalTab,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(),
    )

    fun dismissGlobalError(error: GlobalError) = globalErrorsRepository.removeError(error)

    fun setSearchBarActive(active: Boolean) = localUiState.update {
        it.copy(searchBarActive = partial(active))
    }

    fun setSearchBarSearch(search: Search) = localUiState.update {
        it.copy(searchBarSearch = partial(search))
    }

    fun onSearch(search: Search) {
        localUiState.update { it.copy(searchBarSearch = partial(search)) }
        viewModelScope.launch {
            delay(1000) // delay for better ux
            searchHistoryRepository.addSearch(search)
        }
    }

    fun setShowSearchBarFilters(show: Boolean) = localUiState.update {
        it.copy(showSearchBarFilters = partial(show))
    }

    fun setShowSearchBarSuggestionDeleteRequest(search: Search?) = localUiState.update {
        it.copy(searchBarDeleteSuggestion = partial(search))
    }

    fun onSearchBarQueryChange(query: String) = localUiState.update {
        val updated = when (
            val currentSearch = it.searchBarSearch.getOrElse { MainSearchBar.Defaults.search }
        ) {
            is RedditSearch -> currentSearch.copy(
                query = query,
            )
            is WallhavenSearch -> currentSearch.copy(
                query = query,
            )
        }
        it.copy(searchBarSearch = partial(updated))
    }

    fun deleteSearch(search: Search) = viewModelScope.launch {
        searchHistoryRepository.deleteSearch(search)
        localUiState.update { it.copy(searchBarDeleteSuggestion = partial(null)) }
    }

    fun showSaveSearchAsDialog(search: Search? = null) = localUiState.update {
        it.copy(saveSearchAsSearch = partial(search))
    }

    fun saveSearchAs(name: String, search: Search) = viewModelScope.launch {
        savedSearchRepository.upsert(
            SavedSearch(
                name = name,
                search = search,
            ),
        )
    }

    fun showSavedSearches(show: Boolean = true) = localUiState.update {
        it.copy(showSavedSearchesDialog = partial(show))
    }
}

@Partialize
data class MainUiState(
    val globalErrors: List<GlobalError> = emptyList(),
    val searchBarActive: Boolean = false,
    val searchBarSearch: Search = MainSearchBar.Defaults.search,
    val searchBarSuggestions: List<Suggestion<Search>> = emptyList(),
    val showSearchBarFilters: Boolean = false,
    val searchBarDeleteSuggestion: Search? = null,
    val saveSearchAsSearch: Search? = null,
    val showSavedSearchesDialog: Boolean = false,
    val savedSearches: List<SavedSearch> = emptyList(),
    val theme: Theme = Theme.SYSTEM,
    val searchBarShowNSFW: Boolean = false,
    val showLocalTab: Boolean = true,
)

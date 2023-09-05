package com.ammar.wallflow.ui.screens.favorites

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ammar.wallflow.data.db.entity.toFavorite
import com.ammar.wallflow.data.preferences.LayoutPreferences
import com.ammar.wallflow.data.repository.AppPreferencesRepository
import com.ammar.wallflow.data.repository.FavoritesRepository
import com.ammar.wallflow.model.Favorite
import com.ammar.wallflow.model.Source
import com.ammar.wallflow.model.wallhaven.WallhavenWallpaper
import com.github.materiiapps.partial.Partialize
import com.github.materiiapps.partial.partial
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {
    val favoriteWallpapers = favoritesRepository.favoriteWallpapersPager().cachedIn(viewModelScope)
    private val localUiState = MutableStateFlow(FavoritesUiStatePartial())

    val uiState = combine(
        localUiState,
        appPreferencesRepository.appPreferencesFlow,
        favoritesRepository.observeAll(),
    ) { local, appPreferences, favorites ->
        local.merge(
            FavoritesUiState(
                blurSketchy = appPreferences.blurSketchy,
                blurNsfw = appPreferences.blurNsfw,
                layoutPreferences = appPreferences.lookAndFeelPreferences.layoutPreferences,
                favorites = favorites.map { it.toFavorite() }.toImmutableList(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FavoritesUiState(),
    )

    fun setSelectedWallpaper(wallhavenWallpaper: WallhavenWallpaper) = localUiState.update {
        it.copy(selectedWallhavenWallpaper = partial(wallhavenWallpaper))
    }

    fun toggleFavorite(wallhavenWallpaper: WallhavenWallpaper) = viewModelScope.launch {
        favoritesRepository.toggleFavorite(
            sourceId = wallhavenWallpaper.id,
            source = Source.WALLHAVEN,
        )
    }
}

@Stable
@Partialize
data class FavoritesUiState(
    val blurSketchy: Boolean = false,
    val blurNsfw: Boolean = false,
    val selectedWallhavenWallpaper: WallhavenWallpaper? = null,
    val layoutPreferences: LayoutPreferences = LayoutPreferences(),
    val favorites: ImmutableList<Favorite> = persistentListOf(),
)

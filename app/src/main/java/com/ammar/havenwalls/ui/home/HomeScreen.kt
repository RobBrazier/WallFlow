package com.ammar.havenwalls.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ammar.havenwalls.R
import com.ammar.havenwalls.extensions.findActivity
import com.ammar.havenwalls.extensions.rememberLazyStaggeredGridState
import com.ammar.havenwalls.extensions.search
import com.ammar.havenwalls.extensions.toDp
import com.ammar.havenwalls.model.Search
import com.ammar.havenwalls.model.Tag
import com.ammar.havenwalls.model.TagSearchMeta
import com.ammar.havenwalls.model.Wallpaper
import com.ammar.havenwalls.model.wallpaper1
import com.ammar.havenwalls.model.wallpaper2
import com.ammar.havenwalls.ui.appCurrentDestinationAsState
import com.ammar.havenwalls.ui.common.LocalSystemBarsController
import com.ammar.havenwalls.ui.common.SearchBar
import com.ammar.havenwalls.ui.common.WallpaperFiltersModalBottomSheet
import com.ammar.havenwalls.ui.common.WallpaperStaggeredGrid
import com.ammar.havenwalls.ui.common.bottomWindowInsets
import com.ammar.havenwalls.ui.common.bottombar.BottomBarController
import com.ammar.havenwalls.ui.common.bottombar.LocalBottomBarController
import com.ammar.havenwalls.ui.common.mainsearch.LocalMainSearchBarController
import com.ammar.havenwalls.ui.common.mainsearch.MainSearchBarState
import com.ammar.havenwalls.ui.common.navigation.TwoPaneNavigation
import com.ammar.havenwalls.ui.common.navigation.TwoPaneNavigation.Mode
import com.ammar.havenwalls.ui.common.topWindowInsets
import com.ammar.havenwalls.ui.destinations.SettingsScreenDestination
import com.ammar.havenwalls.ui.destinations.WallpaperScreenDestination
import com.ammar.havenwalls.ui.theme.HavenWallsTheme
import com.ammar.havenwalls.ui.wallpaper.WallpaperScreenNavArgs
import com.ammar.havenwalls.ui.wallpaper.WallpaperViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import kotlinx.coroutines.flow.flowOf

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
)
@RootNavGraph(start = true)
@Destination(
    navArgsDelegate = HomeScreenNavArgs::class,
)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    twoPaneController: TwoPaneNavigation.Controller,
    wallpaperViewModel: WallpaperViewModel,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wallpapers = viewModel.wallpapers.collectAsLazyPagingItems()
    val gridState = wallpapers.rememberLazyStaggeredGridState()
    val refreshing = wallpapers.loadState.refresh == LoadState.Loading
    val expandedFab by remember(gridState.firstVisibleItemIndex) {
        derivedStateOf { gridState.firstVisibleItemIndex == 0 }
    }
    val refreshState = rememberPullRefreshState(
        // refreshing = uiState.wallpapersLoading,
        refreshing = false,
        onRefresh = {
            wallpapers.refresh()
            viewModel.refresh()
        },
    )
    val filtersBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val searchBarController = LocalMainSearchBarController.current
    val bottomBarController = LocalBottomBarController.current
    val systemBarsController = LocalSystemBarsController.current
    val (startPadding, bottomPadding) = getStartBottomPadding(bottomBarController)
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context.findActivity())
    val isExpanded = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
    val currentPane2Destination by twoPaneController.pane2NavHostController.appCurrentDestinationAsState()
    val isTwoPaneMode = twoPaneController.paneMode.value == Mode.TWO_PANE

    LaunchedEffect(refreshing) {
        viewModel.setWallpapersLoading(refreshing)
    }

    LaunchedEffect(Unit) {
        systemBarsController.reset()
        bottomBarController.update { it.copy(visible = true) }
    }

    LaunchedEffect(uiState.search, uiState.isHome) {
        searchBarController.update {
            MainSearchBarState(
                visible = true,
                overflowIcon = if (uiState.isHome) {
                    {
                        SearchBarOverflowMenu(
                            items = listOf(
                                MenuItem(
                                    text = stringResource(R.string.settings),
                                    value = "settings",
                                )
                            ),
                            onItemClick = {
                                if (it.value == "settings") {
                                    twoPaneController.navigate(SettingsScreenDestination) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                } else null,
                search = uiState.search,
                onSearch = { twoPaneController.pane1NavHostController.search(it) }
            )
        }
    }

    LaunchedEffect(uiState.selectedWallpaper) {
        val navArgs = WallpaperScreenNavArgs(
            wallpaperId = uiState.selectedWallpaper?.id,
            thumbUrl = uiState.selectedWallpaper?.thumbs?.original,
        )
        if (!isExpanded) {
            return@LaunchedEffect
        }
        twoPaneController.setPaneMode(Mode.TWO_PANE)
        if (currentPane2Destination is WallpaperScreenDestination) {
            wallpaperViewModel.setWallpaperId(
                wallpaperId = navArgs.wallpaperId,
                thumbUrl = navArgs.thumbUrl,
            )
            return@LaunchedEffect
        }
        twoPaneController.navigatePane2(
            WallpaperScreenDestination(navArgs)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(topWindowInsets)
            .padding(top = SearchBar.Defaults.height)
            .pullRefresh(state = refreshState),
    ) {
        HomeScreenContent(
            gridState = gridState,
            contentPadding = PaddingValues(
                start = startPadding + 8.dp,
                end = 8.dp,
                bottom = bottomPadding + 8.dp,
            ),
            tags = if (uiState.isHome) uiState.tags else emptyList(),
            isTagsLoading = uiState.areTagsLoading,
            wallpapers = wallpapers,
            blurSketchy = uiState.blurSketchy,
            blurNsfw = uiState.blurNsfw,
            selectedWallpaper = uiState.selectedWallpaper,
            showSelection = isTwoPaneMode,
            onWallpaperClick = {
                if (isTwoPaneMode) {
                    viewModel.setSelectedWallpaper(it)
                    return@HomeScreenContent
                }
                // navigate to wallpaper screen
                twoPaneController.navigatePane1(
                    WallpaperScreenDestination(
                        wallpaperId = it.id,
                        thumbUrl = it.thumbs.original,
                    )
                )
            },
            onTagClick = {
                twoPaneController.pane1NavHostController.search(
                    Search(
                        query = "id:${it.id}",
                        meta = TagSearchMeta(it),
                    )
                )
            }
        )

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            // refreshing = uiState.wallpapersLoading,
            refreshing = false,
            state = refreshState,
        )

        if (uiState.isHome) {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    // .windowInsetsPadding(bottomWindowInsets)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-16).dp, y = (-16).dp - bottomPadding),
                onClick = { viewModel.showFilters(true) },
                expanded = expandedFab,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_filter_alt_24),
                        contentDescription = stringResource(R.string.filters),
                    )
                },
                text = { Text(text = stringResource(R.string.filters)) },
            )
        }

        if (uiState.showFilters) {
            WallpaperFiltersModalBottomSheet(
                contentModifier = Modifier.windowInsetsPadding(bottomWindowInsets),
                bottomSheetState = filtersBottomSheetState,
                searchQuery = uiState.search.filters,
                title = "Home Filters",
                onSave = viewModel::updateQuery,
                onDismissRequest = { viewModel.showFilters(false) }
            )
        }
    }
}

@Composable
private fun getStartBottomPadding(bottomBarController: BottomBarController): Pair<Dp, Dp> {
    val bottomBarState by bottomBarController.state
    val startPadding = if (bottomBarState.isRail) bottomBarState.size.width.toDp() else 0.dp
    val bottomInsetsPadding = if (bottomBarState.isRail) {
        bottomWindowInsets.getBottom(LocalDensity.current).toDp()
    } else 0.dp
    val bottomNavBarPadding = if (bottomBarState.isRail) 0.dp else {
        bottomBarState.size.height.toDp()
    }
    val bottomPadding = bottomInsetsPadding + bottomNavBarPadding
    return Pair(startPadding, bottomPadding)
}

@Composable
internal fun HomeScreenContent(
    modifier: Modifier = Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    tags: List<Tag> = emptyList(),
    isTagsLoading: Boolean = false,
    wallpapers: LazyPagingItems<Wallpaper>,
    blurSketchy: Boolean = false,
    blurNsfw: Boolean = false,
    selectedWallpaper: Wallpaper? = null,
    showSelection: Boolean = false,
    onWallpaperClick: (wallpaper: Wallpaper) -> Unit = {},
    onTagClick: (tag: Tag) -> Unit = {},
) {
    WallpaperStaggeredGrid(
        modifier = modifier,
        state = gridState,
        contentPadding = contentPadding,
        wallpapers = wallpapers,
        blurSketchy = blurSketchy,
        blurNsfw = blurNsfw,
        header = {
            if (tags.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    PopularTagsRow(
                        tags = tags,
                        loading = isTagsLoading,
                        onTagClick = onTagClick,
                    )
                }
            }
        },
        selectedWallpaper = selectedWallpaper,
        showSelection = showSelection,
        onWallpaperClick = onWallpaperClick,
    )
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    HavenWallsTheme {
        val wallpapers = flowOf(PagingData.from(listOf(wallpaper1, wallpaper2)))
        val pagingItems = wallpapers.collectAsLazyPagingItems()
        HomeScreenContent(tags = emptyList(), wallpapers = pagingItems)
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
private fun PortraitPreview() {
    HavenWallsTheme {
        val wallpapers = flowOf(PagingData.from(listOf(wallpaper1, wallpaper2)))
        val pagingItems = wallpapers.collectAsLazyPagingItems()
        HomeScreenContent(tags = emptyList(), wallpapers = pagingItems)
    }
}

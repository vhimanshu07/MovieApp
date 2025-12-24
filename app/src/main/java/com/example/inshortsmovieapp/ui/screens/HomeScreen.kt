package com.example.inshortsmovieapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inshortsmovieapp.ui.model.Movie
import com.example.inshortsmovieapp.viewModels.HomeScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Int) -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎬 Inshorts Movie",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if ((uiState.trendingError != null || uiState.nowPlayingError != null) &&
                    (uiState.trendingMovies.isNotEmpty() || uiState.nowPlayingMovies.isNotEmpty())
                ) {
                    item {
                        OfflineMessage()
                    }
                }
                if (uiState.trendingMovies.isEmpty() && uiState.nowPlayingMovies.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No Internet please check connection",
                            icon = Icons.Default.Home
                        )
                    }
                }
                if (uiState.trendingMovies.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Trending Movies")
                    }
                }
                item {
                    if (uiState.isLoadingTrending && uiState.trendingMovies.isEmpty()) {
                        LoadingRow()
                    } else {
                        PaginatedMovieRow(
                            movies = uiState.trendingMovies,
                            onMovieClick = { onMovieClick(it.id) },
                            onBookmarkClick = { viewModel.toggleBookmark(it) },
                            onLoadMore = { viewModel.loadMoreTrendingMovies() },
                            isLoadingMore = uiState.isLoadingMoreTrending,
                            canLoadMore = uiState.canLoadMoreTrending
                        )
                    }
                }
                if (uiState.nowPlayingMovies.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "Now Playing")
                    }
                }
                item {
                    if (uiState.isLoadingNowPlaying && uiState.nowPlayingMovies.isEmpty()) {
                        LoadingRow()
                    } else {
                        PaginatedMovieRow(
                            movies = uiState.nowPlayingMovies,
                            onMovieClick = { onMovieClick(it.id) },
                            onBookmarkClick = { viewModel.toggleBookmark(it) },
                            onLoadMore = { viewModel.loadMoreNowPlayingMovies() },
                            isLoadingMore = uiState.isLoadingMoreNowPlaying,
                            canLoadMore = uiState.canLoadMoreNowPlaying
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginatedMovieRow(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onBookmarkClick: (Movie) -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    canLoadMore: Boolean
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3 && canLoadMore && !isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(movies, key = { it.id }) { movie ->
            MoviePosterCard(
                movie = movie,
                onMovieClick = onMovieClick,
                onBookmarkClick = onBookmarkClick
            )
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

}
package com.example.inshortsmovieapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.inshortsmovieapp.ui.screens.BookMarkScreen
import com.example.inshortsmovieapp.ui.screens.HomeScreen
import com.example.inshortsmovieapp.ui.screens.MovieDetailScreen
import com.example.inshortsmovieapp.ui.screens.SearchScreen

sealed class Route(val name: String) {
    object Home : Route("home")
    object Search : Route("search")
    object Bookmark : Route("bookmark")
    data object MovieDetail : Route("movie/{movieId}") {
        fun createRoute(movieId: Int) = "movie/$movieId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier,
    startDestination: String = Route.Home.name
) {
    NavHost(navController, startDestination = startDestination, modifier) {
        composable(Route.Home.name) {
            HomeScreen(onMovieClick = { movieId ->
                navController.navigate(Route.MovieDetail.createRoute(movieId))
            })
        }
        composable(Route.Search.name) {
            SearchScreen(onMovieClick = { movieId ->
                navController.navigate(Route.MovieDetail.createRoute(movieId))
            })
        }
        composable(Route.Bookmark.name) {
            BookMarkScreen(onMovieClick = { movieId ->
                navController.navigate(Route.MovieDetail.createRoute(movieId))
            })
        }
        composable(
            Route.MovieDetail.name, arguments = listOf(
                navArgument("movieId") {
                    type = NavType.IntType
                }
            )) {
            MovieDetailScreen(onBackClick = {
                navController.popBackStack()
            })
        }
    }

}



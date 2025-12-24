package com.example.inshortsmovieapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.inshortsmovieapp.ui.navigation.BottomNavBar
import com.example.inshortsmovieapp.ui.navigation.NavGraph
import com.example.inshortsmovieapp.ui.theme.InshortsMovieAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InshortsMovieAppTheme {
                SetUpNavigation()
            }
        }
    }

    @Composable
    private fun SetUpNavigation() {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = {
                BottomNavBar(navController)
            },
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            NavGraph(
                navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
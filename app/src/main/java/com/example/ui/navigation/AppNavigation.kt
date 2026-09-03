package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreateMusicScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HelpLegalScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PricingScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.SongDetailsScreen
import com.example.ui.theme.DivBackground
import com.example.ui.theme.DivBorder
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivGradientBrand
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import com.example.ui.theme.DivPurpleLight
import com.example.ui.theme.DivSurfaceDark
import com.example.ui.theme.DivTextMuted
import com.example.ui.theme.DivTextSecondary
import com.example.ui.viewmodel.MainViewModel

object Routes {
    const val HOME = "home"
    const val CREATE = "create"
    const val LIBRARY = "library"
    const val EXPLORE = "explore"
    const val PROFILE = "profile"
    const val PRICING = "pricing"
    const val ADMIN = "admin"
    const val HELP = "help"
    const val AUTH = "auth"
    const val SONG_DETAILS = "song_details/{songId}"
    fun songDetails(songId: String) = "song_details/$songId"
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentPlayingSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    val playbackProgress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f

    val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.CREATE, Routes.LIBRARY, Routes.EXPLORE, Routes.PROFILE)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DivBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                Column {
                    // Persistent Mini Player above the bottom bar
                    MiniPlayerBar(
                        currentSong = currentPlayingSong,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        progress = playbackProgress,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onClickBar = {
                            currentPlayingSong?.let {
                                navController.navigate(Routes.songDetails(it.id))
                            }
                        }
                    )

                    NavigationBar(
                        containerColor = DivSurfaceDark,
                        contentColor = Color.White,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .border(1.dp, DivBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .testTag("main_bottom_nav_bar")
                    ) {
                        // 1. Home
                        NavigationBarItem(
                            selected = currentRoute == Routes.HOME,
                            onClick = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(20.dp)) },
                            label = { Text("Home", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DivCyan,
                                selectedTextColor = DivCyan,
                                unselectedIconColor = DivTextSecondary,
                                unselectedTextColor = DivTextSecondary,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_item_home")
                        )

                        // 2. Explore
                        NavigationBarItem(
                            selected = currentRoute == Routes.EXPLORE,
                            onClick = {
                                navController.navigate(Routes.EXPLORE) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Explore, contentDescription = "Explore", modifier = Modifier.size(20.dp)) },
                            label = { Text("Explore", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DivCyan,
                                selectedTextColor = DivCyan,
                                unselectedIconColor = DivTextSecondary,
                                unselectedTextColor = DivTextSecondary,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_item_explore")
                        )

                        // 3. Create (Signature Highlighted Button)
                        NavigationBarItem(
                            selected = currentRoute == Routes.CREATE,
                            onClick = {
                                navController.navigate(Routes.CREATE) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(DivGradientBrand),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Create",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            label = { Text("Create", fontSize = 10.sp, color = DivPink) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_item_create")
                        )

                        // 4. Library
                        NavigationBarItem(
                            selected = currentRoute == Routes.LIBRARY,
                            onClick = {
                                navController.navigate(Routes.LIBRARY) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library", modifier = Modifier.size(20.dp)) },
                            label = { Text("Library", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DivCyan,
                                selectedTextColor = DivCyan,
                                unselectedIconColor = DivTextSecondary,
                                unselectedTextColor = DivTextSecondary,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_item_library")
                        )

                        // 5. Profile
                        NavigationBarItem(
                            selected = currentRoute == Routes.PROFILE,
                            onClick = {
                                navController.navigate(Routes.PROFILE) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(20.dp)) },
                            label = { Text("Profile", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DivCyan,
                                selectedTextColor = DivCyan,
                                unselectedIconColor = DivTextSecondary,
                                unselectedTextColor = DivTextSecondary,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_item_profile")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCreate = { navController.navigate(Routes.CREATE) },
                    onNavigateToExplore = { navController.navigate(Routes.EXPLORE) },
                    onNavigateToLibrary = { navController.navigate(Routes.LIBRARY) },
                    onNavigateToPricing = { navController.navigate(Routes.PRICING) },
                    onSongClick = { songId -> navController.navigate(Routes.songDetails(songId)) }
                )
            }

            composable(Routes.CREATE) {
                CreateMusicScreen(
                    viewModel = viewModel,
                    onSongGenerated = { newSongId ->
                        navController.navigate(Routes.songDetails(newSongId))
                    }
                )
            }

            composable(Routes.LIBRARY) {
                LibraryScreen(
                    viewModel = viewModel,
                    onNavigateToCreate = { navController.navigate(Routes.CREATE) },
                    onSongClick = { songId -> navController.navigate(Routes.songDetails(songId)) }
                )
            }

            composable(Routes.EXPLORE) {
                ExploreScreen(
                    viewModel = viewModel,
                    onSongClick = { songId -> navController.navigate(Routes.songDetails(songId)) }
                )
            }

            composable(Routes.PROFILE) {
                ProfileSettingsScreen(
                    viewModel = viewModel,
                    onNavigateToPricing = { navController.navigate(Routes.PRICING) },
                    onNavigateToAdmin = { navController.navigate(Routes.ADMIN) },
                    onNavigateToHelp = { navController.navigate(Routes.HELP) },
                    onLogout = { navController.navigate(Routes.AUTH) }
                )
            }

            composable(Routes.PRICING) {
                PricingScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.ADMIN) {
                AdminScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.HELP) {
                HelpLegalScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.AUTH) {
                AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = { navController.navigate(Routes.HOME) }
                )
            }

            composable(
                route = Routes.SONG_DETAILS,
                arguments = listOf(navArgument("songId") { type = NavType.StringType })
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getString("songId") ?: ""
                SongDetailsScreen(
                    songId = songId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onVariationClick = { navController.navigate(Routes.CREATE) }
                )
            }
        }
    }
}

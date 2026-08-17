package com.example.prac

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object UserData {
    var userName by mutableStateOf("")
    var registeredEmail by mutableStateOf("")
    var registeredPassword by mutableStateOf("")
    var isRegistered by mutableStateOf(false)
    var isDarkMode by mutableStateOf(false)
}


data class Song(val id: String, val title: String, val artist: String, val imageUrl: String, val songUrl: String)


val CyanPrimary = Color(0xFF00BCD4)
val CyanLight = Color(0xFFE0F7FA)
val CyanDeep = Color(0xFF006064)

val songLibrary = listOf(
    Song("1", "Maatikkinaaru", "Leon James", "https://picsum.photos/id/1/400", "https://docs.google.com/uc?export=download&id=10hrK4Bnd3R40bM8nwH94j9VXOF0t3XUk"),
    Song("2", "Maname Maname", "Leon James", "https://picsum.photos/id/2/400", "https://docs.google.com/uc?export=download&id=1ch4spOckOOaMlDHU7dH7YCzph8zippXQ"),
    Song("3", "Vazhithunaiye", "Leon James", "https://picsum.photos/id/3/400", "https://docs.google.com/uc?export=download&id=1Ijj6U1qeSJw7WDBLReE6MBHwc5ZqA0dS"),
    Song("4", "Yendi Vittu Pona", "Leon James", "https://picsum.photos/id/4/400", "https://docs.google.com/uc?export=download&id=1fmT8QYTKUWqCerZRY6Rke3RK933S5bSn"),
    Song("5", "Iraivaa", "Leon James", "https://picsum.photos/id/5/400", "https://docs.google.com/uc?export=download&id=1JAfwP2PkAIu2Jf83wuTuDOZwhdfuhjm_")
)



class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val exoPlayer = ExoPlayer.Builder(application).build()
    var currentSong by mutableStateOf<Song?>(null)
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var totalDuration by mutableLongStateOf(0L)
    val likedSongs = mutableStateListOf<Song>()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
                totalDuration = exoPlayer.duration.coerceAtLeast(0L)
            }
        })
    }

    fun playSong(song: Song) {
        if (currentSong?.id != song.id) {
            currentSong = song
            exoPlayer.setMediaItem(MediaItem.fromUri(song.songUrl))
            exoPlayer.prepare()
        }
        exoPlayer.play()
    }

    fun togglePlayPause() { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() }
    fun seekTo(pos: Long) = exoPlayer.seekTo(pos)
    fun toggleLike(song: Song) { if (likedSongs.contains(song)) likedSongs.remove(song) else likedSongs.add(song) }

    suspend fun updateProgress() {
        while (true) {
            if (isPlaying) { currentPosition = exoPlayer.currentPosition }
            delay(500)
        }
    }

    override fun onCleared() { super.onCleared(); exoPlayer.release() }
}



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colorScheme = if (UserData.isDarkMode) {
                darkColorScheme(primary = CyanPrimary, surface = Color(0xFF121212), background = Color(0xFF121212))
            } else {
                lightColorScheme(primary = CyanPrimary, background = Color(0xFFF7FDFF))
            }

            MaterialTheme(colorScheme = colorScheme) {
                val viewModel: MusicViewModel = viewModel()
                LaunchedEffect(Unit) { viewModel.updateProgress() }
                val navController = rememberNavController()

                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController, startDestination = "login") {
                        composable("login") { LoginScreen(navController) }
                        composable("signup") { SignupScreen(navController) }
                        composable("main") { MainShell(navController, viewModel) }
                    }
                }
            }
        }
    }
}


@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("ExpertMusic", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = CyanPrimary)
            Text("Login to stream", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pass, onValueChange = { pass = it },
                label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { passVisible = !passVisible }) { Icon(if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = CyanPrimary) } }
            )
            Button(
                onClick = {
                    if (UserData.isRegistered && email == UserData.registeredEmail && pass == UserData.registeredPassword) {
                        navController.navigate("main") { popUpTo("login") { inclusive = true } }
                    } else if (!UserData.isRegistered) {
                        scope.launch { snackbarHostState.showSnackbar("No account found. Please Sign Up.") }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Invalid email or password.") }
                    }
                },
                Modifier.fillMaxWidth().padding(top = 24.dp).height(50.dp)
            ) { Text("Log In") }
            TextButton(onClick = { navController.navigate("signup") }, Modifier.align(Alignment.CenterHorizontally)) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}


@Composable
fun SignupScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = pass, onValueChange = { pass = it },
            label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = { passVisible = !passVisible }) { Icon(if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = CyanPrimary) } }
        )
        Button(
            onClick = {
                if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                    UserData.userName = name
                    UserData.registeredEmail = email
                    UserData.registeredPassword = pass
                    UserData.isRegistered = true
                    navController.navigate("login")
                }
            },
            Modifier.fillMaxWidth().padding(top = 24.dp).height(50.dp)
        ) { Text("Sign Up") }
    }
}


@Composable
fun MainShell(rootNav: NavHostController, viewModel: MusicViewModel) {
    val nestedNav = rememberNavController()
    var isExpanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(Modifier.fillMaxWidth().height(180.dp).background(CyanPrimary).padding(20.dp)) {
                    Column(Modifier.align(Alignment.BottomStart)) {
                        Surface(Modifier.size(60.dp), shape = CircleShape) { Icon(Icons.Default.AccountCircle, null, tint = CyanPrimary) }
                        Text(UserData.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(UserData.registeredEmail, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                }
                NavigationDrawerItem(label = { Text("Home") }, selected = false, onClick = { scope.launch { drawerState.close() }; nestedNav.navigate("home") }, icon = { Icon(Icons.Default.Home, null) })
                NavigationDrawerItem(label = { Text("Profile") }, selected = false, onClick = { scope.launch { drawerState.close() }; nestedNav.navigate("profile") }, icon = { Icon(Icons.Default.Person, null) })
                NavigationDrawerItem(label = { Text("Logout") }, selected = false, onClick = { rootNav.navigate("login") { popUpTo(0) } }, icon = { Icon(Icons.Default.ExitToApp, null) })
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                Column {
                    if (viewModel.currentSong != null) MiniPlayer(viewModel) { isExpanded = true }
                    NavigationBar {
                        val navBackStackEntry by nestedNav.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        listOf("home", "search", "library", "profile").forEach { route ->
                            NavigationBarItem(
                                icon = { Icon(when(route){ "home"->Icons.Default.Home; "search"->Icons.Default.Search; "library"->Icons.Default.Favorite; else->Icons.Default.Person }, null) },
                                selected = currentRoute == route,
                                onClick = { nestedNav.navigate(route) { popUpTo(nestedNav.graph.startDestinationId); launchSingleTop = true } }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                NavHost(nestedNav, startDestination = "home") {
                    composable("home") { HomeScreen(viewModel, drawerState, scope) }
                    composable("search") { SearchScreen(viewModel) }
                    composable("library") { LibraryScreen(viewModel) }
                    composable("profile") { ProfileScreen(nestedNav, rootNav, viewModel) }
                    composable("edit_profile") { EditProfileScreen(nestedNav) }
                }
            }
            AnimatedVisibility(isExpanded, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                FullPlayerView(viewModel) { isExpanded = false }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MusicViewModel, drawerState: DrawerState, scope: CoroutineScope) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FluxMusic", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Notes, "Menu", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { UserData.isDarkMode = !UserData.isDarkMode }) {
                        Icon(if (UserData.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, null, tint = CyanPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            item { Text("Discover Music", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CyanDeep, modifier = Modifier.padding(vertical = 16.dp)) }
            items(songLibrary) { song -> SongRow(song) { viewModel.playSong(song) } }
        }
    }
}

@Composable
fun ProfileScreen(nestedNav: NavHostController, rootNav: NavHostController, viewModel: MusicViewModel) {
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(Modifier.fillMaxWidth().height(200.dp).background(CyanPrimary), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(Modifier.size(80.dp), shape = CircleShape, color = Color.White) {
                        Icon(Icons.Default.AccountCircle, null, Modifier.fillMaxSize(), tint = CyanPrimary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(UserData.userName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(UserData.registeredEmail, fontSize = 14.sp, color = CyanLight)
                }
            }

            // Features List
            Column(Modifier.padding(24.dp)) {
                Text("Settings", fontWeight = FontWeight.Bold, color = CyanDeep)
                Spacer(Modifier.height(16.dp))

                ProfileMenuItem(Icons.Default.Edit, "Edit Profile") { nestedNav.navigate("edit_profile") }

                // Theme Toggle (Dark Mode Option)
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DarkMode, null, tint = CyanPrimary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Dark Mode", Modifier.weight(1f), fontSize = 16.sp)
                    Switch(checked = UserData.isDarkMode, onCheckedChange = { UserData.isDarkMode = it })
                }

                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null, tint = CyanPrimary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Notifications", Modifier.weight(1f), fontSize = 16.sp)
                    Switch(checked = isNotificationsEnabled, onCheckedChange = { isNotificationsEnabled = it })
                }

                ProfileMenuItem(Icons.Default.Security, "Privacy & Security") {
                    scope.launch { snackbarHostState.showSnackbar("Privacy settings enabled.") }
                }

                ProfileMenuItem(Icons.Default.Help, "Help & Support") {
                    scope.launch { snackbarHostState.showSnackbar("Support center offline.") }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { rootNav.navigate("login") { popUpTo(0) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EditProfileScreen(navController: NavHostController) {
    var name by remember { mutableStateOf(UserData.userName) }
    var email by remember { mutableStateOf(UserData.registeredEmail) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = CyanPrimary) }
        Text("Edit Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                UserData.userName = name
                UserData.registeredEmail = email
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Save Changes") }
    }
}

@Composable
fun SearchScreen(viewModel: MusicViewModel) {
    var q by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(q, { q = it }, placeholder = { Text("Search songs...") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null)})
        LazyColumn { items(songLibrary.filter { it.title.contains(q, true) }) { song -> SongRow(song) { viewModel.playSong(song) } } }
    }
}

@Composable
fun LibraryScreen(viewModel: MusicViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Liked Songs", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CyanDeep, modifier = Modifier.padding(bottom = 16.dp)) }
        items(viewModel.likedSongs) { song -> SongRow(song) { viewModel.playSong(song) } }
    }
}


@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = CyanPrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp)); Text(title, Modifier.weight(1f), fontSize = 16.sp)
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
fun SongRow(song: Song, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(song.imageUrl, null, Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(song.title, fontWeight = FontWeight.Bold)
            Text(song.artist, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.PlayArrow, null, tint = CyanPrimary.copy(alpha = 0.5f))
    }
}

@Composable
fun MiniPlayer(viewModel: MusicViewModel, onClick: () -> Unit) {
    val song = viewModel.currentSong ?: return
    Row(Modifier.fillMaxWidth().height(70.dp).background(CyanPrimary.copy(alpha = 0.1f)).clickable { onClick() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(song.imageUrl, null, Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(song.title, fontWeight = FontWeight.Bold, color = CyanDeep, maxLines = 1)
            Text(song.artist, fontSize = 12.sp, color = CyanPrimary)
        }
        IconButton(onClick = { viewModel.togglePlayPause() }) { Icon(if (viewModel.isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled, null, tint = CyanPrimary, modifier = Modifier.size(40.dp)) }
    }
}

@Composable
fun FullPlayerView(viewModel: MusicViewModel, onCollapse: () -> Unit) {
    val song = viewModel.currentSong ?: return
    val isLiked = viewModel.likedSongs.contains(song)
    BackHandler { onCollapse() }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onCollapse, Modifier.align(Alignment.Start)) { Icon(Icons.Default.KeyboardArrowDown, null, tint = CyanPrimary) }
        AsyncImage(song.imageUrl, null, Modifier.size(320.dp).clip(RoundedCornerShape(20.dp)).padding(top = 20.dp), contentScale = ContentScale.Crop)
        Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(song.title, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(song.artist, color = CyanPrimary, fontSize = 18.sp)
            }
            IconButton(onClick = { viewModel.toggleLike(song) }) { Icon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isLiked) Color.Red else Color.Gray, modifier = Modifier.size(32.dp)) }
        }
        Slider(
            value = viewModel.currentPosition.toFloat(),
            onValueChange = { viewModel.seekTo(it.toLong()) },
            valueRange = 0f..viewModel.totalDuration.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
        )
        Row(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SkipPrevious, null, Modifier.size(48.dp), tint = CyanPrimary)
            Box(Modifier.size(80.dp).background(CyanPrimary, CircleShape).clickable { viewModel.togglePlayPause() }, contentAlignment = Alignment.Center) { Icon(if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, Modifier.size(40.dp), tint = Color.White) }
            Icon(Icons.Default.SkipNext, null, Modifier.size(48.dp), tint = CyanPrimary)
        }
    }
}
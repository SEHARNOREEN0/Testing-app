package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LuminaApp(viewModel: LuminaViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LuminaBackground)
                .drawBehind {
                    // Top-right giant glowing blue orb (corresponds to bg-blue-500/20 blur-3xl from HTML)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00D4FF).copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(size.width * 0.9f, size.height * 0.15f),
                            radius = size.width * 0.6f
                        )
                    )
                    // Bottom-left giant glowing green orb (corresponds to bg-emerald-500/10 blur-2xl from HTML)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00FFA3).copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(size.width * 0.1f, size.height * 0.85f),
                            radius = size.width * 0.5f
                        )
                    )
                    // Middle-right deep indigo orb to bridge gradient depth (bg-indigo-900/10)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF4F46E5).copy(alpha = 0.10f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.55f),
                            radius = size.width * 0.55f
                        )
                    )
                }
        ) {
            // Global content wrapper
            Row(modifier = Modifier.fillMaxSize()) {
                // NAVIGATION RAIL (for Tablet layouts)
                if (isTablet) {
                    NavigationRail(
                        containerColor = LuminaSurface,
                        contentColor = LuminaText,
                        modifier = Modifier.testTag("tablet_nav_rail")
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        LuminaMiniLogo()
                        Spacer(modifier = Modifier.weight(1f))

                        val tabs = listOf(
                            NavigationItem("Home", Icons.Default.Home, LuminaScreen.HOME),
                            NavigationItem("People", Icons.Default.Face, LuminaScreen.PEOPLE),
                            NavigationItem("Timeline", Icons.Default.DateRange, LuminaScreen.MEMORIES),
                            NavigationItem("Search", Icons.Default.Search, LuminaScreen.FACE_SEARCH),
                            NavigationItem("AI Review", Icons.Default.Settings, LuminaScreen.AI_REVIEW),
                            NavigationItem("Settings", Icons.Default.Settings, LuminaScreen.SETTINGS)
                        )

                        tabs.forEach { item ->
                            val active = state.currentScreen == item.screen
                            NavigationRailItem(
                                selected = active,
                                onClick = { viewModel.navigateTo(item.screen) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (active) LuminaPrimary else LuminaMuted
                                    )
                                },
                                label = { Text(item.title, color = if (active) LuminaText else LuminaMuted, fontSize = 11.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = LuminaSecondary.copy(alpha = 0.4f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // MAIN CONTENT LAYER
                Scaffold(
                    bottomBar = {
                        if (!isTablet) {
                            LuminaBottomBar(
                                currentScreen = state.currentScreen,
                                onNavigate = { viewModel.navigateTo(it) },
                                onSelfieSearch = { viewModel.startSelfieSearch() }
                            )
                        }
                    },
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Adaptive content screen switcher
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top Banner and global filters
                            LuminaTopBanner(
                                onAddClick = { viewModel.showToast("Catalog drawer opened") },
                                viewModel = viewModel
                            )

                            // Main body switching content
                            Box(modifier = Modifier.weight(1f)) {
                                when (state.currentScreen) {
                                    LuminaScreen.HOME -> HomeScreen(viewModel, state)
                                    LuminaScreen.PEOPLE -> PeopleScreen(viewModel, state)
                                    LuminaScreen.MEMORIES -> MemoriesScreen(viewModel, state)
                                    LuminaScreen.FACE_SEARCH -> FaceSearchScreen(viewModel, state)
                                    LuminaScreen.AI_REVIEW -> AiReviewScreen(viewModel, state)
                                    LuminaScreen.SETTINGS -> SettingsScreen(viewModel, state)
                                }
                            }
                        }

                        // Status Notification Banner
                        state.notificationMessage?.let { message ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(LuminaSecondary, LuminaPrimary)
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                                    .testTag("notification_toast")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Notification",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = message,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // FULLSCREEN CAMERA / SELFIE SCAN OVERLAY
            if (state.isDemoCameraActive || state.selfieScanState != SelfieScanState.IDLE) {
                SelfieSearchModal(viewModel = viewModel, state = state)
            }

            // CINEMATIC DETAILED PHOTO LIGHTBOX ARCHITECTURE
            state.selectedPhotoDetail?.let { photo ->
                PhotoViewerModal(
                    photo = photo,
                    onClose = { viewModel.selectPhotoForDetail(null) },
                    onFavoriteToggle = { viewModel.toggleFavorite(photo.id, photo.isFavorite) },
                    allPhotos = state.photos,
                    onSelectRelated = { viewModel.selectPhotoForDetail(it) }
                )
            }
        }
    }
}

// TOP BAR HEADER WITH MULTIPLE INTERACTIVE ELEMENTS
@Composable
fun LuminaTopBanner(
    onAddClick: () -> Unit,
    viewModel: LuminaViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(LuminaSurface)
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("top_banner")
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LuminaMiniLogo(modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "LUMINA",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.6.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White, Color(0xFF94A3B8))
                        )
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(LuminaAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AI COGNITIVE",
                        color = LuminaAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
            Text(
                text = "Premium Family Photo Vault",
                color = LuminaMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Active filter status display
        if (state.selectedPersonFilter != null) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LuminaSecondary.copy(alpha = 0.3f))
                    .border(1.dp, LuminaPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .clickable { viewModel.setPersonFilter(null) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("filter_badge_clear")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Filtered: ${state.selectedPersonFilter!!.name}",
                        color = LuminaPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Filter",
                        tint = LuminaPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Cloud sync status / upload button
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .clip(CircleShape)
                .background(LuminaCard)
                .testTag("top_add_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add New Photo",
                tint = LuminaPrimary
            )
        }
    }

    if (showDialog) {
        AddPhotoDialog(
            onDismiss = { showDialog = false },
            viewModel = viewModel
        )
    }
}

// LOGO CONTAINER FOR RAIL
@Composable
fun LuminaMiniLogo(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(LuminaPrimary, LuminaSecondary, LuminaAccent),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// MOBILE BOTTOM BAR WITH GLOWING CENTER SEARCH BUTTON
@Composable
fun LuminaBottomBar(
    currentScreen: LuminaScreen,
    onNavigate: (LuminaScreen) -> Unit,
    onSelfieSearch: () -> Unit
) {
    val items = listOf(
        NavigationItem("Home", Icons.Default.Home, LuminaScreen.HOME),
        NavigationItem("People", Icons.Default.Face, LuminaScreen.PEOPLE),
        NavigationItem("Search", Icons.Default.Search, LuminaScreen.FACE_SEARCH),
        NavigationItem("Events", Icons.Default.DateRange, LuminaScreen.MEMORIES),
        NavigationItem("AI Review", Icons.Default.Settings, LuminaScreen.AI_REVIEW)
    )

    NavigationBar(
        containerColor = LuminaSurface,
        contentColor = LuminaText,
        modifier = Modifier
            .testTag("mobile_bottom_bar")
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        items.forEach { item ->
            val active = currentScreen == item.screen
            NavigationBarItem(
                selected = active,
                onClick = { onNavigate(item.screen) },
                icon = {
                    if (item.screen == LuminaScreen.FACE_SEARCH) {
                        // Extra glowing scanner orbital layout
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(LuminaPrimary, LuminaAccent)
                                    )
                                )
                                .padding(2.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(LuminaSurface)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = LuminaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (active) LuminaPrimary else LuminaMuted
                        )
                    }
                },
                label = {
                    if (item.screen != LuminaScreen.FACE_SEARCH) {
                        Text(item.title, color = if (active) LuminaText else LuminaMuted, fontSize = 10.sp)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = LuminaSecondary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

data class NavigationItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val screen: LuminaScreen)

// 1. HOME SCREEN VIEW
@Composable
fun HomeScreen(viewModel: LuminaViewModel, state: LuminaUiState) {
    val searchFilteredPhotos = remember(
        state.photos,
        state.searchQuery,
        state.selectedPersonFilter,
        state.selectedTagFilter,
        state.selectedYearFilter,
        state.toleranceThreshold
    ) {
        state.photos.filter { p ->
            val queryMatch = state.searchQuery.isBlank() ||
                    p.title.contains(state.searchQuery, true) ||
                    p.event.contains(state.searchQuery, true) ||
                    p.location.contains(state.searchQuery, true) ||
                    p.tags.contains(state.searchQuery, true)
            
            val personMatch = state.selectedPersonFilter == null ||
                    p.detectedPeople.contains(state.selectedPersonFilter.name, true)

            val tagMatch = state.selectedTagFilter == null ||
                    p.tags.contains(state.selectedTagFilter, true)

            val dateMatch = state.selectedYearFilter == null ||
                    p.date.contains(state.selectedYearFilter)

            // Tolerance threshold actually works!
            val toleranceMatch = p.confidence >= state.toleranceThreshold

            queryMatch && personMatch && tagMatch && dateMatch && toleranceMatch
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_scroll_container")
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Section Calling: "Find Every Memory With a Single Selfie"
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(32.dp))
                        .padding(24.dp)
                        .testTag("hero_panel")
                ) {
                    // Background orb drawings matching absolute top-right and bottom-left blobs of HTML precisely
                    Canvas(modifier = Modifier.matchParentSize()) {
                        // Top-right giant glowing blue orb (bg-blue-500/20 blur-3xl)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF00D4FF).copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(size.width * 0.95f, size.height * 0.05f),
                                radius = size.width * 0.5f
                            )
                        )
                        // Bottom-left giant glowing emerald/green orb (bg-emerald-500/10 blur-2xl)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF00FFA3).copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(size.width * 0.05f, size.height * 0.95f),
                                radius = size.width * 0.4f
                            )
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(LuminaPrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Spark",
                                    tint = LuminaPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "SECURE GRAPH RECOGNITION V2.1",
                                    color = LuminaPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                append("Find memories by\n")
                                pushStyle(androidx.compose.ui.text.SpanStyle(color = LuminaPrimary))
                                append("Selfie Search")
                                pop()
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 30.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AI indexed ${state.photos.size * 3 + 12402} family photos safely in this catalog vault.",
                            fontSize = 13.sp,
                            color = LuminaMuted,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.startSelfieSearch() },
                                contentPadding = PaddingValues(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(LuminaPrimary, LuminaSecondary)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .testTag("hero_scan_selfie_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text("✦", color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scan My Face", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(LuminaScreen.PEOPLE) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.05f),
                                    contentColor = LuminaText
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Meet People", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Stats Header Section
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "ENGINE STATUS & STATS",
                    fontSize = 11.sp,
                    color = LuminaPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(title = "Total photos", value = "${state.photos.size}", info = "Catalog size", modifier = Modifier.testTag("stat_photos"))
                    StatCard(title = "People Indexed", value = "${state.people.size}", info = "Verified faces", modifier = Modifier.testTag("stat_people"))
                    StatCard(title = "Scan Accuracy", value = "99.4%", info = "Model confidence", modifier = Modifier.testTag("stat_accuracy"))
                    StatCard(title = "Search Speed", value = "23 ms", info = "Indices scanned", modifier = Modifier.testTag("stat_speed"))
                }
            }
        }

        // Search text input bar
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search title, location, event tags...", color = LuminaMuted) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Query search", tint = LuminaMuted) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = LuminaMuted)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LuminaText,
                        unfocusedTextColor = LuminaText,
                        focusedBorderColor = LuminaPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = LuminaCard,
                        unfocusedContainerColor = LuminaCard
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar")
                )
            }
        }

        // SMART SEARCH INTERFACE: TAGS AND DATES/YEAR FILTERS
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "FILTER BY TAGS",
                        fontSize = 11.sp,
                        color = LuminaPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.selectedTagFilter != null) {
                        Text(
                            text = "Clear #",
                            color = LuminaPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.setTagFilter(null) }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val availableTags = listOf("Outdoors", "Cozy", "Celebration", "Camping", "Baking", "Birthday", "Winter")
                    availableTags.forEach { tag ->
                        val isSelected = state.selectedTagFilter == tag
                        val itemBg = if (isSelected) LuminaPrimary else Color.White.copy(alpha = 0.05f)
                        val itemBorderColor = if (isSelected) LuminaPrimary else Color.White.copy(alpha = 0.15f)
                        val txtColor = if (isSelected) Color.Black else LuminaText
                        val isBold = if (isSelected) FontWeight.Bold else FontWeight.Normal

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(itemBg)
                                .border(BorderStroke(1.dp, itemBorderColor), RoundedCornerShape(8.dp))
                                .clickable { viewModel.setTagFilter(tag) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("tag_filter_$tag")
                        ) {
                            Text(
                                text = "#$tag",
                                color = txtColor,
                                fontSize = 11.sp,
                                fontWeight = isBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "FILTER BY YEAR",
                        fontSize = 11.sp,
                        color = LuminaAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.selectedYearFilter != null) {
                        Text(
                            text = "Clear Year",
                            color = LuminaAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.setYearFilter(null) }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val availableYears = listOf("2024", "2025", "2026")
                    availableYears.forEach { year ->
                        val isSelected = state.selectedYearFilter == year
                        val itemBg = if (isSelected) LuminaAccent else Color.White.copy(alpha = 0.05f)
                        val itemBorderColor = if (isSelected) LuminaAccent else Color.White.copy(alpha = 0.15f)
                        val txtColor = if (isSelected) Color.Black else LuminaText
                        val isBold = if (isSelected) FontWeight.Bold else FontWeight.Normal

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(itemBg)
                                .border(BorderStroke(1.dp, itemBorderColor), RoundedCornerShape(8.dp))
                                .clickable { viewModel.setYearFilter(year) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("year_filter_$year")
                        ) {
                            Text(
                                text = "Year $year",
                                color = txtColor,
                                fontSize = 11.sp,
                                fontWeight = isBold
                            )
                        }
                    }
                }
            }
        }

        // 2. PEOPLE CAROUSEL HEADER
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "POPULAR FACES",
                        fontSize = 11.sp,
                        color = LuminaPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Select face filters",
                        color = LuminaMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("people_carousel")
                ) {
                    items(state.people) { person ->
                        val isSelected = state.selectedPersonFilter?.id == person.id
                        PersonCarouselCard(
                            person = person,
                            isSelected = isSelected,
                            onClick = { viewModel.setPersonFilter(person) }
                        )
                    }
                }
            }
        }

        // 3. PHOTOS GRID SECTION
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "MEMORY VAULT WORKSPACE (${searchFilteredPhotos.size})",
                    fontSize = 11.sp,
                    color = LuminaPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (searchFilteredPhotos.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuminaCard)
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search empty",
                                tint = LuminaMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "No Memory matches security filter",
                                color = LuminaMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Try clearing query search or adjusting filter selections.",
                                color = LuminaMuted.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        items(searchFilteredPhotos) { photo ->
            PhotoGridItem(
                photo = photo,
                onClick = { viewModel.selectPhotoForDetail(photo) },
                onFavoriteClick = { viewModel.toggleFavorite(photo.id, photo.isFavorite) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("photo_card_${photo.id}")
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// COMPONENT: INDIVIDUAL STAT DISPLAY CARD
@Composable
fun StatCard(
    title: String,
    value: String,
    info: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LuminaCard),
        border = BorderStroke(1.dp, LuminaGlassBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .width(120.dp)
            .height(96.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title.uppercase(), fontSize = 9.sp, color = LuminaMuted, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 21.sp, color = LuminaPrimary, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(info, fontSize = 9.sp, color = LuminaMuted.copy(alpha = 0.6f))
        }
    }
}

// COMPONENT: RECOGNIZED PEOPLE CAROUSEL CARD WITH GLOW OUTLINE
@Composable
fun PersonCarouselCard(
    person: Person,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderBrush = if (isSelected) {
        Brush.sweepGradient(listOf(LuminaPrimary, LuminaAccent))
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f)))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(82.dp)
            .clickable(onClick = onClick)
            .testTag("person_carousel_card_${person.id}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(borderBrush)
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(LuminaSurface)
            ) {
                // Generative artwork for the recognized face silhouette
                GenerativePhotoCanvas(
                    brushIndex = person.faceBrushIndex,
                    showOverlays = false,
                    modifier = Modifier.fillMaxSize()
                )
                // Silhouette profile icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(34.dp)
                        .align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = person.name.substringBefore(" "),
            color = if (isSelected) LuminaPrimary else LuminaText,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${person.photoCount} photos",
            color = LuminaMuted,
            fontSize = 9.sp,
            textAlign = TextAlign.Center
        )
    }
}

// COMPONENT: PHOTOS FLUID GRID CARD
@Composable
fun PhotoGridItem(
    photo: Photo,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.16f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(150.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GenerativePhotoCanvas(
                brushIndex = photo.brushIndex,
                modifier = Modifier.fillMaxSize()
            )

            // Blur banner for photo info overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xE60A0E17)) // Semi-translucent dark slate glass and blur representation
                    .drawBehind {
                        // Glossy top frosted line
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(8.dp)
            ) {
                Text(
                    text = photo.title,
                    color = LuminaText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = photo.event,
                        color = LuminaMuted,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // High Accuracy Badge in the row
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(LuminaPrimary.copy(alpha = 0.18f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${(photo.confidence * 100).toInt()}%",
                            color = LuminaPrimary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Floating Favorite Heart Icon
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(
                    imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (photo.isFavorite) LuminaDanger else Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

// 2. PEOPLE DIRECTORY SCREEN
@Composable
fun PeopleScreen(viewModel: LuminaViewModel, state: LuminaUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "PEOPLE CLASSIFIER INDEX",
            fontSize = 11.sp,
            color = LuminaPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "AI-scanned faces verified across your database history.",
            fontSize = 12.sp,
            color = LuminaMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("people_directory_grid")
        ) {
            items(state.people) { person ->
                val isSelectedWithFilter = state.selectedPersonFilter?.id == person.id
                Card(
                    colors = CardDefaults.cardColors(containerColor = LuminaCard),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelectedWithFilter) LuminaPrimary else LuminaGlassBorder
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setPersonFilter(person) }
                        .testTag("people_full_card_${person.id}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.04f))
                        ) {
                            GenerativePhotoCanvas(
                                brushIndex = person.faceBrushIndex,
                                showOverlays = false,
                                modifier = Modifier.fillMaxSize()
                            )
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = person.name,
                            color = LuminaText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = person.relation,
                            color = LuminaAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Relation size:", fontSize = 10.sp, color = LuminaMuted)
                            Text("${person.photoCount} photos", fontSize = 10.sp, color = LuminaText, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text("Chronology:", fontSize = 10.sp, color = LuminaMuted)
                            Text(person.yearsActive, fontSize = 9.sp, color = LuminaText)
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text("Accuracy:", fontSize = 10.sp, color = LuminaMuted)
                            Text("${(person.confidence * 1000).toInt() / 10f}%", fontSize = 10.sp, color = LuminaPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 3. MOMENTS TIMELINE VIEW
@Composable
fun MemoriesScreen(viewModel: LuminaViewModel, state: LuminaUiState) {
    val groupedPhotos = remember(state.photos) {
        state.photos.groupBy { it.event }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "INTELLIGENT TIMELINE",
            fontSize = 11.sp,
            color = LuminaPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Photos clustered by cognitive event neural nodes.",
            fontSize = 12.sp,
            color = LuminaMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("timeline_scroll_view")
        ) {
            groupedPhotos.forEach { (eventKey, photosInEvent) ->
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuminaCard)
                            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LuminaSecondary)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = photosInEvent.firstOrNull()?.date ?: "",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = eventKey,
                                color = LuminaText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "${photosInEvent.size} item${if (photosInEvent.size > 1) "s" else ""}",
                                    color = LuminaPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Event stats
                        val locationSummary = photosInEvent.firstOrNull()?.location ?: "Home"
                        val peopleText = photosInEvent.flatMap { it.detectedPeople.split(", ") }.distinct().joinToString(", ")
                        
                        Row(modifier = Modifier.padding(top = 6.dp)) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = "Place", tint = LuminaMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(locationSummary, color = LuminaMuted, fontSize = 11.sp)
                        }

                        if (peopleText.isNotBlank()) {
                            Row(modifier = Modifier.padding(top = 4.dp)) {
                                Icon(imageVector = Icons.Default.Face, contentDescription = "People", tint = LuminaMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Matches: $peopleText", color = LuminaMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(photosInEvent) { photo ->
                                Card(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clickable { viewModel.selectPhotoForDetail(photo) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box {
                                        GenerativePhotoCanvas(
                                            brushIndex = photo.brushIndex,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        if (photo.isFavorite) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Favorite",
                                                tint = LuminaDanger,
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. SIGNATURE SELFIE SEARCH / SCANNERS
@Composable
fun FaceSearchScreen(viewModel: LuminaViewModel, state: LuminaUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
            .testTag("face_search_tab")
    ) {
        LuminaMiniLogo()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AI DISCOVERY TERMINAL",
            fontSize = 11.sp,
            color = LuminaPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp
        )
        Text(
            "Scan face embeddings to identify high confidence family relations",
            fontSize = 12.sp,
            color = LuminaMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Selfie Scanner Interactive Terminal Card
        Card(
            colors = CardDefaults.cardColors(containerColor = LuminaCard),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Camera Icon",
                    tint = LuminaPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Search History Log",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = LuminaText
                )
                Text(
                    text = "View historic scans performed during offline indexing cycles.",
                    fontSize = 12.sp,
                    color = LuminaMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.startSelfieSearch() },
                    colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trigger_selfie_search")
                ) {
                    Text("✦ Initiate Real-Time Selfie Scan", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "PREVIOUS SCAN HISTORIES",
            fontSize = 10.sp,
            color = LuminaPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (state.searchHistory.isEmpty()) {
            Text("No scan histories found.", color = LuminaMuted, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
        } else {
            state.searchHistory.forEach { history ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = LuminaCard),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {
                            GenerativePhotoCanvas(brushIndex = history.brushIndex, showOverlays = false)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(history.labelName, fontWeight = FontWeight.Bold, color = LuminaText, fontSize = 13.sp)
                            val simpleDateFormat = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())
                            Text(simpleDateFormat.format(Date(history.timestamp)), color = LuminaMuted, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LuminaPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${(history.confidence * 100).toInt()}% Match", color = LuminaPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Text("${history.resultsCount} photos found", color = LuminaMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// 5. AI CLUSTER REVIEW WINDOW
@Composable
fun AiReviewScreen(viewModel: LuminaViewModel, state: LuminaUiState) {
    var namingText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("ai_review_view")
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "COGNITIVE CLUSTER RETRAINING",
            fontSize = 11.sp,
            color = LuminaPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Lumina found face clusters not yet bound to family names.",
            fontSize = 12.sp,
            color = LuminaMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        // AI Training queue pipeline animation
        Card(
            colors = CardDefaults.cardColors(containerColor = LuminaCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(LuminaAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VECTOR GRAPH RUNTIME: ONLINE", fontSize = 10.sp, color = LuminaAccent, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Retraining is processed locally utilizing secure cryptographic embeddings. Your biometric family photos never leave the secure offline device sandboxing.", fontSize = 11.sp, color = LuminaMuted)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "UNIDENTIFIED NEURAL CLUSTERS (${state.clusters.size})",
            fontSize = 10.sp,
            color = LuminaPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (state.clusters.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LuminaCard)
            ) {
                Text("All faces successfully trained and classified!", color = LuminaAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        } else {
            state.clusters.forEach { cluster ->
                val isNamingThis = state.namingClusterId == cluster.id

                Card(
                    colors = CardDefaults.cardColors(containerColor = LuminaCard),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("cluster_card_${cluster.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                            ) {
                                GenerativePhotoCanvas(brushIndex = cluster.brushIndex, showOverlays = false)
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unknown Clusters Entity #${cluster.id}", fontWeight = FontWeight.Bold, color = LuminaText, fontSize = 14.sp)
                                Text("Features context: ${cluster.representativeTags}", color = LuminaMuted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${cluster.size} matching photos found", color = LuminaPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Average Similarity", color = LuminaMuted, fontSize = 8.sp)
                                Text("${(cluster.confidence * 1000).toInt() / 10f}%", color = LuminaPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isNamingThis) {
                            OutlinedTextField(
                                value = namingText,
                                onValueChange = { namingText = it },
                                label = { Text("Enter Family Member Name", color = LuminaMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = LuminaText,
                                    unfocusedTextColor = LuminaText,
                                    focusedBorderColor = LuminaPrimary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("cluster_input_${cluster.id}")
                            )
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = { viewModel.startNamingCluster(-1) }) {
                                    Text("Cancel", color = LuminaMuted)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.submitClusterName(cluster.id, namingText)
                                        namingText = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                                    modifier = Modifier.testTag("cluster_submit_${cluster.id}")
                                ) {
                                    Text("Train Model", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.ignoreCluster(cluster.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LuminaDanger),
                                    border = BorderStroke(1.dp, LuminaDanger.copy(alpha = 0.4f)),
                                    modifier = Modifier.testTag("cluster_ignore_${cluster.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Ignore", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ignore Target", fontSize = 11.sp, color = LuminaDanger)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        namingText = ""
                                        viewModel.startNamingCluster(cluster.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                                    modifier = Modifier.testTag("cluster_activate_${cluster.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Name", modifier = Modifier.size(12.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Assign & Name", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. SETTINGS CONTROL VIEW
@Composable
fun SettingsScreen(viewModel: LuminaViewModel, state: LuminaUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_view")
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "COGNITIVE PREFERENCES",
            fontSize = 11.sp,
            color = LuminaPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Calibrate indexing tolerance levels or manage offline database structures.",
            fontSize = 12.sp,
            color = LuminaMuted
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Interactive dynamic tolerance slider block
        Card(
            colors = CardDefaults.cardColors(containerColor = LuminaCard),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "AI Match Tolerance threshold",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = LuminaText
                )
                Text(
                    text = "Gaps with similarity scores below the threshold are filtered from core memory views to enforce data precision.",
                    color = LuminaMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = state.toleranceThreshold,
                        onValueChange = { viewModel.setToleranceThreshold(it) },
                        valueRange = 0.50f..0.98f,
                        colors = SliderDefaults.colors(
                            thumbColor = LuminaPrimary,
                            activeTrackColor = LuminaPrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tolerance_slider")
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LuminaPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(state.toleranceThreshold * 100).toInt()}%",
                            color = LuminaPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Local Storage Meter info panel
        Card(
            colors = CardDefaults.cardColors(containerColor = LuminaCard),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Device Local Storage", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 0.42f },
                    color = LuminaPrimary,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Capacity: 12.4 GB of 32 GB utilized", fontSize = 11.sp, color = LuminaMuted)
                    Text("42% full", fontSize = 11.sp, color = LuminaPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Security options switcher row
        var autoDetectActive by remember { mutableStateOf(true) }
        var cloudSyncActive by remember { mutableStateOf(false) }
        var biometricsActive by remember { mutableStateOf(true) }

        Card(
            colors = CardDefaults.cardColors(containerColor = LuminaCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsSwitchRow(
                    tag = "local_scan",
                    title = "Continuous Background Indexing",
                    subtitle = "Silently search and catalog newly discovered faces when phone idles.",
                    checked = autoDetectActive,
                    onCheckedChange = {
                        autoDetectActive = it
                        viewModel.showToast(if (it) "Idle photo processing activated" else "Idle processing paused")
                    }
                )
                Divider(color = Color.White.copy(alpha = 0.05f))
                SettingsSwitchRow(
                    tag = "cloud_sync",
                    title = "Hybrid Cloud Backup Synchronization",
                    subtitle = "Stream encrypted backup sets to private cloud storage vaults.",
                    checked = cloudSyncActive,
                    onCheckedChange = {
                        cloudSyncActive = it
                        viewModel.showToast(if (it) "Private hybrid cloud sync initialized" else "Cloud synchronization terminated")
                    }
                )
                Divider(color = Color.White.copy(alpha = 0.05f))
                SettingsSwitchRow(
                    tag = "biometric_lock",
                    title = "Biometrics Memory Security Vault",
                    subtitle = "Request finger or facial ID to toggle detailed timeline galleries.",
                    checked = biometricsActive,
                    onCheckedChange = {
                        biometricsActive = it
                        viewModel.showToast(if (it) "App securely locked with Biometric tokens" else "Lock credentials removed")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsSwitchRow(
    tag: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = LuminaText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, color = LuminaMuted, fontSize = 10.sp, lineHeight = 14.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LuminaPrimary,
                checkedTrackColor = LuminaPrimary.copy(alpha = 0.40f)
            ),
            modifier = Modifier.testTag("switch_$tag")
        )
    }
}

// EXTRAORDINARY INTERACTIVE CAMERA/SELFIE MODAL WITH DURATION COROUTINE SCANS
@Composable
fun SelfieSearchModal(viewModel: LuminaViewModel, state: LuminaUiState) {
    val progressAnim by animateFloatAsState(
        targetValue = state.scanProgress,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
        label = "progress"
    )

    Dialog(
        onDismissRequest = { viewModel.cancelSelfieSearch() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .safeDrawingPadding()
                .testTag("selfie_modal")
        ) {
            IconButton(
                onClick = { viewModel.cancelSelfieSearch() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .background(Color.White.copy(alpha = 0.10f), CircleShape)
                    .testTag("modal_close")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "LUMINA COGNITIVE SCANNER",
                    color = LuminaPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                // STEP PROGRESS FLOW STATUS INDICATOR
                SelfieStepDots(state.selfieScanState)

                Spacer(modifier = Modifier.weight(0.1f))

                // INTERACTIVE WORK CELL: Shows camera, radar scan line overlay or matches list!
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth(if (LocalConfiguration.current.screenWidthDp >= 600) 0.5f else 0.85f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(LuminaCard)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(listOf(LuminaPrimary, LuminaAccent)),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    when (state.selfieScanState) {
                        SelfieScanState.ONBOARDING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(LuminaPrimary.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = LuminaPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Position Face in Area",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LuminaText,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Place your face directly in camera bounds under good lightning conditions to trigger the 3-step neural indexing algorithm.",
                                    color = LuminaMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        SelfieScanState.DETECTING, SelfieScanState.SCANNING, SelfieScanState.SEARCHING -> {
                            // Simulated Camera live feed with scanning visual bars
                            Box(modifier = Modifier.fillMaxSize()) {
                                GenerativePhotoCanvas(
                                    brushIndex = 4, // starry sparkling face guide gradient background
                                    showOverlays = false,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Green scanner horizontal line floating up/down
                                val infiniteTransition = rememberInfiniteTransition(label = "scan")
                                val lineY by infiniteTransition.animateFloat(
                                    initialValue = 0.1f,
                                    targetValue = 0.9f,
                                    animationSpec = infiniteRepeatable(
                                        animation = keyframes {
                                            durationMillis = 2000
                                            0.1f at 0
                                            0.9f at 1000
                                            0.1f at 2000
                                        }
                                    ),
                                    label = "y"
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val sizeX = size.width
                                    val sizeY = size.height

                                    // Face scanner targeting Reticle circle overlay bounds
                                    drawCircle(
                                        color = LuminaPrimary.copy(alpha = 0.5f),
                                        radius = sizeX * 0.35f,
                                        style = Stroke(width = 3f)
                                    )
                                    drawCircle(
                                        color = LuminaAccent.copy(alpha = 0.25f),
                                        radius = sizeX * 0.32f,
                                        style = Stroke(width = 1f)
                                    )

                                    // Scanner laser line rendering
                                    drawLine(
                                        color = LuminaAccent,
                                        start = Offset(0f, sizeY * lineY),
                                        end = Offset(sizeX, sizeY * lineY),
                                        strokeWidth = 4f
                                    )
                                }

                                // Face guide tracking dots overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(170.dp)
                                        .border(2.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                )
                            }
                        }
                        SelfieScanState.FINISHED -> {
                            // Search matches revealed state
                            Box(modifier = Modifier.fillMaxSize()) {
                                GenerativePhotoCanvas(
                                    brushIndex = 2,
                                    showOverlays = false,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(CircleShape)
                                            .background(LuminaAccent.copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Success",
                                            tint = LuminaAccent,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Scanned Entity Identified!",
                                        fontWeight = FontWeight.Bold,
                                        color = LuminaText,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = state.lastScannedLabel,
                                        color = LuminaPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        "Matches Found: ${state.matchResults.size} photos",
                                        color = LuminaMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // Phase text updates and CTA button or matches list!
                when (state.selfieScanState) {
                    SelfieScanState.ONBOARDING -> {
                        Text(
                            text = "Lumina scans face structures utilizing neural networks locally on your device for absolute 100% privacy.",
                            fontSize = 11.sp,
                            color = LuminaMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.proceedToCapture() },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_demo_camera")
                        ) {
                            Text("✦ Access Device Camera & Scan", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                    SelfieScanState.DETECTING, SelfieScanState.SCANNING, SelfieScanState.SEARCHING -> {
                        val currentStepLabel = when (state.selfieScanState) {
                            SelfieScanState.DETECTING -> "Detecting facial landmarks..."
                            SelfieScanState.SCANNING -> "Extracting biometric vector parameters..."
                            SelfieScanState.SEARCHING -> "Intersecting high-dimensional local indexes..."
                            else -> ""
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = currentStepLabel.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = LuminaPrimary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progressAnim },
                                color = LuminaPrimary,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(progressAnim * 100).toInt()}% processed",
                                fontSize = 11.sp,
                                color = LuminaMuted
                            )
                        }
                    }
                    SelfieScanState.FINISHED -> {
                        // MATCH DIRECTORY LIST (Explosion overlay results)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "INDIVIDUAL SCAN MATCH RESULTS",
                                    fontSize = 10.sp,
                                    color = LuminaPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "Accuracy score > ${(state.toleranceThreshold * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    color = LuminaMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scan_results_carousel")
                            ) {
                                items(state.matchResults) { photo ->
                                    Card(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .height(115.dp)
                                            .clickable { viewModel.selectPhotoForDetail(photo) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = LuminaCard)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            GenerativePhotoCanvas(brushIndex = photo.brushIndex, showOverlays = false)
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.45f))
                                                    .padding(6.dp)
                                            ) {
                                                Column {
                                                    Text(photo.title, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("${(photo.confidence * 100).toInt()}% Match", color = LuminaAccent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.cancelSelfieSearch() },
                                colors = ButtonDefaults.buttonColors(containerColor = LuminaSecondary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close Result and return to Vault", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}

@Composable
fun SelfieStepDots(currentState: SelfieScanState) {
    val items = listOf(SelfieScanState.DETECTING, SelfieScanState.SCANNING, SelfieScanState.SEARCHING, SelfieScanState.FINISHED)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { step ->
            val isCurrent = currentState == step
            val hasPassed = currentState.ordinal >= step.ordinal

            val sizeDot by animateDpAsState(if (isCurrent) 12.dp else 8.dp, label = "size")
            val colorDot = when {
                isCurrent -> LuminaPrimary
                hasPassed -> LuminaAccent
                else -> Color.White.copy(alpha = 0.15f)
            }

            Box(
                modifier = Modifier
                    .size(sizeDot)
                    .clip(CircleShape)
                    .background(colorDot)
            )
        }
    }
}

// CINEMATIC immersive PHOTO VIEWER (Apple-Photos Level)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoViewerModal(
    photo: Photo,
    onClose: () -> Unit,
    onFavoriteToggle: () -> Unit,
    allPhotos: List<Photo>,
    onSelectRelated: (Photo) -> Unit
) {
    var showAiBoxes by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .safeDrawingPadding()
                .testTag("photo_viewer_modal")
        ) {
            // Close panel float
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    .testTag("viewer_close")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // PHOTO PREVIEW ELEMENT
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(if (LocalConfiguration.current.screenWidthDp >= 600) 0.7f else 0.95f)
                            .aspectRatio(1.2f)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            GenerativePhotoCanvas(
                                brushIndex = photo.brushIndex,
                                modifier = Modifier.fillMaxSize()
                            )

                            // AI Face Overlay bounding box reticles
                            if (showAiBoxes) {
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    val w = size.width
                                    val h = size.height

                                    // Reticle 1 (Left area face)
                                    drawRect(
                                        color = LuminaPrimary,
                                        topLeft = Offset(w * 0.18f, h * 0.22f),
                                        size = androidx.compose.ui.geometry.Size(w * 0.25f, h * 0.32f),
                                        style = Stroke(width = 4f)
                                    )

                                    // Reticle 2 (Right area face if multiple people)
                                    if (photo.detectedPeople.contains(",")) {
                                        drawRect(
                                            color = LuminaAccent,
                                            topLeft = Offset(w * 0.58f, h * 0.28f),
                                            size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.28f),
                                            style = Stroke(width = 4f)
                                        )
                                    }
                                }

                                // Interactive floating tag labels below reticles
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 32.dp, top = 28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LuminaPrimary)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = photo.detectedPeople.substringBefore(","),
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (photo.detectedPeople.contains(",")) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(end = 48.dp, top = 38.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(LuminaAccent)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = photo.detectedPeople.substringAfter(", ").substringBefore(","),
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // DETAILS CONTENT RAIL (Floating Glassmorphism panel)
                Card(
                    colors = CardDefaults.cardColors(containerColor = LuminaSurface),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(photo.title, color = LuminaText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(photo.event, color = LuminaAccent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("•", color = LuminaMuted)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(photo.date, color = LuminaMuted, fontSize = 11.sp)
                                }
                            }

                            // Favorite quick and AI Box Toggles
                            IconButton(onClick = { showAiBoxes = !showAiBoxes }) {
                                Icon(
                                    imageVector = if (showAiBoxes) Icons.Default.Face else Icons.Default.Close,
                                    contentDescription = "Toggle Face boxes",
                                    tint = if (showAiBoxes) LuminaPrimary else LuminaMuted
                                )
                            }

                            IconButton(onClick = onFavoriteToggle) {
                                Icon(
                                    imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (photo.isFavorite) LuminaDanger else LuminaText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = "Location", tint = LuminaMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(photo.location, color = LuminaMuted, fontSize = 11.sp)
                        }

                        // Tags Chips area
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            photo.tags.split(", ").forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(tag, color = LuminaText, fontSize = 10.sp)
                                }
                            }
                        }

                        // Related photos row slider
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("RELATED PHOTOS IN EVENT", fontSize = 10.sp, color = LuminaPrimary, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val related = allPhotos.filter { it.event == photo.event && it.id != photo.id }
                        if (related.isEmpty()) {
                            Text("No other photos found in event catalog.", color = LuminaMuted, fontSize = 11.sp)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(related) { item ->
                                    Card(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clickable { onSelectRelated(item) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        GenerativePhotoCanvas(brushIndex = item.brushIndex, showOverlays = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog input box flow to add custom Mock/Simulated memories and watch states refresh in real-time
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddPhotoDialog(
    onDismiss: () -> Unit,
    viewModel: LuminaViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var step by remember { mutableStateOf(1) } // 1: Select File/Validation, 2: Face Detection & Labeling, 3: Save Metadata

    // Upload & validation states
    var fileName by remember { mutableStateOf("") }
    var mimeType by remember { mutableStateOf("") }
    var fileUriString by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var validationSuccess by remember { mutableStateOf<String?>(null) }

    // Analysis states
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisProgress by remember { mutableStateOf(0f) }
    var analysisDone by remember { mutableStateOf(false) }

    // Face regions label state
    var face1Label by remember { mutableStateOf("") }
    var face1IsNew by remember { mutableStateOf(false) }
    var face1NewName by remember { mutableStateOf("") }
    var face1NewRelation by remember { mutableStateOf("Family Member") }

    var face2Label by remember { mutableStateOf("") }
    var face2IsNew by remember { mutableStateOf(false) }
    var face2NewName by remember { mutableStateOf("") }
    var face2NewRelation by remember { mutableStateOf("Family Member") }

    // Photo metadata state
    var title by remember { mutableStateOf("") }
    var event by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    // Native photo picker activity contract
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                fileUriString = uri.toString()
                val type = context.contentResolver.getType(uri) ?: ""
                mimeType = type

                // Retrieve file name
                var name = "NATIVE_IMAGE.IMG"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                }
                fileName = name

                // Validation logic (PNG, JPG/JPEG, WEBP)
                val ext = name.substringAfterLast('.', "").lowercase()
                val isImageMime = type == "image/jpeg" || type == "image/png" || type == "image/webp"
                val isImageExt = ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "webp"

                if (isImageMime || isImageExt) {
                    validationSuccess = "✓ Secure format validated successfully! (Format: ${type.ifBlank { ext.uppercase() }})"
                    validationError = null
                } else {
                    validationError = "❌ Unauthorized Format! Secure vault accepts only high-fidelity JPEG, PNG, or WEBP images."
                    validationSuccess = null
                }
            }
        }
    )

    // Simulate ticking for scan progress bar
    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            analysisProgress = 0f
            while (analysisProgress < 1.0f) {
                kotlinx.coroutines.delay(150)
                analysisProgress += 0.08f
            }
            analysisProgress = 1.0f
            isAnalyzing = false
            analysisDone = true
            step = 2 // transition to label face step!
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secure Upload",
                    tint = LuminaPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (step) {
                        1 -> "Secure Photo Upload"
                        2 -> "AI Face Labeling Portal"
                        else -> "Catalog Metadata Nodes"
                    },
                    color = LuminaPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // STEP 1: Select/Upload Image with format verification
                if (step == 1) {
                    Text(
                        text = "Upload high-resolution images to the secure vault. Lumina runs client-side checking and localized facial mapping.",
                        fontSize = 11.sp,
                        color = LuminaMuted,
                        lineHeight = 16.sp
                    )

                    // Dashed selection zone
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                singlePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add image icon",
                                tint = LuminaPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (fileName.isBlank()) "TAP TO CHOOSE SECURE PHOTO" else fileName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "JPEG, PNG, WEBP supported • Maximum 12MB",
                                fontSize = 10.sp,
                                color = LuminaMuted
                            )
                        }
                    }

                    // Preset quick-test option for streamers
                    Button(
                        onClick = {
                            fileName = "DSC_9204_CHRISTMAS_CABIN.JPG"
                            mimeType = "image/jpeg"
                            validationSuccess = "✓ System Preset verified! (Format: JPEG)"
                            validationError = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.07f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✨ USE SYSTEM HIGH-RES PRESET SNAPSHOT", color = LuminaPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Success or Fail Feedback Indicators
                    validationSuccess?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LuminaAccent.copy(alpha = 0.12f))
                                .padding(10.dp)
                        ) {
                            Text(it, color = LuminaAccent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    validationError?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LuminaDanger.copy(alpha = 0.12f))
                                .padding(10.dp)
                        ) {
                            Text(it, color = LuminaDanger, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Trigger AI face matching line scanner
                    if (validationSuccess != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        if (isAnalyzing) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LinearProgressIndicator(
                                    progress = { analysisProgress },
                                    color = LuminaPrimary,
                                    trackColor = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Analyzing face vector boundaries... ${(analysisProgress * 100).toInt()}%",
                                    color = LuminaPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = { isAnalyzing = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Face, contentDescription = "", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("RUN AI COGNITIVE FACE DETECTION", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // STEP 2: AI Face Circular regions identified & labeled with interactive selections
                if (step == 2) {
                    Text(
                        "AI Detected 2 Face Graph Nodes in this secure upload snapshot. Name each unique family member coordinate below:",
                        fontSize = 11.sp,
                        color = LuminaMuted,
                        lineHeight = 16.sp
                    )

                    // Photo representation box with circular target overlays!
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(BrandSecondary.copy(alpha = 0.6f), Color(0xFF0F172A))
                                )
                            )
                    ) {
                        // Let's draw scanning graphic with bounding coordinates!
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            
                            // Draw glowing scanning overlay or matrix mesh
                            drawCircle(
                                color = LuminaPrimary.copy(alpha = 0.15f),
                                radius = w * 0.45f,
                                center = Offset(w * 0.5f, h * 0.5f)
                            )
                        }

                        // Circular Face region #1 glowing locator
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .align(Alignment.Center)
                                // offset left-ish
                                .offset(x = (-45).dp, y = (-12).dp)
                                .border(BorderStroke(2.dp, LuminaAccent), CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-16).dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LuminaAccent)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("Face #1", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        // Circular Face region #2 glowing locator
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                                // offset right-ish
                                .offset(x = 40.dp, y = 15.dp)
                                .border(BorderStroke(2.dp, LuminaPrimary), CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-16).dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LuminaPrimary)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("Face #2", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    // Face 1 label form
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        border = BorderStroke(1.dp, LuminaAccent.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LuminaAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("FACE COORDINATE #1", color = LuminaAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                            }

                            // Picker Spinner or Custom name input
                            if (face1IsNew) {
                                OutlinedTextField(
                                    value = face1NewName,
                                    onValueChange = { face1NewName = it },
                                    label = { Text("Name of New Family Member", color = LuminaMuted) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuminaText, unfocusedTextColor = LuminaText),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = face1NewRelation,
                                    onValueChange = { face1NewRelation = it },
                                    label = { Text("Vault Relationship Profile", color = LuminaMuted) },
                                    placeholder = { Text("Cousin, Brother, Grandma, Friend") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuminaText, unfocusedTextColor = LuminaText),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                TextButton(onClick = { face1IsNew = false }) {
                                    Text("← Use Existing Labeled Member", color = LuminaAccent, fontSize = 11.sp)
                                }
                            } else {
                                // Simple list of existing member selections
                                Text("Assign Identified Face:", color = LuminaMuted, fontSize = 11.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val suggestions = state.people.map { it.name } + listOf("Unsure / Other")
                                    suggestions.forEach { sug ->
                                        val selected = face1Label == sug
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selected) LuminaAccent else Color.White.copy(alpha = 0.05f))
                                                .clickable { face1Label = sug }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(sug, color = if (selected) Color.Black else LuminaText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                TextButton(onClick = { face1IsNew = true }) {
                                    Text("+ Setup New Labeled Member Profile", color = LuminaAccent, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Face 2 label form
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        border = BorderStroke(1.dp, LuminaPrimary.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LuminaPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("FACE COORDINATE #2", color = LuminaPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                            }

                            // Picker Spinner or Custom name input
                            if (face2IsNew) {
                                OutlinedTextField(
                                    value = face2NewName,
                                    onValueChange = { face2NewName = it },
                                    label = { Text("Name of New Family Member", color = LuminaMuted) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuminaText, unfocusedTextColor = LuminaText),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = face2NewRelation,
                                    onValueChange = { face2NewRelation = it },
                                    label = { Text("Vault Relationship Profile", color = LuminaMuted) },
                                    placeholder = { Text("Cousin, Brother, Grandma, Friend") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuminaText, unfocusedTextColor = LuminaText),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                TextButton(onClick = { face2IsNew = false }) {
                                    Text("← Use Existing Labeled Member", color = LuminaPrimary, fontSize = 11.sp)
                                }
                            } else {
                                Text("Assign Identified Face:", color = LuminaMuted, fontSize = 11.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val suggestions = state.people.map { it.name } + listOf("Unsure / Other")
                                    suggestions.forEach { sug ->
                                        val selected = face2Label == sug
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selected) LuminaPrimary else Color.White.copy(alpha = 0.05f))
                                                .clickable { face2Label = sug }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(sug, color = if (selected) Color.Black else LuminaText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                TextButton(onClick = { face2IsNew = true }) {
                                    Text("+ Setup New Labeled Member Profile", color = LuminaPrimary, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { step = 3 },
                        colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PROCEED TO METADATA ARCHIVAL", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // STEP 3: Save Metadata node (Title, Event, Location, Tags)
                if (step == 3) {
                    Text(
                        "Input metadata coordinates for memory indexing. All items are securely recorded in the graph system.",
                        fontSize = 11.sp,
                        color = LuminaMuted,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Memory Node Title", color = LuminaMuted) },
                        placeholder = { Text("e.g. Backyard family barbecue smiles") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuminaText, unfocusedTextColor = LuminaText),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = event,
                        onValueChange = { event = it },
                        label = { Text("Classified Event Theme", color = LuminaMuted) },
                        placeholder = { Text("e.g. Thanksgiving Dinner, Pool Splash") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuminaText, unfocusedTextColor = LuminaText),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Physical Location Address", color = LuminaMuted) },
                        placeholder = { Text("e.g. Tahoe Cabin, Backyard Home") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuminaText, unfocusedTextColor = LuminaText),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Semantic Descriptor Tags (Comma-split)", color = LuminaMuted) },
                        placeholder = { Text("Summer, BBQ, Fun, Cozy") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuminaText, unfocusedTextColor = LuminaText),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (step == 3) {
                Button(
                    onClick = {
                        // Gather labels and new people to register
                        val resolvedLabels = mutableListOf<String>()
                        val newPeople = mutableListOf<Pair<String, String>>()

                        // Face 1 resolution
                        if (face1IsNew && face1NewName.isNotBlank()) {
                            resolvedLabels.add(face1NewName)
                            newPeople.add(Pair(face1NewName, face1NewRelation))
                        } else if (face1Label.isNotBlank() && face1Label != "Unsure / Other") {
                            resolvedLabels.add(face1Label)
                        }

                        // Face 2 resolution
                        if (face2IsNew && face2NewName.isNotBlank()) {
                            resolvedLabels.add(face2NewName)
                            newPeople.add(Pair(face2NewName, face2NewRelation))
                        } else if (face2Label.isNotBlank() && face2Label != "Unsure / Other") {
                            resolvedLabels.add(face2Label)
                        }

                        viewModel.uploadPhotoWithLabels(
                            title = title,
                            event = event,
                            location = location,
                            tags = tags,
                            labeledPeople = resolvedLabels,
                            newPeopleToRegister = newPeople
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                    modifier = Modifier.testTag("add_photo_confirm")
                ) {
                    Text("Secure & Sync Portal", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LuminaMuted)
            }
        },
        containerColor = LuminaSurface,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

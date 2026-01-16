package com.example.library.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.library.data.*
import com.example.library.ui.*
import com.example.library.ui.components.BottomNavigationBar
import com.example.library.ui.components.NavigationItem
import com.example.library.ui.components.SearchBar
import com.example.library.ui.components.SunIcon
import com.example.library.ui.components.WorkItem
import com.example.library.ui.components.WorkItemCard
import com.example.library.ui.theme.IconTextColor
import com.example.library.ui.theme.MainBackgroundColor
import com.example.library.ui.theme.TitleColorBetween
import java.io.File

// Extension function to convert Work to WorkItem
fun Work.toWorkItem(): WorkItem {
    val episodesText = when (type) {
        WorkType.BOOK -> chapters?.let { "$it ${if (it == 1) "том" else "томов"}" } ?: ""
        WorkType.MANGA -> chapters?.let { "$it ${if (it == 1) "глава" else "глав"}" } ?: ""
        WorkType.ANIME -> episodes?.let { "$it ${if (it == 1) "эп" else "эп"}" } ?: ""
        WorkType.SERIES -> episodes?.let { "$it ${if (it == 1) "сезон" else "сезонов"}" } ?: ""
    }
    
    return WorkItem(
        id = id,
        title = title,
        imageUrl = coverPath,
        episodes = episodesText,
        description = description
    )
}

@Composable
fun LibraryScreen(
    currentTheme: AppTheme = AppTheme.DARK,
    onThemeChange: (AppTheme) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val languageState = rememberLanguageState()
    val currentLanguage = languageState.currentLanguage // Observe language changes
    val strings = remember(currentLanguage) { languageState.strings }
    val context = LocalContext.current
    val density = LocalDensity.current
    val repository = remember { WorkRepository(context) }

    // Preferences for tabs & modes
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    // Notes mode
    var notesMode by remember {
        mutableStateOf<Boolean>(prefs.getBoolean("notes_mode", false))
    }

    // Per-tab visibility switches (except Profile, which is always shown)
    var booksTabEnabled by remember {
        mutableStateOf(prefs.getBoolean("tab_books_enabled", true))
    }
    var animeTabEnabled by remember {
        mutableStateOf(prefs.getBoolean("tab_anime_enabled", true))
    }
    var mangaTabEnabled by remember {
        mutableStateOf(prefs.getBoolean("tab_manga_enabled", true))
    }
    var tvSeriesTabEnabled by remember {
        mutableStateOf(prefs.getBoolean("tab_tv_enabled", true))
    }

    fun getDefaultTab(
        notesModeEnabled: Boolean,
        booksEnabled: Boolean,
        animeEnabled: Boolean,
        mangaEnabled: Boolean,
        tvEnabled: Boolean
    ): NavigationItem {
        // При включенном режиме заметок по умолчанию открываем вкладку "Заметки"
        if (notesModeEnabled) return NavigationItem.Notes

        // Иначе выбираем первую доступную вкладку с произведениями
        return when {
            booksEnabled -> NavigationItem.Books
            animeEnabled -> NavigationItem.Anime
            mangaEnabled -> NavigationItem.Manga
            tvEnabled -> NavigationItem.TVSeries
            else -> NavigationItem.Profile
        }
    }

    var selectedItem by remember {
        mutableStateOf<NavigationItem>(
            getDefaultTab(
                notesModeEnabled = notesMode,
                booksEnabled = booksTabEnabled,
                animeEnabled = animeTabEnabled,
                mangaEnabled = mangaTabEnabled,
                tvEnabled = tvSeriesTabEnabled
            )
        )
    }

    var searchQuery by remember { mutableStateOf<String>("") }
    var showAddWorkScreen by remember { mutableStateOf<Boolean>(false) }
    var works by remember { mutableStateOf<List<Work>>(emptyList()) }
    var selectedWork by remember { mutableStateOf<Work?>(null) }
    var editingWork by remember { mutableStateOf<Work?>(null) }
    var workToDelete by remember { mutableStateOf<Work?>(null) }
    var expandedCoverWork by remember { mutableStateOf<Work?>(null) }

    // Search inside details-mode (icon-only search bar)
    var detailSearchExpanded by remember { mutableStateOf(false) }
    var detailSearchQuery by remember { mutableStateOf("") }

    // Back press when detail-search is open: close search instead of leaving work
    BackHandler(enabled = selectedWork != null && detailSearchExpanded) {
        detailSearchExpanded = false
        detailSearchQuery = ""
    }

    // Back для увеличенной обложки
    BackHandler(enabled = expandedCoverWork != null) {
        expandedCoverWork = null
    }

    // Общий back: закрываем экран добавления или просмотра произведения.
    // Не должен срабатывать, пока открыт detail-search (detailSearchExpanded = true) или увеличенная обложка,
    // чтобы системная кнопка «Назад» сперва закрывала поиск или обложку.
    BackHandler(enabled = showAddWorkScreen || (selectedWork != null && !detailSearchExpanded && expandedCoverWork == null)) {
        when {
            showAddWorkScreen -> {
                // Если редактировали существующее произведение, возвращаемся к экрану просмотра
                if (editingWork != null) {
                    selectedWork = editingWork
                }
                showAddWorkScreen = false
                editingWork = null
            }
            selectedWork != null -> selectedWork = null
        }
    }
    
    LaunchedEffect(Unit) {
        works = repository.getAllWorks()
    }

    // Reset detail search when leaving work details
    LaunchedEffect(selectedWork) {
        if (selectedWork == null) {
            detailSearchExpanded = false
            detailSearchQuery = ""
        }
    }
    
    // Filter works by selected tab
    val filteredWorks = remember(works, selectedItem, searchQuery) {
        val typeFilter = when (selectedItem) {
            NavigationItem.Books -> WorkType.BOOK
            NavigationItem.Anime -> WorkType.ANIME
            NavigationItem.Manga -> WorkType.MANGA
            NavigationItem.TVSeries -> WorkType.SERIES
            else -> null
        }
        
        var filtered = if (typeFilter != null) {
            works.filter { it.type == typeFilter }
        } else {
            emptyList()
        }
        
        // Apply search filter
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.otherTitle?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        filtered
    }

    val mainBackgroundColor = MainBackgroundColor()
    val isDarkTheme = currentTheme == AppTheme.DARK
    // Colors used across header + detail-search list
    val iconTextColor = IconTextColor()
    val titleColorBetween = TitleColorBetween()
    
    CompositionLocalProvider(LocalStrings provides strings) {
        Box(modifier = modifier.fillMaxSize()) {
            // Фон-обложка начинается с самого верха экрана (включая область с кнопками)
            if (selectedWork != null && !detailSearchExpanded) {
                val coverPath = selectedWork?.coverPath
                if (!coverPath.isNullOrBlank()) {
                    val coverImageUri = if (coverPath.startsWith("/")) {
                        Uri.fromFile(File(coverPath))
                    } else {
                        coverPath.toUri()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Высота: Spacer (40dp) + высота строки поиска (~56dp) + область обложки (343dp)
                            .height(439.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(coverImageUri)
                                    .build()
                            ),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter,
                            alpha = 0.35f
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            mainBackgroundColor
                                        )
                                    )
                                )
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (selectedWork != null && !detailSearchExpanded) {
                            Modifier.background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        mainBackgroundColor
                                    ),
                                    startY = 0f,
                                    endY = with(density) { 439.dp.toPx() }
                                )
                            )
                        } else {
                            Modifier.background(mainBackgroundColor)
                        }
                    )
            ) {
            // Spacer to push search bar from top
            Spacer(modifier = Modifier.height(40.dp))

            // Search bar with theme toggle and add/edit button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconTextColor = IconTextColor()
                val titleColorBetween = TitleColorBetween()
                
                // Back button (only when viewing work details)
                if (selectedWork != null) {
                    IconButton(onClick = { selectedWork = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancel,
                            tint = titleColorBetween
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                // Search bar (icon-only when viewing work details, full when not)
                SearchBar(
                    modifier = Modifier.weight(1f),
                    currentTheme = currentTheme,
                    iconOnly = selectedWork != null,
                    query = if (selectedWork != null) detailSearchQuery else null,
                    onSearchQueryChange = { q ->
                        if (selectedWork != null) detailSearchQuery = q else searchQuery = q
                    },
                    expanded = if (selectedWork != null) detailSearchExpanded else null,
                    onExpandedChange = { detailSearchExpanded = it }
                )
                
                Spacer(modifier = Modifier.width(12.dp))

                // Hide other icons when detail-search expanded (search should take full width)
                if (!(selectedWork != null && detailSearchExpanded)) {
                    SunIcon(
                        onClick = {
                            onThemeChange(if (currentTheme == AppTheme.DARK) AppTheme.LIGHT else AppTheme.DARK)
                        },
                        color = iconTextColor,
                        iconSize = 20.dp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    if (selectedWork != null) {
                        IconButton(
                            onClick = {
                                editingWork = selectedWork
                                selectedWork = null
                                showAddWorkScreen = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit work",
                                tint = if (currentTheme == AppTheme.DARK) Color.White else Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { workToDelete = selectedWork }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete work",
                                // same color as edit icon
                                tint = if (currentTheme == AppTheme.DARK) Color.White else Color.Black
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showAddWorkScreen = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = strings.addWork,
                                tint = if (currentTheme == AppTheme.DARK) iconTextColor.copy(alpha = 0.9f) else Color.Black
                            )
                        }
                    }
                }
            }

            // Main content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Delete confirmation dialog
                workToDelete?.let { w ->
                    // Dialog styling per theme (restore previous dark theme container)
    val dialogContainer = if (isDarkTheme) Color(0xFF333333) else Color.White
                    val dialogTitleColor = if (isDarkTheme) Color.White else Color.Black
                    val dialogTextColor = if (isDarkTheme) Color.White else Color.Black
                    AlertDialog(
                        onDismissRequest = { workToDelete = null },
                        title = { Text("Удалить?", color = dialogTitleColor) },
                        text = { Text("«${w.title}» будет удалено вместе с файлом .md. Это действие нельзя отменить.", color = dialogTextColor) },
                        containerColor = dialogContainer,
                        titleContentColor = dialogTitleColor,
                        textContentColor = dialogTextColor,
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (repository.deleteWork(w.id)) {
                                        works = repository.getAllWorks()
                                        if (selectedWork?.id == w.id) selectedWork = null
                                        workToDelete = null
                                    }
                                }
                            ) { Text("Удалить") }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { workToDelete = null }
                            ) { Text("Отмена") }
                        }
                    )
                }

                when (selectedItem) {
                    NavigationItem.Notes -> {
                        NotesScreen(modifier = Modifier.fillMaxSize())
                    }
                    NavigationItem.Profile -> {
                        ProfileScreen(
                            currentLanguage = languageState.currentLanguage,
                            onLanguageChange = { languageState.setLanguage(it) },
                            notesMode = notesMode,
                            onNotesModeChange = { newMode ->
                                notesMode = newMode
                                prefs.edit { putBoolean("notes_mode", newMode) }
                                // Больше не переключаемся автоматически на вкладку "Заметки"
                                // при включении режима заметок.
                                // При выключении режима, если пользователь был на вкладке "Заметки",
                                // выберем первую доступную вкладку с произведениями.
                                if (!newMode && selectedItem == NavigationItem.Notes) {
                                    selectedItem = getDefaultTab(
                                        notesModeEnabled = false,
                                        booksEnabled = booksTabEnabled,
                                        animeEnabled = animeTabEnabled,
                                        mangaEnabled = mangaTabEnabled,
                                        tvEnabled = tvSeriesTabEnabled
                                    )
                                }
                            },
                            booksTabEnabled = booksTabEnabled,
                            onBooksTabEnabledChange = { enabled ->
                                booksTabEnabled = enabled
                                prefs.edit { putBoolean("tab_books_enabled", enabled) }
                                if (!enabled && selectedItem == NavigationItem.Books) {
                                    selectedItem = getDefaultTab(
                                        notesModeEnabled = notesMode,
                                        booksEnabled = false,
                                        animeEnabled = animeTabEnabled,
                                        mangaEnabled = mangaTabEnabled,
                                        tvEnabled = tvSeriesTabEnabled
                                    )
                                }
                            },
                            animeTabEnabled = animeTabEnabled,
                            onAnimeTabEnabledChange = { enabled ->
                                animeTabEnabled = enabled
                                prefs.edit { putBoolean("tab_anime_enabled", enabled) }
                                if (!enabled && selectedItem == NavigationItem.Anime) {
                                    selectedItem = getDefaultTab(
                                        notesModeEnabled = notesMode,
                                        booksEnabled = booksTabEnabled,
                                        animeEnabled = false,
                                        mangaEnabled = mangaTabEnabled,
                                        tvEnabled = tvSeriesTabEnabled
                                    )
                                }
                            },
                            mangaTabEnabled = mangaTabEnabled,
                            onMangaTabEnabledChange = { enabled ->
                                mangaTabEnabled = enabled
                                prefs.edit { putBoolean("tab_manga_enabled", enabled) }
                                if (!enabled && selectedItem == NavigationItem.Manga) {
                                    selectedItem = getDefaultTab(
                                        notesModeEnabled = notesMode,
                                        booksEnabled = booksTabEnabled,
                                        animeEnabled = animeTabEnabled,
                                        mangaEnabled = false,
                                        tvEnabled = tvSeriesTabEnabled
                                    )
                                }
                            },
                            tvSeriesTabEnabled = tvSeriesTabEnabled,
                            onTvSeriesTabEnabledChange = { enabled ->
                                tvSeriesTabEnabled = enabled
                                prefs.edit { putBoolean("tab_tv_enabled", enabled) }
                                if (!enabled && selectedItem == NavigationItem.TVSeries) {
                                    selectedItem = getDefaultTab(
                                        notesModeEnabled = notesMode,
                                        booksEnabled = booksTabEnabled,
                                        animeEnabled = animeTabEnabled,
                                        mangaEnabled = mangaTabEnabled,
                                        tvEnabled = false
                                    )
                                }
                            },
                            onWorksUpdated = {
                                // Обновляем список произведений при добавлении через ProfileScreen
                                works = repository.getAllWorks()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        // Список показываем только когда детальный экран не открыт
                        if (selectedWork == null) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredWorks) { work ->
                                    WorkItemCard(
                                        workItem = work.toWorkItem(),
                                        onClick = { selectedWork = work }
                                    )
                                }
                            }
                        }
                    }
                }

                // Work Detail Screen (поверх списка, но внутри контентного Box, чтобы фон-обложка был виден)
                androidx.compose.animation.AnimatedVisibility(
                    visible = selectedWork != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    selectedWork?.let { work ->
                        if (detailSearchExpanded) {
                            val filtered = works
                                .filter {
                                    if (detailSearchQuery.isBlank()) true
                                    else it.title.contains(detailSearchQuery, ignoreCase = true) ||
                                        it.otherTitle?.contains(detailSearchQuery, ignoreCase = true) == true
                                }
                                .sortedBy { it.title.lowercase() }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered, key = { it.id }) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedWork = item
                                                detailSearchExpanded = false
                                                detailSearchQuery = ""
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(item.coverPath),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                color = titleColorBetween,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = when (item.type) {
                                                    WorkType.BOOK -> strings.books
                                                    WorkType.MANGA -> strings.manga
                                                    WorkType.ANIME -> strings.anime
                                                    WorkType.SERIES -> strings.tvSeries
                                                },
                                                color = iconTextColor.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            WorkDetailScreen(
                                work = work,
                                onBack = { selectedWork = null },
                                onEdit = {
                                    editingWork = work
                                    selectedWork = null
                                    showAddWorkScreen = true
                                },
                                onDelete = { workToDelete = work },
                                onSave = { updatedWork ->
                                    repository.saveWork(updatedWork)
                                    works = repository.getAllWorks()
                                    selectedWork = updatedWork
                                },
                                onCoverClick = { expandedCoverWork = work },
                                currentTheme = currentTheme
                            )
                        }
                    }
                }
            }
            
            // Add Work Screen
            androidx.compose.animation.AnimatedVisibility(
                visible = showAddWorkScreen,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut(animationSpec = tween(20)) + slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(20)
                )
            ) {
                AddWorkScreen(
                    onBack = { 
                        // Кнопка "Назад" в форме добавления/редактирования
                        if (editingWork != null) {
                            // При редактировании возвращаемся в просмотр произведения
                            selectedWork = editingWork
                        }
                        showAddWorkScreen = false
                        editingWork = null
                    },
                    onSave = { work ->
                        repository.saveWork(work)
                        works = repository.getAllWorks()
                        showAddWorkScreen = false
                        // После сохранения:
                        // - если это было редактирование — остаёмся в экране просмотра обновлённого произведения
                        // - если это новое произведение — остаёмся на вкладке со списком
                        selectedWork = if (editingWork != null) work else selectedWork
                        editingWork = null
                    },
                    work = editingWork // Pass work for editing
                )
            }
            
            // Bottom navigation bar
            BottomNavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { item ->
                    selectedItem = item
                    // При смене вкладки закрываем детальный экран, поиск внутри него и увеличенную обложку
                    selectedWork = null
                    detailSearchExpanded = false
                    detailSearchQuery = ""
                    expandedCoverWork = null
                    // Также закрываем форму добавления/редактирования
                    showAddWorkScreen = false
                    editingWork = null
                },
                currentTheme = currentTheme,
                notesEnabled = notesMode,
                booksEnabled = booksTabEnabled,
                animeEnabled = animeTabEnabled,
                mangaEnabled = mangaTabEnabled,
                tvSeriesEnabled = tvSeriesTabEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }

            // Полноэкранное увеличенное изображение обложки
            expandedCoverWork?.let { work ->
                val coverPath = work.coverPath
                if (coverPath != null && coverPath.isNotBlank()) {
                    val coverImageUri = if (coverPath.startsWith("/")) {
                        Uri.fromFile(File(coverPath))
                    } else {
                        coverPath.toUri()
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.95f))
                            .clickable { expandedCoverWork = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(coverImageUri)
                                    .build()
                            ),
                            contentDescription = strings.cover,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .fillMaxHeight(0.85f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

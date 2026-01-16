package com.example.library.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.library.data.NoteRepository
import com.example.library.data.Work
import com.example.library.data.WorkRepository
import com.example.library.data.WorkStatus
import com.example.library.data.WorkType
import com.example.library.ui.Language
import com.example.library.ui.LocalStrings
import com.example.library.ui.theme.MainBackgroundColor
import com.example.library.ui.theme.TitleColorBetween

enum class AppTheme {
    LIGHT,
    DARK
}

@Composable
fun ProfileScreen(
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    currentTheme: AppTheme = AppTheme.DARK,
    onThemeChange: (AppTheme) -> Unit = {},
    notesMode: Boolean = false,
    onNotesModeChange: (Boolean) -> Unit = {},
    booksTabEnabled: Boolean = true,
    onBooksTabEnabledChange: (Boolean) -> Unit = {},
    animeTabEnabled: Boolean = true,
    onAnimeTabEnabledChange: (Boolean) -> Unit = {},
    mangaTabEnabled: Boolean = true,
    onMangaTabEnabledChange: (Boolean) -> Unit = {},
    tvSeriesTabEnabled: Boolean = true,
    onTvSeriesTabEnabledChange: (Boolean) -> Unit = {},
    onWorksUpdated: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val mainBackgroundColor = MainBackgroundColor()
    val titleColorBetween = TitleColorBetween()
    val context = LocalContext.current
    val repository = remember { WorkRepository(context) }
    
    var showAddWorkScreen by remember { mutableStateOf(false) }
    var works by remember { mutableStateOf<List<Work>>(emptyList()) }

    val noteRepository = remember { NoteRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        coroutineScope.launch {
            var importedCount = 0
            uris.forEach { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.use { stream ->
                        val tempFile = java.io.File.createTempFile("import", ".md")
                        tempFile.outputStream().use { output ->
                            stream.copyTo(output)
                        }
                        noteRepository.importNoteFromFile(tempFile)
                        tempFile.delete()
                        importedCount++
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Toast.makeText(context, "Импортировано $importedCount файлов", Toast.LENGTH_SHORT).show()
        }
    }
    
    LaunchedEffect(Unit) {
        works = repository.getAllWorks()
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(mainBackgroundColor)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = strings.profile,
                color = titleColorBetween,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Statistics Card
            StatisticsCard(
                works = works,
                titleColorBetween = titleColorBetween,
                currentTheme = currentTheme
            )

            // Notes Mode Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF5A5568)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Режим заметок",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = notesMode,
                        onCheckedChange = onNotesModeChange
                    )
                }
            }

            // Add Work Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddWorkScreen = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF5A5568) // Чуть светлее чем было
                )
            ) {
                Text(
                    text = strings.addWork,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Import Files Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        importLauncher.launch("text/markdown")
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF5A5568)
                )
            ) {
                Text(
                    text = "Импортировать файлы",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Export Files Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val exportPath = repository.exportWorksToDownloads()
                        if (exportPath != null) {
                            Toast.makeText(
                                context,
                                "${strings.exportSuccess}\n$exportPath",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                strings.exportError,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF5A5568) // Чуть светлее чем было
                )
            ) {
                Text(
                    text = strings.exportFiles,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Tab visibility toggles (except Profile)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF5A5568)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Вкладки",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Книги",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = booksTabEnabled,
                            onCheckedChange = onBooksTabEnabledChange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Аниме",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = animeTabEnabled,
                            onCheckedChange = onAnimeTabEnabledChange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Манга",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = mangaTabEnabled,
                            onCheckedChange = onMangaTabEnabledChange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Сериалы",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = tvSeriesTabEnabled,
                            onCheckedChange = onTvSeriesTabEnabledChange
                        )
                    }
                }
            }

            // Язык теперь фиксирован на русском, блок выбора убран
        }
        
        // Add Work Screen with animation
        AnimatedVisibility(
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
                onBack = { showAddWorkScreen = false },
                onSave = { work ->
                    repository.saveWork(work)
                    works = repository.getAllWorks()
                    showAddWorkScreen = false
                    // Уведомляем родительский компонент об обновлении списка
                    onWorksUpdated()
                }
            )
        }
    }
}

@Composable
fun LanguageOption(
    language: Language,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleColorBetween = TitleColorBetween()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF494458) else Color(0xFF2A2A2A)
        )
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else titleColorBetween,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun StatisticsCard(
    works: List<Work>,
    titleColorBetween: Color,
    currentTheme: AppTheme = AppTheme.DARK
) {
    val strings = LocalStrings.current
    var selectedStatus by remember { mutableStateOf<WorkStatus?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<WorkType?>(null) }

    // Общая статистика по типам (используется для первой диаграммы)
    val totalWorks = works.size
    val byType = works.groupBy { it.type }
    val mangaCount = byType[WorkType.MANGA]?.size ?: 0
    val booksCount = byType[WorkType.BOOK]?.size ?: 0
    val animeCount = byType[WorkType.ANIME]?.size ?: 0
    val seriesCount = byType[WorkType.SERIES]?.size ?: 0

    // Статистика по статусам с учётом выбранного типа
    val filteredForStatus = selectedTypeFilter?.let { type ->
        works.filter { it.type == type }
    } ?: works
    val totalForStatus = filteredForStatus.size
    val byStatus = filteredForStatus.groupBy { it.status }

    // Цвета для диаграммы по статусам (оставляем цвета)
    val statusColors = mapOf(
        WorkStatus.IN_PLANS to Color(0xFF8E6687),      // rgb(142, 102, 147)
        WorkStatus.ABANDONED to Color(0xFFFF5F5A),     // rgb(255, 95, 90)
        WorkStatus.READING to Color(0xFF7179A4),       // rgb(113, 121, 164)
        WorkStatus.WATCHING to Color(0xFF7179A4),      // rgb(113, 121, 164)
        WorkStatus.READ to Color(0xFF79C77C),          // rgb(121, 199, 124)
        WorkStatus.WATCHED to Color(0xFF79C77C)        // rgb(121, 199, 124)
    )

    // Цвета для типов произведений (не пересекаются с цветами статусов)
    val typeColors = mapOf(
        WorkType.BOOK to Color(0xFF4E89AE),   // синий
        WorkType.ANIME to Color(0xFFF6AE2D),  // жёлто-оранжевый
        WorkType.MANGA to Color(0xFF55A630),  // зелёный
        WorkType.SERIES to Color(0xFFB56576)  // розовато-фиолетовый
    )
    
    val mainBackgroundColor = MainBackgroundColor()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = mainBackgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.statistics,
                color = titleColorBetween,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Диаграмма по типам произведений (монохромная)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.byType,
                color = titleColorBetween.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            DonutChartWithLegend(
                slices = listOf(
                    DonutSlice(strings.books, booksCount, typeColors[WorkType.BOOK] ?: titleColorBetween),
                    DonutSlice(strings.anime, animeCount, typeColors[WorkType.ANIME] ?: titleColorBetween),
                    DonutSlice(strings.manga, mangaCount, typeColors[WorkType.MANGA] ?: titleColorBetween),
                    DonutSlice(strings.tvSeries, seriesCount, typeColors[WorkType.SERIES] ?: titleColorBetween)
                ),
                total = totalWorks,
                showColorDots = true,
                titleColorBetween = titleColorBetween
            )

            // Переключатель типов для статистики по статусам
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = strings.byStatus,
                color = titleColorBetween.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val typeButtons = listOf<Pair<WorkType?, String>>(
                    null to strings.allTypes,
                    WorkType.BOOK to strings.books,
                    WorkType.ANIME to strings.anime,
                    WorkType.MANGA to strings.manga,
                    WorkType.SERIES to strings.tvSeries
                )
                typeButtons.forEach { (type, label) ->
                    val selected = selectedTypeFilter == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) titleColorBetween
                                else titleColorBetween.copy(alpha = 0.12f)
                            )
                            .clickable { selectedTypeFilter = type }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (selected) {
                                // Выбранная кнопка: фон titleColorBetween
                                // В светлой теме titleColorBetween темный (0xFF212121), нужен белый текст
                                // В темной теме titleColorBetween белый (0xFFFFFFFF), нужен черный текст
                                if (currentTheme == AppTheme.LIGHT) {
                                    Color.White // Белый текст на темном фоне
                                } else {
                                    Color.Black // Черный текст на белом фоне
                                }
                            } else {
                                // Невыбранная кнопка: фон titleColorBetween.copy(alpha = 0.12f)
                                // В светлой теме фон очень светлый (почти прозрачный темный), нужен темный текст
                                // В темной теме фон очень темный (почти прозрачный белый), нужен светлый текст
                                if (currentTheme == AppTheme.LIGHT) {
                                    titleColorBetween // Темный текст на светлом фоне
                                } else {
                                    Color.White // Белый текст на темном фоне
                                }
                            },
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            // Диаграмма по статусам (с цветами, без статуса "Отложено")
            Spacer(modifier = Modifier.height(8.dp))

            // Определяем какие статусы показывать в зависимости от выбранного типа
            val statusLabels = mapOf(
                WorkStatus.IN_PLANS to strings.inPlans,
                WorkStatus.READING to strings.reading,
                WorkStatus.WATCHING to strings.watching,
                WorkStatus.READ to strings.read,
                WorkStatus.WATCHED to strings.watched,
                WorkStatus.ABANDONED to strings.abandoned
            )
            
            // Для книг и манги показываем READING и READ, для остальных - WATCHING и WATCHED
            // При выборе "Все" объединяем READING/READ с WATCHING/WATCHED
            val slices = when (selectedTypeFilter) {
                WorkType.BOOK, WorkType.MANGA -> listOf(
                    DonutSlice(
                        label = statusLabels[WorkStatus.READING] ?: "",
                        value = (byStatus[WorkStatus.READING]?.size ?: 0),
                        color = statusColors[WorkStatus.READING] ?: titleColorBetween
                    ),
                    DonutSlice(
                        label = statusLabels[WorkStatus.IN_PLANS] ?: "",
                        value = (byStatus[WorkStatus.IN_PLANS]?.size ?: 0),
                        color = statusColors[WorkStatus.IN_PLANS] ?: titleColorBetween
                    ),
                    DonutSlice(
                        label = statusLabels[WorkStatus.READ] ?: "",
                        value = (byStatus[WorkStatus.READ]?.size ?: 0),
                        color = statusColors[WorkStatus.READ] ?: titleColorBetween
                    ),
                    DonutSlice(
                        label = statusLabels[WorkStatus.ABANDONED] ?: "",
                        value = (byStatus[WorkStatus.ABANDONED]?.size ?: 0),
                        color = statusColors[WorkStatus.ABANDONED] ?: titleColorBetween
                    )
                )
                else -> {
                    // Для "Все" или других типов объединяем READING/READ с WATCHING/WATCHED
                    val activeStatusCount = (byStatus[WorkStatus.READING]?.size ?: 0) + 
                                          (byStatus[WorkStatus.WATCHING]?.size ?: 0)
                    val completedStatusCount = (byStatus[WorkStatus.READ]?.size ?: 0) + 
                                               (byStatus[WorkStatus.WATCHED]?.size ?: 0)
                    val activeLabel = if (selectedTypeFilter == null) {
                        // При "Все" используем общий ярлык
                        strings.watching // или можно сделать "Читаю/Смотрю"
                    } else {
                        statusLabels[WorkStatus.WATCHING] ?: ""
                    }
                    val completedLabel = if (selectedTypeFilter == null) {
                        strings.watched // или можно сделать "Прочитано/Просмотрено"
                    } else {
                        statusLabels[WorkStatus.WATCHED] ?: ""
                    }
                    
                    listOf(
                        DonutSlice(
                            label = activeLabel,
                            value = activeStatusCount,
                            color = statusColors[WorkStatus.WATCHING] ?: titleColorBetween
                        ),
                        DonutSlice(
                            label = statusLabels[WorkStatus.IN_PLANS] ?: "",
                            value = (byStatus[WorkStatus.IN_PLANS]?.size ?: 0),
                            color = statusColors[WorkStatus.IN_PLANS] ?: titleColorBetween
                        ),
                        DonutSlice(
                            label = completedLabel,
                            value = completedStatusCount,
                            color = statusColors[WorkStatus.WATCHED] ?: titleColorBetween
                        ),
                        DonutSlice(
                            label = statusLabels[WorkStatus.ABANDONED] ?: "",
                            value = (byStatus[WorkStatus.ABANDONED]?.size ?: 0),
                            color = statusColors[WorkStatus.ABANDONED] ?: titleColorBetween
                        )
                    )
                }
            }

            DonutChartWithLegend(
                slices = slices,
                total = totalForStatus,
                showColorDots = true,
                titleColorBetween = titleColorBetween
            )
        }
    }
}

@Composable
private fun StatisticRow(
    label: String,
    value: String,
    titleColorBetween: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = titleColorBetween.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = titleColorBetween,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TypeStatisticRow(
    label: String,
    count: Int,
    total: Int,
    titleColorBetween: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = titleColorBetween.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress indicator
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = if (total > 0) count.toFloat() / total else 0f,
                    modifier = Modifier.size(24.dp),
                    color = titleColorBetween,
                    strokeWidth = 3.dp,
                    trackColor = titleColorBetween.copy(alpha = 0.2f)
                )
                Text(
                    text = count.toString(),
                    color = titleColorBetween,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class DonutSlice(
    val label: String,
    val value: Int,
    val color: Color
)

@Composable
private fun DonutChartWithLegend(
    slices: List<DonutSlice>,
    total: Int,
    showColorDots: Boolean,
    titleColorBetween: Color
) {
    val nonEmptySlices = slices.filter { it.value > 0 }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            nonEmptySlices.forEach { slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showColorDots) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                    }
                    Text(
                        text = slice.label,
                        color = titleColorBetween.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = slice.value.toString(),
                        color = titleColorBetween,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(140.dp)
                .padding(start = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diameter = size.minDimension
                val strokeWidth = diameter * 0.18f
                val radius = (diameter - strokeWidth) / 2f
                val topLeft = Offset(
                    (size.width - 2 * radius) / 2f,
                    (size.height - 2 * radius) / 2f
                )

                // Фон кольца
                drawArc(
                    color = titleColorBetween.copy(alpha = 0.15f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(2 * radius, 2 * radius),
                    topLeft = topLeft
                )

                if (total > 0 && nonEmptySlices.isNotEmpty()) {
                    var startAngle = -90f
                    nonEmptySlices.forEach { slice ->
                        val sweep = 360f * (slice.value.toFloat() / total)
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            size = Size(2 * radius, 2 * radius),
                            topLeft = topLeft
                        )
                        startAngle += sweep
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    trackColor: Color
) {
    Canvas(modifier = modifier) {
        val strokeWidthPx = strokeWidth.toPx()
        val radius = (size.minDimension - strokeWidthPx) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // Draw track
        drawCircle(
            color = trackColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidthPx)
        )
        
        // Draw progress
        if (progress > 0) {
            val sweepAngle = 360f * progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
        }
    }
}

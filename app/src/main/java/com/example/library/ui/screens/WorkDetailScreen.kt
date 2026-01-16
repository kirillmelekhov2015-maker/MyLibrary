package com.example.library.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.library.data.*
import com.example.library.ui.LocalStrings
import com.example.library.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDetailScreen(
    work: Work,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSave: (Work) -> Unit,
    onCoverClick: () -> Unit = {},
    currentTheme: AppTheme = AppTheme.DARK,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val mainBackgroundColor = MainBackgroundColor()
    val titleColorBetween = TitleColorBetween()
    val iconTextColor = IconTextColor()
    val searchBarColor = SearchBarColor()
    
    val statusLabel = when (work.status) {
        WorkStatus.READ -> strings.read
        WorkStatus.READING -> strings.reading
        WorkStatus.WATCHING -> strings.watching
        WorkStatus.WATCHED -> strings.watched
        WorkStatus.IN_PLANS -> strings.inPlans
        WorkStatus.ABANDONED -> strings.abandoned
    }
    
    // Цвета для статусов (те же, что в ProfileScreen)
    val statusColor = when (work.status) {
        WorkStatus.IN_PLANS -> Color(0xFF8E6687)      // rgb(142, 102, 147)
        WorkStatus.ABANDONED -> Color(0xFFFF5F5A)     // rgb(255, 95, 90)
        WorkStatus.READING -> Color(0xFF7179A4)       // rgb(113, 121, 164)
        WorkStatus.WATCHING -> Color(0xFF7179A4)      // rgb(113, 121, 164)
        WorkStatus.READ -> Color(0xFF79C77C)          // rgb(121, 199, 124)
        WorkStatus.WATCHED -> Color(0xFF79C77C)        // rgb(121, 199, 124)
    }
    
    // Лейблы для SeriesType и MangaType
    val seriesTypeLabel = when (work.seriesType) {
        SeriesType.TV_SERIES -> strings.tvSeries
        SeriesType.FILM -> strings.film
        SeriesType.CARTOON -> strings.cartoon
        SeriesType.DRAMA -> strings.drama
        null -> null
    }
    
    val mangaTypeLabel = when (work.mangaType) {
        MangaType.MANGA -> strings.manga
        MangaType.MANHWA -> strings.manhwa
        MangaType.MANHUA -> strings.manhua
        null -> null
    }
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    // Используем LocalClipboardManager: он помечен deprecated, но стабильно работает в текущей версии Compose
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    // Подготовим URI обложки один раз
    val coverImageUri: Uri? = remember(work.coverPath) {
        val path = work.coverPath
        if (path != null && path.isNotBlank()) {
            if (path.startsWith("/")) {
                Uri.fromFile(File(path))
            } else {
                Uri.parse(path)
            }
        } else {
            null
        }
    }

    val otherTitles = remember(work.otherTitle) {
        work.otherTitle
            ?.split(";", "\n")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }
    var otherTitlesExpanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            // Фон рисуется на уровне `LibraryScreen` (обложка + градиент + фон темы),
            // поэтому здесь оставляем прозрачным, чтобы не перекрывать его.
            .background(Color.Transparent)
    ) {
        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Область обложки
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
            ) {
                // Основная карточка обложки
                Card(
                    modifier = Modifier
                        .width(215.dp)
                        .height(340.dp)
                        .align(Alignment.Center)
                        .clickable(
                            enabled = coverImageUri != null,
                            onClick = { if (coverImageUri != null) onCoverClick() }
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = searchBarColor
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(coverImageUri)
                                        .build()
                                ),
                                contentDescription = strings.cover,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "📖",
                                fontSize = 72.sp
                            )
                        }
                    }
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = work.title,
                    color = titleColorBetween,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Other titles: show first one, tap to open bottom popup with all
                otherTitles.firstOrNull()?.let { firstOtherTitle ->
                    Text(
                        text = firstOtherTitle,
                        color = iconTextColor.copy(alpha = 0.7f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { otherTitlesExpanded = true }
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Статус с цветом (только текст и рамка)
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(statusLabel) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.Transparent,
                        labelColor = statusColor,
                        disabledContainerColor = Color.Transparent,
                        disabledLabelColor = statusColor
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        borderColor = statusColor,
                        borderWidth = 2.dp,
                        enabled = true
                    )
                )
                // SeriesType для сериалов
                seriesTypeLabel?.let { label ->
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.Transparent,
                            labelColor = iconTextColor,
                            disabledContainerColor = Color.Transparent,
                            disabledLabelColor = iconTextColor
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            borderColor = iconTextColor.copy(alpha = 0.5f),
                            borderWidth = 2.dp,
                            enabled = true
                        )
                    )
                }
                // MangaType для манги
                mangaTypeLabel?.let { label ->
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.Transparent,
                            labelColor = iconTextColor,
                            disabledContainerColor = Color.Transparent,
                            disabledLabelColor = iconTextColor
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            borderColor = iconTextColor.copy(alpha = 0.5f),
                            borderWidth = 2.dp,
                            enabled = true
                        )
                    )
                }
            }
            
            // Информационное поле (сезоны, эпизоды, год, страна)
            val infoParts = mutableListOf<String>()
            if (work.type == WorkType.SERIES) {
                work.seasons?.let { infoParts.add("${strings.seasonsView}: $it") }
            }
            if (work.type == WorkType.ANIME || work.type == WorkType.SERIES) {
                work.episodes?.let { infoParts.add("${strings.episodesView}: $it") }
            }
            if (work.type == WorkType.BOOK) {
                work.chapters?.let { infoParts.add("${strings.volumesView}: $it") }
                work.bookChapters?.let { infoParts.add("${strings.chaptersView}: $it") }
            }
            if (work.type == WorkType.MANGA) {
                work.chapters?.let { infoParts.add("${strings.chaptersView}: $it") }
            }
            work.year?.let { infoParts.add("${strings.year}: $it") }
            work.country?.let { infoParts.add("${strings.country}: $it") }
            
            if (infoParts.isNotEmpty()) {
                Surface(
                    color = mainBackgroundColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        infoParts.forEach { info ->
                            Text(
                                text = info,
                                color = if (currentTheme == AppTheme.DARK) Color.White else Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            // Description block – фон и текст зависят от темы (без заголовка "Описание")
            Surface(
                modifier = Modifier.padding(top = if (infoParts.isNotEmpty()) 1.dp else 0.dp),
                color = mainBackgroundColor,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = if (work.description.isBlank()) "Описание появится позже." else work.description,
                        color = titleColorBetween,
                        fontSize = 17.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Link field
            work.link?.takeIf { it.isNotBlank() }?.let { link ->
                Surface(
                    color = mainBackgroundColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Ссылка",
                            color = if (currentTheme == AppTheme.DARK) Color.White else Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = link,
                            color = Color(0xFF2196F3), // Blue color
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }

        }

        // Bottom popup with names (like sheet), animated over content
        androidx.compose.animation.AnimatedVisibility(
            visible = otherTitlesExpanded && otherTitles.isNotEmpty(),
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(
                initialOffsetY = { it / 3 }
            ),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it / 3 }
            ),
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter)
        ) {
            // Контейнер для фонового клика и самого Popup
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Фоновый слой: закрывает Popup только при клике за его пределами
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            otherTitlesExpanded = false
                        }
                )

                // Сам Popup: клики внутри него НЕ закрывают окно
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 0.dp, start = 16.dp, end = 16.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            // Поглощаем клик внутри Popup, чтобы не срабатывало закрытие
                        },
                    color = mainBackgroundColor,
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Название
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Название",
                                color = titleColorBetween,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(work.title))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Скопировать название",
                                    tint = iconTextColor
                                )
                            }
                        }
                        Text(
                            text = work.title,
                            color = iconTextColor,
                            fontSize = 14.sp
                        )

                        // Разделитель
                        HorizontalDivider(color = iconTextColor.copy(alpha = 0.2f))

                        // Альтернативные названия
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Альтернативные названия",
                                color = titleColorBetween,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(otherTitles.joinToString("\n")))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Скопировать альтернативные названия",
                                    tint = iconTextColor
                                )
                            }
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            otherTitles.forEach { t ->
                                Text(
                                    text = t,
                                    color = iconTextColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

/**
 * Преобразует дату из формата YYYY-MM-DD в ДД.ММ.ГГГГ
 * Если дата уже в формате ДД.ММ.ГГГГ, возвращает её без изменений
 */
private fun convertDateFormat(date: String): String {
    return try {
        // Проверяем, является ли дата в формате YYYY-MM-DD
        if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            val parts = date.split("-")
            if (parts.size == 3) {
                "${parts[2]}.${parts[1]}.${parts[0]}"
            } else {
                date
            }
        } else {
            // Если уже в формате ДД.ММ.ГГГГ или другом, возвращаем как есть
            date
        }
    } catch (e: Exception) {
        date
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    iconTextColor: Color,
    titleColorBetween: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = iconTextColor.copy(alpha = 0.7f),
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

@file:Suppress("DEPRECATION")

package com.example.library.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.library.data.*
import com.example.library.data.WorkRepository
import com.example.library.ui.LocalStrings
import com.example.library.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkScreen(
    onBack: () -> Unit,
    onSave: (Work) -> Unit,
    work: Work? = null, // For editing
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val mainBackgroundColor = MainBackgroundColor()
    val titleColorBetween = TitleColorBetween()
    val iconTextColor = IconTextColor()
    val searchBarColor = SearchBarColor()
    
    val context = LocalContext.current
    val repository = remember { WorkRepository(context) }
    
    var title by remember { mutableStateOf(work?.title ?: "") }
    var description by remember { mutableStateOf(work?.description ?: "") }
    var workType by remember { mutableStateOf<WorkType?>(work?.type) }
    var chapters by remember { mutableStateOf(work?.chapters?.toString() ?: "") }
    var bookChapters by remember { mutableStateOf(work?.bookChapters?.toString() ?: "") }
    var episodes by remember { mutableStateOf(work?.episodes?.toString() ?: "") }
    var seasons by remember { mutableStateOf(work?.seasons?.toString() ?: "") }
    var year by remember { mutableStateOf(work?.year?.toString() ?: "") }
    var country by remember { mutableStateOf(work?.country ?: "") }
    var status by remember { mutableStateOf<WorkStatus?>(work?.status) }
    var seriesType by remember { mutableStateOf<SeriesType?>(work?.seriesType) }
    var mangaType by remember { mutableStateOf<MangaType?>(work?.mangaType) }
    var coverPath by remember { mutableStateOf(work?.coverPath ?: "") }
    var link by remember { mutableStateOf(work?.link ?: "") }
    // Allow multiple alternative titles (one per line). If stored with separators, show as-is.
    var otherTitle by remember { mutableStateOf(work?.otherTitle ?: "") }
    var dateReadText by remember { 
        mutableStateOf(
            work?.dateRead?.let { 
                // Convert from YYYY-MM-DD to digits only (DDMMYYYY)
                val parts = it.split("-")
                if (parts.size == 3) {
                    parts[2] + parts[1] + parts[0] // DDMMYYYY
                } else {
                    it.filter { char -> char.isDigit() }
                }
            } ?: ""
        )
    }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val scrollState = rememberScrollState()
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Copy image to app's external storage (visible in Android/data)
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
                val imageFile = File(baseDir, "covers/${WorkRepository.generateId()}.jpg")
                imageFile.parentFile?.mkdirs()
                val outputStream = FileOutputStream(imageFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                coverPath = imageFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Handle system back button
    BackHandler(onBack = onBack)
    
    // Set default status based on work type
    LaunchedEffect(workType) {
        if (status == null && workType != null) {
            status = when (workType) {
                WorkType.BOOK, WorkType.MANGA -> WorkStatus.READING
                WorkType.ANIME, WorkType.SERIES -> WorkStatus.WATCHING
                else -> WorkStatus.IN_PLANS
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (work == null) strings.addWork else strings.editWork,
                        color = titleColorBetween,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancel,
                            tint = titleColorBetween
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = mainBackgroundColor
                )
            )
        },
        containerColor = mainBackgroundColor
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .animateContentSize(animationSpec = tween(250)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.title) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Other Title
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                OutlinedTextField(
                    value = otherTitle,
                    onValueChange = { otherTitle = it },
                    label = { Text(strings.otherTitle) },
                    placeholder = { Text("Каждое название с новой строки") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Description
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.description) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Work Type
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                TypeDropdown(
                    label = strings.type,
                    selectedType = workType,
                    onTypeSelected = { 
                        workType = it
                        // Reset dependent fields
                        if (it != WorkType.SERIES) seriesType = null
                        if (it != WorkType.MANGA) mangaType = null
                        // Очищаем числовые поля, специфичные для типов,
                        // чтобы данные не «перетекали» между типами
                        chapters = ""
                        bookChapters = ""
                        episodes = ""
                        seasons = ""
                        // Set default status only if type is selected
                        if (it != null) {
                            status = when (it) {
                                WorkType.BOOK, WorkType.MANGA -> WorkStatus.READING
                                WorkType.ANIME, WorkType.SERIES -> WorkStatus.WATCHING
                                else -> WorkStatus.IN_PLANS
                            }
                        } else {
                            status = null
                        }
                    },
                    iconTextColor = iconTextColor,
                    searchBarColor = searchBarColor
                )
            }
            
            // Series Type (only for TV Series)
            AnimatedVisibility(
                visible = workType == WorkType.SERIES,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                SeriesTypeDropdown(
                    label = strings.tvSeriesType,
                    selectedType = seriesType,
                    onTypeSelected = { seriesType = it },
                    iconTextColor = iconTextColor,
                    searchBarColor = searchBarColor
                )
            }
            
            // Manga Type (only for Manga)
            AnimatedVisibility(
                visible = workType == WorkType.MANGA,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                MangaTypeDropdown(
                    label = strings.mangaType,
                    selectedType = mangaType,
                    onTypeSelected = { mangaType = it },
                    iconTextColor = iconTextColor,
                    searchBarColor = searchBarColor
                )
            }
            
            // Volumes (for Books)
            AnimatedVisibility(
                visible = workType == WorkType.BOOK,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = chapters,
                    onValueChange = { chapters = it },
                    label = { Text(strings.volumes) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Chapters (for Books)
            AnimatedVisibility(
                visible = workType == WorkType.BOOK,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = bookChapters,
                    onValueChange = { bookChapters = it },
                    label = { Text(strings.chapters) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Chapters (for Manga)
            AnimatedVisibility(
                visible = workType == WorkType.MANGA,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = chapters,
                    onValueChange = { chapters = it },
                    label = { Text(strings.chapters) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Episodes (for Anime)
            AnimatedVisibility(
                visible = workType == WorkType.ANIME,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = episodes,
                    onValueChange = { episodes = it },
                    label = { Text(strings.episodes) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Season + Episodes (for Series)
            AnimatedVisibility(
                visible = workType == WorkType.SERIES,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Season
                    OutlinedTextField(
                        value = seasons,
                        onValueChange = { seasons = it },
                        label = { Text(strings.seasons) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(searchBarColor, iconTextColor)
                    )
                    // Episodes inside season
                    OutlinedTextField(
                        value = episodes,
                        onValueChange = { episodes = it },
                        label = { Text(strings.episodes) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(searchBarColor, iconTextColor)
                    )
                }
            }
            
            // Year
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text(strings.year) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Country
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text(strings.country) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Cover Image Picker
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = searchBarColor
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverPath.isNotBlank()) {
                            val imageUri = if (selectedImageUri != null) {
                                selectedImageUri
                            } else if (coverPath.startsWith("/")) {
                                Uri.fromFile(File(coverPath))
                            } else {
                                null
                            }
                            
                            if (imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(context)
                                            .data(imageUri)
                                            .build()
                                    ),
                                    contentDescription = strings.cover,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = "Изображение выбрано",
                                    color = iconTextColor,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = strings.cover,
                                    tint = iconTextColor.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = strings.cover,
                                    color = iconTextColor.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Link
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Ссылка") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            // Status
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                StatusDropdown(
                    label = strings.status,
                    selectedStatus = status,
                    workType = workType,
                    onStatusSelected = { status = it },
                    iconTextColor = iconTextColor,
                    searchBarColor = searchBarColor
                )
            }
            
            // Date Read/Watched (only when status is READ or WATCHED)
            AnimatedVisibility(
                visible = status == WorkStatus.READ || status == WorkStatus.WATCHED,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                val dateLabel = when (workType) {
                    WorkType.BOOK, WorkType.MANGA -> strings.dateReadForBooks
                    WorkType.ANIME, WorkType.SERIES -> strings.dateWatched
                    else -> strings.dateRead
                }
                
                OutlinedTextField(
                    value = dateReadText,
                    onValueChange = { newValue ->
                        // Only allow digits, limit to 8 digits
                        val digitsOnly = newValue.filter { it.isDigit() }.take(8)
                        dateReadText = digitsOnly
                    },
                    label = { Text(dateLabel) },
                    placeholder = { Text("ДД.ММ.ГГГГ") },
                    visualTransformation = DateVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(searchBarColor, iconTextColor)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(20)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = tween(20)
                ),
                exit = fadeOut() + slideOutVertically()
            ) {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newWork = Work(
                                id = work?.id ?: WorkRepository.generateId(),
                                title = title,
                                description = description,
                                type = workType ?: WorkType.BOOK,
                                coverPath = coverPath.takeIf { it.isNotBlank() },
                                chapters = chapters.toIntOrNull(),
                                bookChapters = bookChapters.toIntOrNull(),
                                episodes = episodes.toIntOrNull(),
                                seasons = seasons.toIntOrNull(),
                                year = year.toIntOrNull(),
                                country = country.takeIf { it.isNotBlank() },
                                status = status ?: WorkStatus.IN_PLANS,
                                seriesType = seriesType,
                                mangaType = mangaType,
                                otherTitle = otherTitle
                                    .lines()
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                    .joinToString("; ")
                                    .takeIf { it.isNotBlank() },
                                dateRead = dateReadText.takeIf { it.isNotBlank() }?.let { 
                                    // Convert from DDMMYYYY to YYYY-MM-DD
                                    if (it.length == 8) {
                                        val day = it.substring(0, 2)
                                        val month = it.substring(2, 4)
                                        val year = it.substring(4, 8)
                                        "$year-$month-$day"
                                    } else {
                                        null
                                    }
                                },
                                link = link.takeIf { it.isNotBlank() }
                            )
                            onSave(newWork)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = strings.save,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun textFieldColors(containerColor: Color, textColor: Color) = TextFieldDefaults.colors(
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor,
    disabledContainerColor = containerColor,
    focusedIndicatorColor = textColor.copy(alpha = 0.6f),
    unfocusedIndicatorColor = textColor.copy(alpha = 0.3f),
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    focusedLabelColor = textColor.copy(alpha = 0.7f),
    unfocusedLabelColor = textColor.copy(alpha = 0.5f)
)

/**
 * VisualTransformation для форматирования даты в формате ДД.ММ.ГГГГ
 */
private class DateVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }
        
        val formatted = when {
            digitsOnly.isEmpty() -> ""
            digitsOnly.length <= 2 -> digitsOnly
            digitsOnly.length <= 4 -> "${digitsOnly.substring(0, 2)}.${digitsOnly.substring(2)}"
            else -> "${digitsOnly.substring(0, 2)}.${digitsOnly.substring(2, 4)}.${digitsOnly.substring(4)}"
        }
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val digitsBeforeOffset = text.text.substring(0, offset.coerceIn(0, text.length)).filter { it.isDigit() }.length
                return when {
                    digitsBeforeOffset == 0 -> 0
                    digitsBeforeOffset <= 2 -> digitsBeforeOffset
                    digitsBeforeOffset <= 4 -> digitsBeforeOffset + 1 // +1 for dot after day
                    else -> digitsBeforeOffset + 2 // +2 for two dots
                }.coerceIn(0, formatted.length)
            }
            
            override fun transformedToOriginal(offset: Int): Int {
                val textBeforeOffset = formatted.substring(0, offset.coerceIn(0, formatted.length))
                return textBeforeOffset.filter { it.isDigit() }.length
            }
        }
        
        return androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(formatted),
            offsetMapping
        )
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

/**
 * Преобразует дату из формата ДД.ММ.ГГГГ в YYYY-MM-DD для сохранения
 */
private fun convertDateToStorageFormat(date: String): String {
    return try {
        // Если дата в формате ДД.ММ.ГГГГ
        if (date.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
            val parts = date.split(".")
            if (parts.size == 3) {
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else {
                date
            }
        } else {
            // Если уже в формате YYYY-MM-DD или другом, возвращаем как есть
            date
        }
    } catch (e: Exception) {
        date
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeDropdown(
    label: String,
    selectedType: WorkType?,
    onTypeSelected: (WorkType?) -> Unit,
    iconTextColor: Color,
    searchBarColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    
    val types = listOf(
        null to "",
        WorkType.ANIME to strings.anime,
        WorkType.BOOK to strings.books,
        WorkType.MANGA to strings.manga,
        WorkType.SERIES to strings.tvSeries
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType?.let { type ->
                types.find { it.first == type }?.second ?: ""
            } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = textFieldColors(searchBarColor, iconTextColor)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(searchBarColor)
                .animateContentSize(animationSpec = tween(200))
        ) {
            types.forEach { (type, typeLabel) ->
                DropdownMenuItem(
                    text = { Text(if (typeLabel.isEmpty()) "" else typeLabel, color = iconTextColor) },
                    onClick = {
                        if (type != null) {
                            onTypeSelected(type)
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeriesTypeDropdown(
    label: String,
    selectedType: SeriesType?,
    onTypeSelected: (SeriesType?) -> Unit,
    iconTextColor: Color,
    searchBarColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    
    val types = listOf(
        null to "",
        SeriesType.TV_SERIES to strings.tvSeries,
        SeriesType.FILM to strings.film,
        SeriesType.CARTOON to strings.cartoon,
        SeriesType.DRAMA to strings.drama
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType?.let { type ->
                types.find { it.first == type }?.second ?: ""
            } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = textFieldColors(searchBarColor, iconTextColor)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(searchBarColor)
                .animateContentSize(animationSpec = tween(200))
        ) {
            types.forEach { (type, typeLabel) ->
                DropdownMenuItem(
                    text = { Text(if (typeLabel.isEmpty()) "" else typeLabel, color = iconTextColor) },
                    onClick = {
                        if (type != null) {
                            onTypeSelected(type)
                        } else {
                            onTypeSelected(null)
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MangaTypeDropdown(
    label: String,
    selectedType: MangaType?,
    onTypeSelected: (MangaType?) -> Unit,
    iconTextColor: Color,
    searchBarColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    
    val types = listOf(
        null to "",
        MangaType.MANGA to strings.manga,
        MangaType.MANHWA to strings.manhwa,
        MangaType.MANHUA to strings.manhua
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType?.let { type ->
                types.find { it.first == type }?.second ?: ""
            } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = textFieldColors(searchBarColor, iconTextColor)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(searchBarColor)
                .animateContentSize(animationSpec = tween(200))
        ) {
            types.forEach { (type, typeLabel) ->
                DropdownMenuItem(
                    text = { Text(if (typeLabel.isEmpty()) "" else typeLabel, color = iconTextColor) },
                    onClick = {
                        if (type != null) {
                            onTypeSelected(type)
                        } else {
                            onTypeSelected(null)
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    label: String,
    selectedStatus: WorkStatus?,
    workType: WorkType?,
    onStatusSelected: (WorkStatus?) -> Unit,
    iconTextColor: Color,
    searchBarColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    
    val availableStatuses = when (workType) {
        WorkType.BOOK, WorkType.MANGA -> listOf(
            null to "",
            WorkStatus.IN_PLANS to strings.inPlans,
            WorkStatus.READING to strings.reading,
            WorkStatus.READ to strings.read,
            WorkStatus.ABANDONED to strings.abandoned
        )
        WorkType.ANIME, WorkType.SERIES -> listOf(
            null to "",
            WorkStatus.IN_PLANS to strings.inPlans,
            WorkStatus.WATCHING to strings.watching,
            WorkStatus.WATCHED to strings.watched,
            WorkStatus.ABANDONED to strings.abandoned
        )
        else -> listOf(
            null to "",
            WorkStatus.IN_PLANS to strings.inPlans,
            WorkStatus.ABANDONED to strings.abandoned
        )
    }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedStatus?.let { status ->
                availableStatuses.find { it.first == status }?.second ?: ""
            } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = textFieldColors(searchBarColor, iconTextColor)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(searchBarColor)
                .animateContentSize(animationSpec = tween(200))
        ) {
            availableStatuses.forEach { (status, statusLabel) ->
                DropdownMenuItem(
                    text = { Text(if (statusLabel.isEmpty()) "" else statusLabel, color = iconTextColor) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

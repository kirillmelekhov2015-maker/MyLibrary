package com.example.library.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.example.library.data.Note
import com.example.library.data.NoteRepository
import com.example.library.ui.LocalStrings
import com.example.library.ui.theme.MainBackgroundColor
import com.example.library.ui.theme.TitleColorBetween
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val repository = remember { NoteRepository(context) }
    val mainBackgroundColor = MainBackgroundColor()
    val titleColorBetween = TitleColorBetween()

    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var showAddNoteScreen by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    LaunchedEffect(Unit) {
        notes = repository.getAllNotes()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Notes grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(mainBackgroundColor)
        ) {
            items(notes) { note ->
                NoteCard(
                    note = note,
                    onClick = { editingNote = note },
                    onLongClick = { noteToDelete = note }
                )
            }
        }
        
        // Floating Action Button (bottom right, above navigation bar)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp, bottom = 10.dp), // Slightly above navigation bar
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showAddNoteScreen = true },
                containerColor = Color(0xFFFFC107), // Yellow color like in the icon
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить заметку",
                    tint = Color.White
                )
            }
        }
        
        // Add/Edit Note Screen
        if (showAddNoteScreen || editingNote != null) {
            AddEditNoteScreen(
                note = editingNote,
                onBack = {
                    showAddNoteScreen = false
                    editingNote = null
                },
                onSave = { note ->
                    repository.saveNote(note)
                    notes = repository.getAllNotes()
                },
                onDelete = {
                    if (editingNote != null) {
                        repository.deleteNote(editingNote!!.id)
                        notes = repository.getAllNotes()
                    }
                    showAddNoteScreen = false
                    editingNote = null
                }
            )
        }
            
        // Delete confirmation dialog
        noteToDelete?.let { note ->
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
                title = { Text("Удалить заметку?", color = titleColorBetween) },
                text = { Text("«${note.title}» будет удалена. Это действие нельзя отменить.", color = titleColorBetween) },
                containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF333333) else Color.White,
                titleContentColor = titleColorBetween,
                textContentColor = titleColorBetween,
                confirmButton = {
                    TextButton(
                        onClick = {
                            repository.deleteNote(note.id)
                            notes = repository.getAllNotes()
                            if (editingNote?.id == note.id) {
                                editingNote = null
                                showAddNoteScreen = false
                            }
                            noteToDelete = null
                        }
                    ) { Text("Удалить") }
                },
                dismissButton = {
                    TextButton(onClick = { noteToDelete = null }) { Text("Отмена") }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val titleColorBetween = TitleColorBetween()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = titleColorBetween
            )
            Spacer(modifier = Modifier.height(4.dp))
            MarkdownText(
                markdown = note.content.take(100) + if (note.content.length > 100) "..." else "",
                maxLines = 4,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    color = titleColorBetween.copy(alpha = 0.7f)
                )
            )
        }
    }
}
    
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddEditNoteScreen(
    note: Note? = null,
    onBack: () -> Unit,
    onSave: (Note) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val mainBackgroundColor = MainBackgroundColor()
    val titleColorBetween = TitleColorBetween()
    val iconTextColor = com.example.library.ui.theme.IconTextColor()
    
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    
    val dateFormat = SimpleDateFormat("d MMMM HH:mm", Locale.forLanguageTag("ru"))
    val currentTime = System.currentTimeMillis()
    val noteDate = note?.updatedAt ?: currentTime
    val dateStr = dateFormat.format(Date(noteDate))
    val charCount = content.length

    // Back handler to return to notes tab
    BackHandler(enabled = true) {
        onBack()
    }
    
    Box(modifier = Modifier.fillMaxSize().background(mainBackgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header at search bar level (40dp from top to match old search bar)
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = titleColorBetween
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Delete icon (always show if onDelete is provided)
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = iconTextColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Title (smaller, light gray)
            androidx.compose.foundation.text.BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = titleColorBetween.copy(alpha = 0.7f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            "Заголовок",
                            color = titleColorBetween.copy(alpha = 0.5f),
                            fontSize = 20.sp
                        )
                    }
                    innerTextField()
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date and character count
            Text(
                text = "$dateStr | $charCount ${if (charCount == 1 || charCount % 10 == 1 && charCount % 100 != 11) "символ" else if (charCount % 10 in 2..4 && charCount % 100 !in 12..14) "символа" else "символов"}",
                color = iconTextColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content - now scrollable
            androidx.compose.foundation.text.BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = titleColorBetween,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            "Начните ввод",
                            color = iconTextColor.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
    
    // Auto-save when content changes (debounced) - save even if empty
    LaunchedEffect(title, content) {
        if (note?.title != title || note?.content != content) {
            kotlinx.coroutines.delay(1000) // Debounce 1 second
            val newNote = note?.copy(
                title = title,
                content = content,
                updatedAt = System.currentTimeMillis()
            ) ?: Note(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            onSave(newNote)
        }
    }
}
package com.example.library.data

import android.content.Context
import java.io.File
import java.util.UUID

class NoteRepository(private val context: Context) {
    private val notesDirectory: File
        get() {
            val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
            val dir = File(baseDir, "notes")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val files = notesDirectory.listFiles { _, name -> name.endsWith(".md") }
        files?.forEach { file ->
            try {
                val note = parseMarkdownFile(file)
                note?.let { notes.add(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return notes.sortedByDescending { it.updatedAt }
    }

    fun saveNote(note: Note): Boolean {
        return try {
            val filename = "${note.id}.md"
            val file = File(notesDirectory, filename)
            val content = noteToMarkdown(note)
            file.writeText(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteNote(noteId: String): Boolean {
        return try {
            val file = File(notesDirectory, "$noteId.md")
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importNoteFromFile(file: File): Note? {
        return try {
            val content = file.readText()
            val lines = content.lines()
            var title = file.nameWithoutExtension
            var noteContent = content
            if (lines.isNotEmpty() && lines[0].startsWith("# ")) {
                title = lines[0].substring(2).trim()
                noteContent = lines.drop(1).joinToString("\n").trim()
            }
            val note = Note(
                id = UUID.randomUUID().toString(),
                title = title,
                content = noteContent,
                createdAt = file.lastModified(),
                updatedAt = file.lastModified()
            )
            saveNote(note)
            note
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseMarkdownFile(file: File): Note? {
        return try {
            val content = file.readText()
            val lines = content.lines()
            var title = file.nameWithoutExtension
            var noteContent = ""
            var createdAt = file.lastModified()
            var updatedAt = file.lastModified()

            if (lines.size > 1 && lines[0] == "---") {
                var i = 1
                while (i < lines.size && lines[i] != "---") {
                    val line = lines[i]
                    when {
                        line.startsWith("title: ") -> title = line.substring(7).trim()
                        line.startsWith("createdAt: ") -> createdAt = line.substring(11).trim().toLongOrNull() ?: createdAt
                        line.startsWith("updatedAt: ") -> updatedAt = line.substring(11).trim().toLongOrNull() ?: updatedAt
                    }
                    i++
                }
                noteContent = lines.drop(i + 1).joinToString("\n").trim()
            } else {
                noteContent = content
            }

            Note(
                id = file.nameWithoutExtension,
                title = title,
                content = noteContent,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun noteToMarkdown(note: Note): String {
        val sb = StringBuilder()
        sb.appendLine("---")
        sb.appendLine("title: ${escapeMarkdown(note.title)}")
        sb.appendLine("createdAt: ${note.createdAt}")
        sb.appendLine("updatedAt: ${note.updatedAt}")
        sb.appendLine("---")
        sb.appendLine(note.content)
        return sb.toString()
    }

    private fun escapeMarkdown(text: String): String {
        return text.replace("\\", "\\\\").replace(":", "\\:")
    }

    fun getNotesDirectoryPath(): String {
        return notesDirectory.absolutePath
    }
}
package com.example.library.data

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class WorkRepository(private val context: Context) {
    private val worksDirectory: File
        get() {
            // Use external files directory so it's visible in Android/data
            val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
            val dir = File(baseDir, "works")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun getAllWorks(): List<Work> {
        val works = mutableListOf<Work>()
        val files = worksDirectory.listFiles { _, name -> name.endsWith(".md") }
        files?.forEach { file ->
            try {
                val work = parseMarkdownFile(file)
                work?.let { works.add(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return works
    }

    fun saveWork(work: Work): Boolean {
        return try {
            val filename = "${work.id}.md"
            val file = File(worksDirectory, filename)
            val content = workToMarkdown(work)
            file.writeText(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteWork(workId: String): Boolean {
        return try {
            val file = File(worksDirectory, "$workId.md")
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun workToMarkdown(work: Work): String {
        val sb = StringBuilder()
        sb.appendLine("---")
        sb.appendLine("id: ${work.id}")
        sb.appendLine("title: ${escapeMarkdown(work.title)}")
        sb.appendLine("type: ${work.type.name}")
        sb.appendLine("status: ${work.status.name}")
        work.coverPath?.let {
            sb.appendLine("cover: $it")
        }
        work.chapters?.let {
            sb.appendLine("chapters: $it")
        }
        work.bookChapters?.let {
            sb.appendLine("bookChapters: $it")
        }
        work.episodes?.let {
            sb.appendLine("episodes: $it")
        }
        work.seasons?.let {
            sb.appendLine("seasons: $it")
        }
        work.dateRead?.let {
            sb.appendLine("dateRead: $it")
        }
        work.year?.let {
            sb.appendLine("year: $it")
        }
        work.country?.let {
            sb.appendLine("country: ${escapeMarkdown(it)}")
        }
        work.seriesType?.let {
            sb.appendLine("seriesType: ${it.name}")
        }
        work.mangaType?.let {
            sb.appendLine("mangaType: ${it.name}")
        }
        work.otherTitle?.let {
            sb.appendLine("otherTitle: ${escapeMarkdown(it)}")
        }
        work.link?.let {
            sb.appendLine("link: ${escapeMarkdown(it)}")
        }
        sb.appendLine("---")
        sb.appendLine()
        sb.appendLine(work.description)
        return sb.toString()
    }

    private fun parseMarkdownFile(file: File): Work? {
        return try {
            val content = file.readText()
            val frontMatterRegex = Regex("---\\s*\\n([\\s\\S]*?)\\n---")
            val match = frontMatterRegex.find(content)
            if (match == null) return null

            val frontMatter = match.groupValues[1]
            val properties = frontMatter.split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .associate { line ->
                    val colonIndex = line.indexOf(':')
                    if (colonIndex > 0) {
                        val key = line.substring(0, colonIndex).trim()
                        val value = line.substring(colonIndex + 1).trim()
                        key to value
                    } else {
                        "" to ""
                    }
                }

            val id = properties["id"] ?: return null
            val title = properties["title"] ?: ""
            val type = WorkType.valueOf(properties["type"] ?: "BOOK")
            val status = WorkStatus.valueOf(properties["status"] ?: "IN_PLANS")
            val coverPath = properties["cover"]
            val chapters = properties["chapters"]?.toIntOrNull()
            val bookChapters = properties["bookChapters"]?.toIntOrNull()
            val episodes = properties["episodes"]?.toIntOrNull()
            val seasons = properties["seasons"]?.toIntOrNull()
            val year = properties["year"]?.toIntOrNull()
            val country = properties["country"]
            val seriesType = properties["seriesType"]?.let { SeriesType.valueOf(it) }
            val mangaType = properties["mangaType"]?.let { MangaType.valueOf(it) }
            val otherTitle = properties["otherTitle"]
            val dateRead = properties["dateRead"]
            val link = properties["link"]
            
            // Description is in the body after front matter
            val bodyStart = match.range.last + 1
            val description = content.substring(bodyStart).trim()

            Work(
                id = id,
                title = title,
                description = description,
                type = type,
                coverPath = coverPath,
                chapters = chapters,
                bookChapters = bookChapters,
                episodes = episodes,
                seasons = seasons,
                year = year,
                country = country,
                status = status,
                seriesType = seriesType,
                mangaType = mangaType,
                otherTitle = otherTitle,
                dateRead = dateRead,
                link = link
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeMarkdown(text: String): String {
        return text.replace("\n", "\\n").replace(":", "\\:")
    }

    fun exportWorksToDownloads(): String? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val exportDir = File(downloadsDir, "MyLibrary_Works")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val sourceFiles = worksDirectory.listFiles { _, name -> name.endsWith(".md") }
            sourceFiles?.forEach { sourceFile ->
                val destFile = File(exportDir, sourceFile.name)
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            
            exportDir.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getWorksDirectoryPath(): String {
        return worksDirectory.absolutePath
    }

    companion object {
        fun generateId(): String = UUID.randomUUID().toString()
    }
}

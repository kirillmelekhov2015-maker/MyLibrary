package com.example.library.data

enum class WorkType {
    ANIME,
    BOOK,
    MANGA,
    SERIES
}

enum class WorkStatus {
    READ,           // For books and manga
    READING,        // For books and manga
    WATCHING,       // For anime and TV series
    WATCHED,        // For anime and TV series
    IN_PLANS,       // For all works
    ABANDONED       // For all works
}

enum class SeriesType {
    TV_SERIES,
    FILM,
    CARTOON,
    DRAMA
}

enum class MangaType {
    MANGA,
    MANHWA,
    MANHUA
}

data class Work(
    val id: String,
    val title: String,
    val description: String = "",
    val type: WorkType,
    val coverPath: String? = null,  // Path to cover image
    val chapters: Int? = null,      // For manga (chapters) and books (volumes)
    val bookChapters: Int? = null,  // For books (chapters) - separate from volumes
    val episodes: Int? = null,      // For anime (episodes)
    val seasons: Int? = null,       // For series (seasons)
    val year: Int? = null,
    val country: String? = null,
    val status: WorkStatus,
    val seriesType: SeriesType? = null,  // For TV series
    val mangaType: MangaType? = null,    // For manga
    val otherTitle: String? = null,      // Alternative title
    val dateRead: String? = null,         // Date when work was read/watched (format: YYYY-MM-DD)
    val link: String? = null              // Link to the work
)

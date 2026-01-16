package com.example.library.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class Language {
    RUSSIAN
}

data class Strings(
    val profile: String,
    val anime: String,
    val books: String,
    val manga: String,
    val tvSeries: String,
    val tabBooks: String,
    val tabAnime: String,
    val tabManga: String,
    val tabSeries: String,
    val searchByWorkTitles: String,
    val search: String,
    val language: String,
    val selectLanguage: String,
    val russian: String,
    val english: String,
    val myTab: String,
    val latest: String,
    val ongoings: String,
    val announcements: String,
    val theme: String,
    val selectTheme: String,
    val light: String,
    val dark: String,
    val addWork: String,
    val statistics: String,
    val totalWorks: String,
    val byType: String,
    val byStatus: String,
    val title: String,
    val description: String,
    val type: String,
    val chapters: String,
    val volumes: String,
    val episodes: String,
    val seasons: String,
    val year: String,
    val country: String,
    val status: String,
    val cover: String,
    val save: String,
    val cancel: String,
    val selectType: String,
    val selectStatus: String,
    val read: String,
    val reading: String,
    val watching: String,
    val watched: String,
    val inPlans: String,
    val abandoned: String,
    val tvSeriesType: String,
    val film: String,
    val cartoon: String,
    val drama: String,
    val mangaType: String,
    val manhwa: String,
    val manhua: String,
    val otherTitle: String,
    val dateRead: String,
    val dateReadForBooks: String,
    val dateWatched: String,
    val exportFiles: String,
    val exportSuccess: String,
    val exportError: String,
    val filesLocation: String,
    val editWork: String,
    val allTypes: String,
    val chaptersView: String,
    val volumesView: String,
    val episodesView: String,
    val seasonsView: String
)

val LocalStrings = compositionLocalOf<Strings> { error("No Strings provided") }

object LocalizedStrings {
    val russian = Strings(
        profile = "Профиль",
        anime = "Аниме",
        books = "Книги",
        manga = "Манга",
        tvSeries = "Сериалы",
        tabBooks = "Книги",
        tabAnime = "Аниме",
        tabManga = "Манга",
        tabSeries = "Сериалы",
        searchByWorkTitles = "Поиск по названиям произведений",
        search = "Поиск",
        language = "Язык",
        selectLanguage = "Выберите язык",
        russian = "Русский",
        english = "English",
        myTab = "Моя вкладка",
        latest = "Последнее",
        ongoings = "Онгоинги",
        announcements = "Анон",
        theme = "Тема",
        selectTheme = "Выберите тему",
        light = "Светлая",
        dark = "Тёмная",
        addWork = "Добавить произведение",
        statistics = "Статистика",
        totalWorks = "Всего произведений",
        byType = "По типам",
        byStatus = "По статусам",
        title = "Название",
        description = "Описание",
        type = "Тип",
        chapters = "Главы",
        volumes = "Тома",
        episodes = "Эпизоды",
        seasons = "Сезоны",
        year = "Год",
        country = "Страна",
        status = "Статус",
        cover = "Обложка",
        save = "Сохранить",
        cancel = "Отмена",
        selectType = "Выберите тип",
        selectStatus = "Выберите статус",
        read = "Прочитано",
        reading = "Читаю",
        watching = "Смотрю",
        watched = "Просмотрено",
        inPlans = "В планах",
        abandoned = "Заброшено",
        tvSeriesType = "Тип сериала",
        film = "Фильм",
        cartoon = "Мультфильм",
        drama = "Дорама",
        mangaType = "Тип манги",
        manhwa = "Манхва",
        manhua = "Маньхуа",
        otherTitle = "Другое название",
        dateRead = "Дата прочтения (просмотра)",
        dateReadForBooks = "Дата прочтения",
        dateWatched = "Дата просмотра",
        exportFiles = "Экспортировать файлы",
        exportSuccess = "Файлы экспортированы в Downloads/MyLibrary_Works",
        exportError = "Ошибка при экспорте файлов",
        filesLocation = "Расположение файлов",
        editWork = "Редактировать произведение",
        allTypes = "Все",
        chaptersView = "Главы",
        volumesView = "Томов",
        episodesView = "Эпизодов",
        seasonsView = "Сезонов"
    )

    // English strings removed – app uses only Russian now.
}

@Composable
fun rememberLanguageState(): LanguageState {
    return remember { LanguageState() }
}

class LanguageState {
    var currentLanguage by mutableStateOf(Language.RUSSIAN)
        private set

    fun setLanguage(language: Language) {
        currentLanguage = language
    }

    val strings: Strings
        get() = when (currentLanguage) {
            Language.RUSSIAN -> LocalizedStrings.russian
        }
}

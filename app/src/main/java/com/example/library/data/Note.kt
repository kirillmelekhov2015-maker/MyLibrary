package com.example.library.data

data class Note(
    val id: String,
    val title: String,
    val content: String, // Markdown content
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
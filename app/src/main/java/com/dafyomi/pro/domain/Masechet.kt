package com.dafyomi.pro.domain

data class Masechet(
    val id: Int,
    val hebrew: String,
    val english: String,
    val transliteration: String,
    val pronunciation: String,
    val dafCount: Int
)

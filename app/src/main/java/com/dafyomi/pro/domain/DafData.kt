package com.dafyomi.pro.domain

data class DafData(
    val dafIndex: Int,
    val masechet: Masechet,
    val dafNumber: Int,
    val cycleDay: Int,
    val cyclePercent: Float,
    val summary: String
)

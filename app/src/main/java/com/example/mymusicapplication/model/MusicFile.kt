package com.example.mymusicapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicFile(
    val path: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String
) : Parcelable

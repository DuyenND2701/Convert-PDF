package com.example.covertpdfapplication.uploadfile.model

import android.net.Uri

data class SelectedFile(
    val name: String,
    val size: String,
    val time: String,
    val uri: Uri,
    var isSelected: Boolean = false,
    var version: Int = 1
)
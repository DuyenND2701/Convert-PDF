package com.example.covertpdfapplication.convert.converttopdf.interface1

import android.content.Context
import android.net.Uri
import java.io.File

interface IConverterToPdf {
    suspend fun convertToPdf(context: Context, uris: List<Uri>): File?
}
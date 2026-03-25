package com.example.covertpdfapplication.convert.converttopdf.factory

import android.content.Context
import com.example.covertpdfapplication.convert.converttopdf.interface1.IConverterToPdf
import com.example.covertpdfapplication.convert.converttopdf.service.ConvertPresentationService

class ConvertToPdfFactory(private val context: Context) {

    fun getConverter(ext: String): IConverterToPdf? {
        return when (ext.lowercase()) {
            "pptx", "ppt", "odp" -> ConvertPresentationService(context)
            else -> null
        }
    }
}
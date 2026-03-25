package com.example.covertpdfapplication.pdfconvertto.model

import android.graphics.Color

sealed class PdfContent{
    data class  Text(val value:String,val font:String, val size:Float,val color: String, val x: Float, val y: Float): PdfContent()
    data class  Image(val base64:String, val x: Float, val y: Float, val width: Float, val height: Float): PdfContent()
    data class Shape(val type: String, val properties: Map<String, Any>):PdfContent()
    data class Annotation(val note: String, val target: String?) : PdfContent()
}

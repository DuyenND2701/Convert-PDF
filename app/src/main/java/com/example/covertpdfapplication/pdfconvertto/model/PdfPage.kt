package com.example.covertpdfapplication.pdfconvertto.model

data class PdfPage(
    val pageNumber: Int,
    val content: List<PdfContent>
)

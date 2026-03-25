package com.example.covertpdfapplication.pdfconvertto.model

data class PdfDocumentJson(
    val title: String,
    val author: String,
    val pages: List<PdfPage>
)

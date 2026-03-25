package com.example.covertpdfapplication.common

object FormatData {
    val sourceFormatMap = linkedMapOf(
        "Presentation" to listOf("PPTX", "PPT", "ODP"),
        "Image" to listOf("JPG", "JPEG", "PNG", "GIF", "BMP", "TIFF", "WEBP", "SVG"),
        "Web" to listOf("HTML", "ZIP"),
        "Text" to listOf("MD", "TXT", "RTF"),
        "Email" to listOf("EML", "MSG"),
        "eBook" to listOf("EPUB", "MOBI", "AZW3", "FB2"),
        "Multiple Files" to listOf("Any", "Images"),
        "Document" to listOf("PDF", "DOCX", "DOC", "ODT"),
        "Archive" to listOf("CBZ", "CBR"),
        "Spreadsheet" to listOf("XLSX", "XLS", "ODS")
    )

    val targetFormatMap = mapOf(
        "Document" to listOf("PDF/A", "PDF/X", "DOCX", "ODT"),
        "Archive" to listOf("CBZ"),
        "Spreadsheet" to listOf("CSV", "XLSX"),
        "Presentation" to listOf("PPTX", "ODP"),
        "Text" to listOf("TXT", "RTF", "MD"),
        "Image" to listOf("PNG", "JPG", "GIF", "TIFF", "BMP", "WEBP"),
        "Web" to listOf("HTML", "XML"),
        "eBook" to listOf("EPUB", "AZW3")
    )
}
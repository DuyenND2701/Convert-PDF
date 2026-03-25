package com.example.covertpdfapplication.pdfconvertto.model

import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition

class RichTextStripper : PDFTextStripper() {
    val texts = mutableListOf<PdfContent.Text>()

    override fun processTextPosition(text: TextPosition) {
        val value = text.unicode
        val fontName = text.font?.name ?: "Unknown"
        val fontSize = text.fontSizeInPt
        val x = text.xDirAdj
        val y = text.yDirAdj

        // Màu chữ từ graphicsState
        val color = graphicsState.nonStrokingColor?.toRGB() ?: "#000000"

        texts.add(
            PdfContent.Text(
                value = value,
                font = fontName,
                size = fontSize,
                color = color.toString(),
                x = x,
                y = y
            )
        )
    }

}
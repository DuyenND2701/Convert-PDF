package com.example.covertpdfapplication.pdfconvertto.convert

import android.content.Context
import android.net.Uri
import com.example.covertpdfapplication.pdfconvertto.model.ImageExtractorWithCoords
import com.example.covertpdfapplication.pdfconvertto.model.PdfContent
import com.example.covertpdfapplication.pdfconvertto.model.PdfDocumentJson
import com.example.covertpdfapplication.pdfconvertto.model.PdfPage
import com.example.covertpdfapplication.pdfconvertto.model.RichTextStripper
import com.google.gson.GsonBuilder
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import org.json.JSONObject

class PdfToJson {
    object PdfToJson {
        val pages = mutableListOf<PdfPage>()
        fun parsePdfToJson(context: Context, pdfFilePath: Uri): JSONObject {
            val inputStream = context.contentResolver.openInputStream(pdfFilePath)
            val document = PDDocument.load(inputStream)
            val gson = GsonBuilder().setPrettyPrinting().create()
            val stripper = RichTextStripper()

            for (i in 1..document.numberOfPages) {
                stripper.startPage = i
                stripper.endPage = i
                stripper.getText(document)
                val pageText = stripper.texts
                val contentList = mutableListOf<PdfContent>()
                val page = document.getPage(i - 1)
                if (getImagefromPage(page).isNotEmpty()) {
                    contentList.addAll(getImagefromPage(page))
                }
                contentList.addAll(pageText)

                pages.add(PdfPage(i, contentList))
            }

            val pdfJson = PdfDocumentJson(
                title = document.documentInformation?.title ?: "",
                author = document.documentInformation?.author ?: "",
                pages = pages
            )
            document.close()
            inputStream?.close()
            return JSONObject(gson.toJson(pdfJson))
        }

        private fun getImagefromPage(page: PDPage): List<PdfContent.Image> {
            val extractor = ImageExtractorWithCoords(page)
            extractor.processPage(page)
            return extractor.images
        }
    }
}
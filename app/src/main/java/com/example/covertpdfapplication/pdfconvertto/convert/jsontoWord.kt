package com.example.covertpdfapplication.pdfconvertto.convert

import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.Document
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.json.JSONObject
import java.io.FileOutputStream

class jsontoWord(json: JSONObject, outputPath: String) {
    fun parsePageContent(page: JSONObject): List<JSONObject> {
        val content = page.getJSONArray("content")
        return (0 until content.length()).map { content.getJSONObject(it) }
    }

    fun sortByY(contents: List<JSONObject>): List<JSONObject> {
        return contents.sortedBy { it.optDouble("y", 0.0) }
    }

    fun groupByY(contents: List<JSONObject>, threshold: Double = 50.0): List<List<JSONObject>> {
        val grouped = mutableListOf<List<JSONObject>>()
        var currentGroup = mutableListOf<JSONObject>()

        for (item in contents) {
            if (currentGroup.isEmpty()) {
                currentGroup.add(item)
            } else {
                val lastY = currentGroup.last().optDouble("y", 0.0)
                if (Math.abs(item.optDouble("y", 0.0) - lastY) < threshold) {
                    currentGroup.add(item)
                } else {
                    grouped.add(currentGroup)
                    currentGroup = mutableListOf(item)
                }
            }
        }
        if (currentGroup.isNotEmpty()) grouped.add(currentGroup)
        return grouped
    }
    fun renderGroupToWord(document: XWPFDocument, group: List<JSONObject>) {
        val para = document.createParagraph()
        val run = para.createRun()

        for (item in group) {
            when (item.getString("type")) {
                "Text" -> {
                    run.addCarriageReturn()
                    run.setText(item.getString("value"))
                    run.fontFamily = item.optString("font", "Times New Roman")
                    run.fontSize = item.optInt("size", 12)
                    run.setColor(item.optString("color", "000000"))
                }
                "Image" -> {
                    val base64 = item.getString("base64")
                    val bytes = java.util.Base64.getDecoder().decode(base64)
                    val inputStream = bytes.inputStream()
                    run.addPicture(
                        inputStream,
                        Document.PICTURE_TYPE_PNG,
                        "image.png",
                        Units.toEMU(item.optDouble("width", 100.0)),
                        Units.toEMU(item.optDouble("height", 100.0))
                    )
                }
            }
        }
    }

    fun jsonToWord(json: JSONObject, outputPath: String) {
        val document = XWPFDocument()

        val pages = json.getJSONArray("pages")
        for (i in 0 until pages.length()) {
            val page = pages.getJSONObject(i)
            val contents = parsePageContent(page)
            val sorted = sortByY(contents)
            val grouped = groupByY(sorted)

            for (group in grouped) {
                renderGroupToWord(document, group)
            }
        }

        FileOutputStream(outputPath).use { out ->
            document.write(out)
        }
    }
}
package com.example.covertpdfapplication.common
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommonFun {
    fun saveWordWithOriginalName(document: XWPFDocument, inputFile: File, outputDir: File) {
        val originalName = inputFile.nameWithoutExtension // "myfile"
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.US)
        val datePart = formatter.format(Date())
        val outputFile = File(outputDir, "${originalName}_${datePart}.docx")

        FileOutputStream(outputFile).use { out ->
            document.write(out)
        }

        println("File Word đã được lưu: ${outputFile.absolutePath}")
    }

}
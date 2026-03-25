package com.example.covertpdfapplication.convert.converttopdf.service

import android.content.Context
import android.graphics.*
import android.graphics.Canvas
import android.net.Uri
import com.example.covertpdfapplication.convert.converttopdf.interface1.IConverterToPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFTextShape
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.hslf.usermodel.HSLFTextShape
import com.itextpdf.kernel.pdf.*
import com.itextpdf.layout.*
import com.itextpdf.layout.element.Image
import com.itextpdf.io.image.ImageDataFactory
import java.io.*
import java.util.zip.ZipInputStream

class ConvertPresentationService(
    private val context: Context
) : IConverterToPdf {

    override suspend fun convertToPdf(context: Context, uris: List<Uri>): File? {
        return withContext(Dispatchers.IO) {

            if (uris.isEmpty()) return@withContext null

            val outputFile = File(context.cacheDir, "presentation_${System.currentTimeMillis()}.pdf")

            val document = Document(PdfDocument(PdfWriter(outputFile)))

            try {

                uris.forEach { uri ->
                    val file = copyUriToFile(context, uri)
                    when (file.extension.lowercase()) {
                        "pptx" -> convertPptx(file, document)
                        "ppt" -> convertPpt(file, document)
                        "odp" -> convertOdp(file, document)
                    }
                }

                document.close()
                outputFile

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun copyUriToFile(context: Context, uri: Uri): File {
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }

    private fun convertPptx(file: File, document: Document) {
        val ppt = XMLSlideShow(FileInputStream(file))

        ppt.slides.forEachIndexed { index, slide ->
            val bitmap = createBitmap()
            val canvas = Canvas(bitmap)
            val paint = createPaint()

            var y = 100f
            canvas.drawText("Slide ${index + 1}", 50f, y, paint)
            y += 60f

            slide.shapes.forEach {
                if (it is XSLFTextShape) {
                    it.text.split("\n").forEach { line ->
                        if (line.isNotEmpty()) {
                            canvas.drawText(line, 50f, y, paint)
                            y += 50f
                        }
                    }
                }
            }

            addToPdf(bitmap, document)
        }

        ppt.close()
    }

    private fun convertPpt(file: File, document: Document) {
        val ppt = HSLFSlideShow(FileInputStream(file))

        ppt.slides.forEachIndexed { index, slide ->
            val bitmap = createBitmap()
            val canvas = Canvas(bitmap)
            val paint = createPaint()

            var y = 100f
            canvas.drawText("Slide ${index + 1}", 50f, y, paint)
            y += 60f

            slide.shapes.forEach {
                if (it is HSLFTextShape) {
                    it.text.split("\n").forEach { line ->
                        if (line.isNotEmpty()) {
                            canvas.drawText(line, 50f, y, paint)
                            y += 50f
                        }
                    }
                }
            }

            addToPdf(bitmap, document)
        }

        ppt.close()
    }

    private fun convertOdp(file: File, document: Document) {
        val tempDir = File(context.cacheDir, "odp_temp")
        tempDir.mkdirs()

        unzip(file, tempDir)

        val xmlFile = File(tempDir, "content.xml")
        val xml = xmlFile.readText()

        val texts = Regex("<text:p[^>]*>(.*?)</text:p>")
            .findAll(xml)
            .map { it.groupValues[1] }
            .toList()

        texts.chunked(10).forEachIndexed { index, chunk ->
            val bitmap = createBitmap()
            val canvas = Canvas(bitmap)
            val paint = createPaint()

            var y = 100f
            canvas.drawText("Slide ${index + 1}", 50f, y, paint)
            y += 60f

            chunk.forEach {
                val clean = it.replace(Regex("<.*?>"), "")
                canvas.drawText(clean, 50f, y, paint)
                y += 50f
            }

            addToPdf(bitmap, document)
        }

        tempDir.deleteRecursively()
    }

    private fun createBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888)
        Canvas(bmp).drawColor(Color.WHITE)
        return bmp
    }

    private fun createPaint(): Paint {
        return Paint().apply {
            color = Color.BLACK
            textSize = 36f
            isAntiAlias = true
        }
    }

    private fun addToPdf(bitmap: Bitmap, document: Document) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = Image(ImageDataFactory.create(stream.toByteArray()))
        image.setAutoScale(true)
        document.add(image)
    }

    private fun unzip(zip: File, target: File) {
        val zis = ZipInputStream(FileInputStream(zip))
        var entry = zis.nextEntry
        val buffer = ByteArray(1024)

        while (entry != null) {
            val newFile = File(target, entry.name)
            if (entry.isDirectory) newFile.mkdirs()
            else {
                newFile.parentFile?.mkdirs()
                FileOutputStream(newFile).use { fos ->
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
        zis.close()
    }
}